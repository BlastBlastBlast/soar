package no.uio.ifi.in2000.met2025.data.models.locationforecast.apiResponse

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Unit labels for each forecast parameter.
 *
 * @property airPressureAtSeaLevel   in hPa
 * @property airTemperature          in °C
 * @property relativeHumidity        unit for humidity in %
 * @property windSpeed               in m/s
 * @property windSpeedOfGust         in m/s, or "not_included"
 * @property windFromDirection       in degrees
 * @property precipitationAmount     in mm, or "not_included"
 * @property fogAreaFraction         in %, or "not_included"
 * @property dewPointTemperature     in °C, or "not_included"
 * @property cloudAreaFraction       unit for total cloud cover in %
 * @property cloudAreaFractionHigh   unit for high-level cloud cover in %
 * @property cloudAreaFractionLow    unit for low-level cloud cover in %
 * @property cloudAreaFractionMedium unit for medium-level cloud cover in %
 * @property probabilityOfThunder    in %, or "not_included"
 */
@Serializable
data class Units(
    @SerialName("air_pressure_at_sea_level")val airPressureAtSeaLevel: String,
    @SerialName("air_temperature")          val airTemperature: String,
    @SerialName("relative_humidity")        val relativeHumidity: String,
    @SerialName("wind_speed")               val windSpeed: String,
    @SerialName("wind_speed_of_gust")       val windSpeedOfGust: String = "not_included",
    @SerialName("wind_from_direction")      val windFromDirection: String,
    @SerialName("precipitation_amount")     val precipitationAmount: String = "not_included",
    @SerialName("fog_area_fraction")        val fogAreaFraction: String = "not_included",
    @SerialName("dew_point_temperature")    val dewPointTemperature: String = "not_included",
    @SerialName("cloud_area_fraction")      val cloudAreaFraction: String,
    @SerialName("cloud_area_fraction_high") val cloudAreaFractionHigh: String,
    @SerialName("cloud_area_fraction_low")  val cloudAreaFractionLow: String,
    @SerialName("cloud_area_fraction_medium") val cloudAreaFractionMedium: String,
    @SerialName("probability_of_thunder")   val probabilityOfThunder: String = "not_included"
)