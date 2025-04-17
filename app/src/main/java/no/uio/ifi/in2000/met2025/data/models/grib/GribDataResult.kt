package no.uio.ifi.in2000.met2025.data.models.grib

sealed class GribDataResult {
    data class Success(val gribDataMap: GribDataMap) : GribDataResult()
    object AvailabilityError : GribDataResult()
    object FetchingError : GribDataResult()
    object ParsingError : GribDataResult()
}