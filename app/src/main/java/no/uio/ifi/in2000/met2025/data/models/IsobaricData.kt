package no.uio.ifi.in2000.met2025.data.models

//data class IsobaricData(
//    val updatedAt: String = "",
//    val timeSeries: List<IsobaricDataItem>
//)

sealed class IsobaricDataResult {
    data class Success(val isobaricData: Result<IsobaricData>) : IsobaricDataResult()
    object GribAvailabilityError : IsobaricDataResult()
    object GribFetchingError : IsobaricDataResult()
    object LocationForecastFetchingError: IsobaricDataResult()
}

data class IsobaricData(
    //val updatedAt: String, TODO: Add support for this value, could be displayed when failing to load most recent data
    val time: String,
    val valuesAtLayer: Map<Int, IsobaricDataValues> // key is pressure in hPa
)

data class IsobaricDataValues(
    val altitude: Double,
    val airTemperature: Double,
    val windSpeed: Double,
    val windFromDirection: Double
)