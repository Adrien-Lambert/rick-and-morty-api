package org.mathieu.data.local

import io.realm.kotlin.UpdatePolicy
import io.realm.kotlin.ext.query
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.mathieu.data.local.objects.LocationPreviewObject

internal class LocationPreviewLocal(private val database: RealmDatabase) {

    suspend fun getLocations(): Flow<List<LocationPreviewObject>> = database.use {
        query<LocationPreviewObject>().find().asFlow().map { it.list }
    }

    suspend fun getLocation(id: Int): LocationPreviewObject? = database.use {
        query<LocationPreviewObject>("id == $id").first().find()
    }

    suspend fun saveLocations(locations: List<LocationPreviewObject>) = locations.onEach {
        insert(it)
    }

    suspend fun insert(location: LocationPreviewObject) {
        database.write {
            copyToRealm(location, UpdatePolicy.ALL)
        }
    }
}