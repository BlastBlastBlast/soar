package no.uio.ifi.in2000.met2025.data.models.locationforecast.apiResponse

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Numerical details for the next hour.
 *
 * @property precipitationAmount    in mm, if any
 * @property probabilityOfThunder   chance of thunder in %, if any
 */
@Serializable
data class NextHoursDetails(
    @SerialName("precipitation_amount")     val precipitationAmount: Double? = null,
    @SerialName("probability_of_thunder")   val probabilityOfThunder: Double? = null
)