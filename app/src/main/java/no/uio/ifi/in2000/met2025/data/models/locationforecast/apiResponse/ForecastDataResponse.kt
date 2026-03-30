package no.uio.ifi.in2000.met2025.data.models.locationforecast.apiResponse

import kotlinx.serialization.Serializable

/**
 * Locationforecast API models
 *
 * Maps the JSON response from the MET Locationforecast endpoint into Kotlin types.
 *
 * Special notes:
 * - All timestamp fields are ISO-8601 in UTC.
 * - @SerialName is used when JSON keys don’t follow Kotlin naming conventions.
 * - Fields not present in some payloads (like next_1_hours) are nullable.
 */

/**
 * Top-level wrapper for the forecast response.
 *
 * @property type       JSON feature type (e.g. "Feature")
 * @property geometry   geographic point (longitude, latitude, altitude)
 * @property properties metadata and time series entries
 */
@Serializable
data class ForecastDataResponse(
    val type: String,               // f.eks "Feature"
    val geometry: Geometry,
    val properties: Properties
)


