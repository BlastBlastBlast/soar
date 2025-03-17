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
    @SerialName("air_temperature")          val airTemperature: String,
    @SerialName("relative_humidity")        val relativeHumidity: String,
    @SerialName("wind_speed")               val windSpeed: String,
    @SerialName("wind_speed_of_gust")       val windSpeedOfGust: String,
    @SerialName("wind_from_direction")      val windFromDirection: String,
    @SerialName("precipitation_amount")     val precipitationAmount: String,
    @SerialName("fog_area_fraction")        val fogAreaFraction: String,
    @SerialName("dew_point_temperature")    val dewPointTemperature: String,
    @SerialName("cloud_area_fraction")      val cloudAreaFraction: String,
    @SerialName("probability_of_thunder")   val probabilityOfThunder: String
)

@Serializable
data class TimeSeries(
    val time: String,
    val data: Data
)

@Serializable
data class Data(
    val instant: Instant
)

@Serializable
data class Instant(
    val details: Details
)

@Serializable
data class Details(
    @SerialName("air_temperature")          val airTemperature: Double,
    @SerialName("relative_humidity")        val relativeHumidity: Double,
    @SerialName("wind_speed")               val windSpeed: Double,
    @SerialName("wind_speed_of_gust")       val windSpeedOfGust: Double,
    @SerialName("wind_from_direction")      val windFromDirection: Double,
    @SerialName("precipitation_amount")     val precipitationAmount: Double,
    @SerialName("fog_area_fraction")        val fogAreaFraction: Double,
    @SerialName("dew_point_temperature")    val dewPointTemperature: Double,
    @SerialName("cloud_area_fraction")      val cloudAreaFraction: Double,
    @SerialName("probability_of_thunder")   val probabilityOfThunder: Double
)

