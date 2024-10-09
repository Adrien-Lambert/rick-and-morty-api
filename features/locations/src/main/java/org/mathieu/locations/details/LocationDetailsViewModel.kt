package org.mathieu.locations.details

import android.app.Application
import org.koin.core.component.inject
import org.mathieu.domain.models.character.Character
import org.mathieu.domain.repositories.LocationRepository
import org.mathieu.ui.ViewModel


class LocationDetailsViewModel(application: Application) : ViewModel<LocationDetailsState>(
    LocationDetailsState(), application) {

    private val locationRepository: LocationRepository by inject()

    fun init(locationId: Int) {
        fetchData(
            source = { locationRepository.getLocation(locationId) }
        ) {

            onSuccess { location ->
                updateState {
                    copy(
                        isLoading = false,
                        locationName = location.name,
                        locationType = location.type,
                        locationDimension = location.dimension,
                        residents = location.residents,
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

}


data class LocationDetailsState(
    val isLoading: Boolean = true,
    val locationName: String = "",
    val locationType: String = "",
    val locationDimension: String = "",
    val residents: List<Character> = emptyList(),
    val error: String? = null
)