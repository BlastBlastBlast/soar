package no.uio.ifi.in2000.met2025.data.models

//data class IsobaricData(
//    val updatedAt: String = "",
//    val timeSeries: List<IsobaricDataItem>
//)

data class IsobaricDataItem(
    val time: String,
    val valuesAtLayer: Map<Int, IsobaricDataValues> // key is pressure in hPa
)

data class IsobaricDataValues(
    val altitude: Double,
    val windSpeed: Double,
    val windFromDirection: Double
)