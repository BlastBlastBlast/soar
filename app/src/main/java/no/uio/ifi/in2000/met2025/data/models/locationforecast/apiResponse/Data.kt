package no.uio.ifi.in2000.met2025.data.models.locationforecast.apiResponse

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Contains instant-and-hourly forecast details.
 *
 * @property instant    measurements at the exact timestamp
 * @property next1Hours summary for the following hour, if available
 */
@Serializable
data class Data(
    val instant: Instant,
    @SerialName("next_1_hours")             val next1Hours: NextHours? = null
)