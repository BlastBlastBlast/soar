package no.uio.ifi.in2000.met2025.data.models

import kotlinx.serialization.Serializable

@Serializable
data class SunTime(
    val type: String,
    val geometry: Geometry,
    val `when`: TimeInterval,
    val properties: SunProperties
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
    val solarnoon: SolarEvent,
    val solarmidnight: SolarEvent
)

@Serializable
data class SunEvent(
    val time: String,
    val azimuth: Double?
)

@Serializable
data class SolarEvent(
    val time: String,
    val disc_centre_elevation: Double,
    val visible: Boolean
)
