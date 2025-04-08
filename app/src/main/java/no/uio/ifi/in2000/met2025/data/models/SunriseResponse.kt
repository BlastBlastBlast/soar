package no.uio.ifi.in2000.met2025.data.models

import kotlinx.serialization.Serializable

@Serializable
data class SunriseResponse(
    val location: Location
)

@Serializable
data class Location(
    val time: List<SunTime>
)

@Serializable
data class SunTime(
    val date: String,
    val sunrise: TimePoint? = null,
    val sunset: TimePoint? = null,
    val solarnoon: TimePoint? = null,
    val solarmidnight: TimePoint? = null
)

@Serializable
data class TimePoint(
    val time: String,
    val description: String
)
