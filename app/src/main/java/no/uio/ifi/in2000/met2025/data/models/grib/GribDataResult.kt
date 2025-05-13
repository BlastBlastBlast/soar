package no.uio.ifi.in2000.met2025.data.models.grib

/**
 * GribDataResult is a sealed class representing the result of fetching GRIB data.
 * Each subclass represents a different possible outcome, that can be handled in
 * the UI accordingly.
 */
sealed class GribDataResult {
    data class Success(val gribDataMap: GribDataMap) : GribDataResult()
    object AvailabilityError : GribDataResult()
    object FetchingError : GribDataResult()
    object ParsingError : GribDataResult()
}