package no.uio.ifi.in2000.met2025.data.models.locationforecast

import kotlinx.serialization.SerialName
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

/**
 * Geographic point information.
 *
 * @property type        geometry type (always "Point")
 * @property coordinates list of [longitude, latitude, altitude]
 */
@Serializable
data class Geometry(
    val type: String,               // f.eks "Point"
    val coordinates: List<Double>   // [lon, lat, alt]
)

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

/**
 * Metadata about this forecast feed.
 *
 * @property updatedAt ISO-8601 UTC timestamp when the feed was last refreshed
 * @property units     mapping from parameter names to their unit symbols
 */
@Serializable
data class Meta(
    @SerialName("updated_at")               val updatedAt: String,  //f.eks "2025-03-15T16:26:43Z"
    val units: Units
)

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

/**
 * Instantaneous forecast details.
 *
 * @property details detailed meteorological measurements at this instant
 */
@Serializable
data class Instant(
    val details: Details
)


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

/**
 * Weather symbol summary for the next hour.
 *
 * @property symbolCode code identifying the weather icon (e.g. "partlycloudy_day")
 */
@Serializable
data class Summary(
    @SerialName("symbol_code") val symbolCode: String
)