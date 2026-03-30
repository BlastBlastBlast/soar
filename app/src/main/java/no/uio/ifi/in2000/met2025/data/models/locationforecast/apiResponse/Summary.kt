package no.uio.ifi.in2000.met2025.data.models.locationforecast.apiResponse

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Weather symbol summary for the next hour.
 *
 * @property symbolCode code identifying the weather icon (e.g. "partlycloudy_day")
 */
@Serializable
data class Summary(
    @SerialName("symbol_code") val symbolCode: String
)