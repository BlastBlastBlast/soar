package no.uio.ifi.in2000.met2025.data.models.locationforecast.apiResponse

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Container for forecast metadata and the actual time series.
 *
 * @property meta       feed metadata (update time, units)
 * @property timeSeries ordered list of forecast entries
 */
@Serializable
data class Properties(
    val meta: Meta,
    @SerialName("timeseries")               val timeSeries: List<TimeSeries>
)