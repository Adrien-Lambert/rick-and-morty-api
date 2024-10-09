package org.mathieu.data.remote.responses

import kotlinx.serialization.Serializable
import org.mathieu.domain.models.location.LocationPreview

/**
 * Represents detailed information about a location preview, typically received from an API response.
 *
 * @property id Unique identifier of the character.
 * @property name Name of the location.
 * @property type The type or category of the location.
 * @property dimension The specific dimension or universe where this location exists.
 */
@Serializable
data class LocationPreviewResponse(
    val id: Int,
    val name: String,
    val type: String,
    val dimension: String,
    val residents: List<String>? = null
)

internal fun LocationPreviewResponse.toModel(): LocationPreview {
    return LocationPreview(
        id = id,
        name = name,
        type = type,
        dimension = dimension
    )
}