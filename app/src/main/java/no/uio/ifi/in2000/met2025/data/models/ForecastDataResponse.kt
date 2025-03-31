package no.uio.ifi.in2000.met2025.data.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ForecastDataResponse(
    val type: String,               // f.eks "Feature"
    val geometry: Geometry,
    val properties: Properties
)

@Serializable
data class Geometry(
    val type: String,               // f.eks "Point"
    val coordinates: List<Double>   // [lon, lat, alt]
)

@Serializable
data class Properties(
    val meta: Meta,
    @SerialName("timeseries")               val timeSeries: List<TimeSeries>
)

@Serializable
data class Meta(
    @SerialName("updated_at")               val updatedAt: String,  //f.eks "2025-03-15T16:26:43Z"
    val units: Units
)

@Serializable
data class Units(
    @SerialName("air_pressure_at_sea_level")val airPressureAtSeaLevel: String,
    @SerialName("air_temperature")          val airTemperature: String,
    @SerialName("relative_humidity")        val relativeHumidity: String,
    @SerialName("wind_speed")               val windSpeed: String,
    @SerialName("wind_speed_of_gust")       val windSpeedOfGust: String = "not_included",
    @SerialName("wind_from_direction")      val windFromDirection: String,
    @SerialName("precipitation_amount")     val precipitationAmount: String,
    @SerialName("fog_area_fraction")        val fogAreaFraction: String = "not_included",
    @SerialName("dew_point_temperature")    val dewPointTemperature: String = "not_included",
    @SerialName("cloud_area_fraction")      val cloudAreaFraction: String,
    @SerialName("probability_of_thunder")   val probabilityOfThunder: String = "not_included"
)


@Serializable
data class TimeSeries(
    val time: String,
    val data: Data
)

@Serializable
data class Data(
    val instant: Instant,
    @SerialName("next_1_hours")             val next1Hours: NextHours? = null
)

@Serializable
data class Instant(
    val details: Details
)

@Serializable
data class Details(
    @SerialName("air_pressure_at_sea_level")val airPressureAtSeaLevel: Double,
    @SerialName("air_temperature")          val airTemperature: Double,
    @SerialName("relative_humidity")        val relativeHumidity: Double,
    @SerialName("wind_speed")               val windSpeed: Double,
    @SerialName("wind_speed_of_gust")       val windSpeedOfGust: Double = 0.0, // should probably not be 0.0 when not included, rather null
    @SerialName("wind_from_direction")      val windFromDirection: Double,
    @SerialName("fog_area_fraction")        val fogAreaFraction: Double = 0.0,
    @SerialName("dew_point_temperature")    val dewPointTemperature: Double = 0.0,
    @SerialName("cloud_area_fraction")      val cloudAreaFraction: Double
)

@Serializable
data class NextHours(
    val details: NextHoursDetails
)

@Serializable
data class NextHoursDetails(
    @SerialName("precipitation_amount")     val precipitationAmount: Double,
    @SerialName("probability_of_thunder")   val probabilityOfThunder: Double
)

