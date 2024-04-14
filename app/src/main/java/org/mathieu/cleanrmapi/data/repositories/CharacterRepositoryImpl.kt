package org.mathieu.cleanrmapi.data.repositories

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.mathieu.cleanrmapi.common.extensions.mapElement
import org.mathieu.cleanrmapi.common.extensions.toList
import org.mathieu.cleanrmapi.data.local.CharacterLocal
import org.mathieu.cleanrmapi.data.local.objects.CharacterObject
import org.mathieu.cleanrmapi.data.local.objects.toDetailedModel
import org.mathieu.cleanrmapi.data.local.objects.toModel
import org.mathieu.cleanrmapi.data.local.objects.toRealmObject
import org.mathieu.cleanrmapi.data.remote.CharacterApi
import org.mathieu.cleanrmapi.data.remote.EpisodeApi
import org.mathieu.cleanrmapi.data.remote.responses.CharacterResponse
import org.mathieu.cleanrmapi.data.validators.annotations.MustBeCommaSeparatedIds
import org.mathieu.cleanrmapi.domain.character.models.Character
import org.mathieu.cleanrmapi.domain.character.models.CharacterDetails
import org.mathieu.cleanrmapi.domain.episode.models.Episode
import org.mathieu.cleanrmapi.domain.character.CharacterRepository

private const val CHARACTER_PREFS = "character_repository_preferences"
private val nextPage = intPreferencesKey("next_characters_page_to_load")

private val Context.dataStore by preferencesDataStore(
    name = CHARACTER_PREFS
)

internal class CharacterRepositoryImpl(
    private val context: Context,
    private val episodeApi: EpisodeApi,
    private val characterApi: CharacterApi,
    private val characterLocal: CharacterLocal
) : CharacterRepository {

    override suspend fun getCharacters(): Flow<List<Character>> =
        characterLocal
            .getCharacters()
            .mapElement(transform = CharacterObject::toModel)
            .also { if (it.first().isEmpty()) fetchNext() }


    /**
     * Fetches the next batch of characters and saves them to local storage.
     *
     * This function works as follows:
     * 1. Reads the next page number from the data store.
     * 2. If there's a valid next page (i.e., page is not -1), it fetches characters from the API for that page.
     * 3. Extracts the next page number from the API response and updates the data store with it.
     * 4. Transforms the fetched character data into their corresponding realm objects.
     * 5. Saves the transformed realm objects to the local database.
     *
     * Note: If the `next` attribute from the API response is null or missing, the page number is set to -1, indicating there's no more data to fetch.
     */
    private suspend fun fetchNext() {

        val page = context.dataStore.data.map { prefs -> prefs[nextPage] }.first()

        if (page != -1) {

            val response = characterApi.getCharacters(page)

            val nextPageToLoad = response.info.next?.split("?page=")?.last()?.toInt() ?: -1

            context.dataStore.edit { prefs -> prefs[nextPage] = nextPageToLoad }

            val objects = response.results.map(transform = CharacterResponse::toRealmObject)

            characterLocal.saveCharacters(objects)
        }

    }


    override suspend fun loadMore() = fetchNext()


    /**
     * Retrieves the character with the specified ID.
     *
     * The function follows these steps:
     * 1. Tries to fetch the character from the local storage.
     * 2. If not found locally, it fetches the character from the API.
     * 3. Upon successful API retrieval, it saves the character to local storage.
     * 4. If the character is still not found, it throws an exception.
     *
     * @param id The unique identifier of the character to retrieve.
     * @return The [Character] object representing the character details.
     * @throws Exception If the character cannot be found both locally and via the API.
     */
    override suspend fun getCharacterDetailed(id: Int): CharacterDetails {

        val characterLocal = GetCharacterObjectIfExists(characterId = id)

        return characterLocal.toDetailedModel(
            idsToEpisodesConverter = ::getEpisodesFromIdList
        )

    }

    override suspend fun getEpisodesWhere(characterId: Int): List<Episode> {
        val characterLocal = GetCharacterObjectIfExists(characterId)

        return getEpisodesFromIdList(idList = characterLocal.episodesIds)

    }

    private suspend fun getEpisodesFromIdList(@MustBeCommaSeparatedIds idList: String): List<Episode> {

        //TODO: fetch system

        return if (idList.contains(",")) {
            val episodesResponse = episodeApi.getEpisodesFromIds(ids = idList)
            episodesResponse.map { it.toRealmObject().toModel() }
        } else {
            val episodeResponse = episodeApi.getEpisode(idList.toInt())
            episodeResponse?.toRealmObject()?.toModel()?.toList() ?: emptyList()
        }

    }

}

/**
 * Orchestrates the retrieval of a CharacterObject by attempting to fetch it locally first,
 * then remotely if it's not found in the local storage.
 *
 *  Note: By abstracting away part of the `getCharacter` repository implementation logic into a sequential,
 *  clearly-defined process, this approach aims to improve code readability and maintainability.
 *  It leverages the principle of separation of concerns, ensuring efficient data retrieval by minimizing
 *  network requests and providing a robust mechanism for error handling when character data cannot be found.
 *
 * @param characterId The unique identifier for the character to be retrieved. This ID is used first
 * to attempt to fetch the character from local storage and then from a remote source if necessary.
 *
 * @return A CharacterObject instance representing the character details. If the character is not found
 * locally, it is fetched from the remote API, converted into a realm object, and saved locally before
 * being returned.
 *
 * @throws Exception when the character cannot be found both locally and remotely.
 *
 */
private object GetCharacterObjectIfExists : KoinComponent {

    private val characterApi: CharacterApi by inject()
    private val characterLocal: CharacterLocal by inject()


    suspend operator fun invoke(characterId: Int): CharacterObject =
        tryToGetCharacterLocally(characterId)
            .fetchRemotelyIfNotFound(characterId)
            .throwIfWeCannotFindIt()


    private suspend fun tryToGetCharacterLocally(id: Int) = characterLocal.getCharacter(id)

    private suspend fun CharacterObject?.fetchRemotelyIfNotFound(id: Int): CharacterObject? {
        if (this != null) return this

        return characterApi.getCharacter(id = id)
            ?.toRealmObject()
            ?.also { obj ->
                characterLocal.insert(obj)
            }
    }

    private fun CharacterObject?.throwIfWeCannotFindIt(): CharacterObject {
        if (this != null) return this
        throw Exception("Could not find Character locally and remotely.")
    }

}
