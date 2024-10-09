package org.mathieu.characters.details

import android.app.Application
import org.koin.core.component.inject
import org.mathieu.domain.models.location.Location
import org.mathieu.domain.repositories.CharacterRepository
import org.mathieu.domain.repositories.LocationRepository
import org.mathieu.ui.ViewModel


class CharacterDetailsViewModel(application: Application) : org.mathieu.ui.ViewModel<CharacterDetailsState>(
    CharacterDetailsState(), application) {

    private val characterRepository: CharacterRepository by inject()
    private val locationRepository: LocationRepository by inject()

    fun init(characterId: Int) {
        fetchData(
            source = { characterRepository.getCharacter(id = characterId) }
        ) {

            onSuccess { character ->
                updateState {
                    copy(
                        avatarUrl = character.avatarUrl,
                        name = character.name,
                        error = null
                    )
                }

                // Fetch location data
                fetchLocation(character.location.second)
            }

            onFailure {
                updateState { copy(error = it.toString()) }
            }

            updateState { copy(isLoading = false) }
        }
    }

    private fun fetchLocation(locationId: Int) {
        fetchData(
            source = { locationRepository.getLocation(locationId) }
        ) {

            onSuccess { location ->
                updateState {
                    copy(
                        location = location
                    )
                }
            }

            onFailure {
                updateState { copy(error = it.toString()) }
            }
        }
    }


}


data class CharacterDetailsState(
    val isLoading: Boolean = true,
    val avatarUrl: String = "",
    val name: String = "",
    val location: Location? = null,
    val error: String? = null
)