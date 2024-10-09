package org.mathieu.data.local.objects

import io.realm.kotlin.types.RealmObject
import io.realm.kotlin.types.annotations.PrimaryKey
import org.mathieu.data.remote.responses.LocationPreviewResponse
import org.mathieu.domain.models.location.LocationPreview

/**
 * Represents a location preview entity stored in the SQLite database. This object provides fields
 * necessary to represent all the attributes of a location preview from the data source.
 * The object is specifically tailored for SQLite storage using Realm.
 *
 * @property id Unique identifier of the character.
 * @property name Name of the location.
 * @property type The type or category of the location.
 * @property dimension The specific dimension or universe where this location exists.
 */
internal class LocationPreviewObject: RealmObject {
    @PrimaryKey
    var id: Int = -1
    var name: String = ""
    var type: String = ""
    var dimension: String = ""
}

internal fun LocationPreviewResponse.toRealmObject() = LocationPreviewObject().also { obj ->
    obj.id = this.id
    obj.name = this.name
    obj.type = this.type
    obj.dimension = this.dimension
}

internal fun LocationPreviewObject.toModel() = LocationPreview(
    id = this.id,
    name = this.name,
    type = this.type,
    dimension = this.dimension
)