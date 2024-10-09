package org.mathieu.characters.details

import android.app.Application
import org.koin.core.component.inject
import org.mathieu.domain.models.location.LocationPreview
import org.mathieu.domain.repositories.CharacterRepository
import org.mathieu.ui.Destination
import org.mathieu.ui.ViewModel


sealed interface CharacterDetailsAction{
    data class GoToLocationDetails(val locationId: Int) : CharacterDetailsAction
}

class CharacterDetailsViewModel(application: Application) : ViewModel<CharacterDetailsState>(
    CharacterDetailsState(), application) {

    private val characterRepository: CharacterRepository by inject()

    fun init(characterId: Int) {
        fetchData(
            source = { characterRepository.getCharacter(id = characterId) }
        ) {

            onSuccess { character ->
                updateState {
                    copy(
                        avatarUrl = character.avatarUrl,
                        name = character.name,
                        locationPreview = character.locationPreview,
                        error = null
                    )
                }
            }

            onFailure {
                updateState { copy(error = it.toString()) }
            }

            updateState { copy(isLoading = false) }
        }
    }

    fun handleAction(action: CharacterDetailsAction) {
        when (action){
            is CharacterDetailsAction.GoToLocationDetails -> {
                sendEvent(
                    Destination.LocationDetails(
                        locationId = action.locationId.toString()
                    )
                )
            }
        }
    }
}

data class CharacterDetailsState(
    val isLoading: Boolean = true,
    val avatarUrl: String = "",
    val name: String = "",
    val locationPreview: LocationPreview? = null,
    val error: String? = null
)