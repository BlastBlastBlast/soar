package no.uio.ifi.in2000.met2025.data.models.isobaric

/**
 * IsobaricDataValues
 * A set of calculated IsobaricData values.
 * GRIB2 wind vectors are converted to wind speed and direction.
 * Pressure and temperature are used to calculate the altitude.
 */
data class IsobaricDataValues(
    val altitude: Double,
    val airTemperature: Double,
    val windSpeed: Double,
    val windFromDirection: Double
)
