package no.uio.ifi.in2000.met2025.data.models.locationforecast.apiResponse

import kotlinx.serialization.Serializable

/**
 * A single forecast entry in the time series.
 *
 * @property time ISO-8601 UTC timestamp for this entry
 * @property data forecast data at this instant and next hour
 */
@Serializable
data class TimeSeries(
    val time: String,
    val data: Data
)