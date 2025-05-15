package no.uio.ifi.in2000.met2025.data.models.grib

/**
 * GribDataMap
 *
 * Represents a snapshot of GRIB data at a specific time, organized by geographic
 * coordinates and pressure levels.
 *
 * Special notes:
 * - `time` is an ISO-8601 timestamp in UTC.
 * - The outer map key is a latitude/longitude pair, always in .05 intervals (1.05, 1.15, etc.)
 * - The inner map key is a pressure level (in hPa).
 */
data class GribDataMap(
    val time : String,
    val map : Map<Pair<Double, Double>, Map<Int, GribVectors>>
)

/**
 * GribVectors are a set of three components of meteorological data.
 * A set of wind vectors that together can be used to calculate the
 * direction and speed of the wind, and a temperature value.
 */
data class GribVectors(
    val temperature: Float,
    val uComponentWind: Float,
    val vComponentWind: Float
)
