package no.uio.ifi.in2000.met2025.data.models

//data class IsobaricData(
//    val updatedAt: String = "",
//    val timeSeries: List<IsobaricDataItem>
//)

data class IsobaricData(
    //val updatedAt: String, TODO: Add support for this value, could be displayed when failing to load most recent data
    val time: String,
    val valuesAtLayer: Map<Int, IsobaricDataValues> // key is pressure in hPa
)

data class IsobaricDataValues(
    val altitude: Double,
    val windSpeed: Double,
    val windFromDirection: Double
)