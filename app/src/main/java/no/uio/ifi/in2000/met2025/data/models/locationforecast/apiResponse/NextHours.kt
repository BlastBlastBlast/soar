package no.uio.ifi.in2000.met2025.data.models.locationforecast.apiResponse

import kotlinx.serialization.Serializable

/**
 * Forecast summary for the next hour.
 *
 * @property details   precipitation and thunder probability
 * @property summary   optional weather symbol code (e.g. "clearsky_day")
 */
@Serializable
data class NextHours(
    val details: NextHoursDetails,
    val summary: Summary? = null
)