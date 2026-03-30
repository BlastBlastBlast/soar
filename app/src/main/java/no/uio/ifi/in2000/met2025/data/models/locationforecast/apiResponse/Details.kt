package no.uio.ifi.in2000.met2025.data.models.locationforecast.apiResponse

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Detailed meteorological measurements.
 *
 * @property airPressureAtSeaLevel   in hPa
 * @property airTemperature          in °C
 * @property relativeHumidity        humidity in %
 * @property windSpeed               in m/s
 * @property windSpeedOfGust         in m/s, if reported
 * @property windFromDirection       in degrees
 * @property fogAreaFraction         in %, if reported
 * @property dewPointTemperature     in °C, if reported
 * @property cloudAreaFraction       total cloud cover in %
 * @property cloudAreaFractionHigh   high-level cloud cover in %
 * @property cloudAreaFractionLow    low-level cloud cover in %
 * @property cloudAreaFractionMedium medium-level cloud cover in %
 */
@Serializable
data class Details(
    @SerialName("air_pressure_at_sea_level") val airPressureAtSeaLevel: Double,
    @SerialName("air_temperature")          val airTemperature: Double,
    @SerialName("relative_humidity")        val relativeHumidity: Double,
    @SerialName("wind_speed")               val windSpeed: Double,
    @SerialName("wind_speed_of_gust")       val windSpeedOfGust: Double? = null,
    @SerialName("wind_from_direction")      val windFromDirection: Double,
    @SerialName("fog_area_fraction")        val fogAreaFraction: Double? = null,
    @SerialName("dew_point_temperature")    val dewPointTemperature: Double? = null,
    @SerialName("cloud_area_fraction")      val cloudAreaFraction: Double,
    @SerialName("cloud_area_fraction_high") val cloudAreaFractionHigh: Double,
    @SerialName("cloud_area_fraction_low")  val cloudAreaFractionLow: Double,
    @SerialName("cloud_area_fraction_medium") val cloudAreaFractionMedium: Double
)