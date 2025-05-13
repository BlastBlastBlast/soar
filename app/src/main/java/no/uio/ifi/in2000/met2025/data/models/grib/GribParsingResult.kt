package no.uio.ifi.in2000.met2025.data.models.grib

/**
 * GribParsingResult is a sealed class representing the result of parsing GRIB data.
 * Each subclass represents a different possible outcome, that can be handled in
 * the UI accordingly.
 */
sealed class GribParsingResult {
    data class Success(val gribDataMap: GribDataMap) : GribParsingResult()
    object Error : GribParsingResult()
}