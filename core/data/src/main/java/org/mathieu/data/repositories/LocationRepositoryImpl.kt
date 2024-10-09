package org.mathieu.data.repositories

import org.mathieu.data.remote.LocationApi
import org.mathieu.data.remote.responses.toModel
import org.mathieu.domain.models.location.Location
import org.mathieu.domain.repositories.LocationRepository

internal class LocationRepositoryImpl(
    private val locationApi: LocationApi
) : LocationRepository {

    /**
     * Fetches the details of a location and the characters residing in that location.
     *
     * @param id The unique identifier of the location to retrieve.
     * @return A [Location] object containing details of the location and its residents.
     */
    override suspend fun getLocation(id: Int): Location {
        val locationResponse = locationApi.getLocation(id)
            ?: throw Exception("Location not found.")

        val residents = locationResponse.residents.mapNotNull { residentUrl ->
            locationApi.getCharacterOfLocation(residentUrl)?.toModel()
        }

        return locationResponse.toModel(residents)
    }
}