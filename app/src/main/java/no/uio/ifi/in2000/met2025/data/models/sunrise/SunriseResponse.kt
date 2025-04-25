import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class SunriseResponse(
    val copyright: String,
    val licenseURL: String,
    val type: String,
    val geometry: Geometry,
    val `when`: TimeInterval,
    val properties: SunProperties
)

@Serializable
data class Geometry(
    val type: String,
    val coordinates: List<Double>
)

@Serializable
data class TimeInterval(
    val interval: List<String>
)

@Serializable
data class SunProperties(
    val body: String,
    val sunrise: SunEvent,
    val sunset: SunEvent,
    val solarnoon: SolarPoint,
    val solarmidnight: SolarPoint
)

@Serializable
data class SunEvent(
    val time: String,
    val azimuth: Double? = null  // Made optional for compatibility
)

@Serializable
data class SolarPoint(
    val time: String,
    @SerialName("disc_centre_elevation")
    val discCentreElevation: Double,
    val visible: Boolean
)