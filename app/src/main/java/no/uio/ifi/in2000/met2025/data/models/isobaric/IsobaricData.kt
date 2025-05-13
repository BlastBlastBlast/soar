package no.uio.ifi.in2000.met2025.data.models.isobaric

/**
 * IsobaricData
 * A set of calculated IsobaricData values for a specific time.
 */
data class IsobaricData(
    val time: String,
    val valuesAtLayer: Map<Int, IsobaricDataValues>
)

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