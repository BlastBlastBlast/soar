package no.uio.ifi.in2000.met2025.data.models.sunrise

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * SunriseResponse
 *
 * Wrapper for the MET Sun events API response.
 *
 * Special notes:
 * - The JSON field name `when` is a keyword in Kotlin, so it's backticked.
 * - All timestamp strings are ISO-8601 with zone offsets.
 * - `no.uio.ifi.in2000.met2025.data.models.sunrise.SunEvent.azimuth` is optional for compatibility with responses that omit it.
 *
 * @property copyright   data provider attribution
 * @property licenseURL  URL to the data license terms
 * @property type        GeoJSON feature type (always "Feature")
 * @property geometry    location point for which sun events apply
 * @property when        interval defining the valid time window for the data
 * @property properties  detailed sun event times and solar elevations
 */
@Serializable
data class SunriseResponse(
    val copyright: String,
    val licenseURL: String,
    val type: String,
    val geometry: Geometry,
    val `when`: TimeInterval,
    val properties: SunProperties
)

/**
 * Geographic point definition in GeoJSON format.
 *
 * @property type         geometry type (always "Point")
 * @property coordinates  list of [longitude, latitude] (in decimal degrees)
 */
@Serializable
data class Geometry(
    val type: String,
    val coordinates: List<Double>
)

/**
 * TimeInterval
 *
 * Encapsulates the start and end of the data validity period.
 *
 * @property interval  two-element list: [startTime, endTime] as ISO-8601 strings
 */
@Serializable
data class TimeInterval(
    val interval: List<String>
)

/**
 * SunProperties
 *
 * Contains all relevant solar events for the day at the given point.
 *
 * @property body           celestial body name (e.g. "Sun")
 * @property sunrise        sunrise event with time and optional azimuth
 * @property sunset         sunset event with time and optional azimuth
 * @property solarnoon      solar noon event, includes sun elevation and visibility
 * @property solarmidnight  solar midnight event, includes sun elevation and visibility
 */
@Serializable
data class SunProperties(
    val body: String,
    val sunrise: SunEvent,
    val sunset: SunEvent,
    val solarnoon: SolarPoint,
    val solarmidnight: SolarPoint
)

/**
 * SunEvent
 *
 * A single sun event occurrence (sunrise or sunset).
 *
 * @property time     event timestamp in ISO-8601 with offset (e.g. "2025-05-13T03:44+01:00")
 * @property azimuth  compass direction of sun at event, in degrees. nullable if not provided
 */
@Serializable
data class SunEvent(
    val time: String,
    val azimuth: Double? = null  // Made optional for compatibility
)

/**
 * SolarPoint
 *
 * A time point with sun’s elevation and visibility status.
 *
 * @property time                 timestamp in ISO-8601 with offset
 * @property discCentreElevation  sun’s elevation angle in degrees at disc center
 * @property visible              whether the sun is above the horizon (true = visible)
 */
@Serializable
data class SolarPoint(
    val time: String,
    @SerialName("disc_centre_elevation")
    val discCentreElevation: Double,
    val visible: Boolean
)