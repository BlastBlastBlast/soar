package no.uio.ifi.in2000.met2025.data.models.locationforecast.apiResponse

import kotlinx.serialization.Serializable

/**
 * Instantaneous forecast details.
 *
 * @property details detailed meteorological measurements at this instant
 */
@Serializable
data class Instant(
    val details: Details
)