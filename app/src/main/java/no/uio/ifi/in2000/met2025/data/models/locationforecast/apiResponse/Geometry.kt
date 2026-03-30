package no.uio.ifi.in2000.met2025.data.models.locationforecast.apiResponse

import kotlinx.serialization.Serializable

/**
 * Geographic point information.
 *
 * @property type        geometry type (always "Point")
 * @property coordinates list of [longitude, latitude, altitude]
 */
@Serializable
data class Geometry(
    val type: String,               // f.eks "Point"
    val coordinates: List<Double>   // [lon, lat, alt]
)


