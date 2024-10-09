package org.mathieu.data.repositories

import org.mathieu.data.remote.LocationApi
import org.mathieu.data.remote.responses.toModel
import org.mathieu.domain.models.location.Location
import org.mathieu.domain.repositories.LocationRepository

internal class LocationRepositoryImpl(
    private val locationApi: LocationApi
) : LocationRepository {

    override suspend fun getLocation(id: Int): Location {
        return locationApi.getLocation(id)?.toModel()
            ?: throw Exception("Location not found.")
    }
}