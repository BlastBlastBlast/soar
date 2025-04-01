package no.uio.ifi.in2000.met2025.data.models

data class IsobaricDataItem(
    val time: String,
    val valuesAtLayer: Map<Int, IsobaricDataValues> // key is pressure in hPa
)

data class IsobaricDataValues(
    val airTemperature: Double,
    val windSpeed: Double,
    val windFromDirection: Double
)
/*
data class IsobaricData(
    val updatedAt: String,
    val timeSeries: List<IsobaricDataItem>
)



 */