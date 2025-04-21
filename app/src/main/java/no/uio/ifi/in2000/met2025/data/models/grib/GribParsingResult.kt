package no.uio.ifi.in2000.met2025.data.models.grib

sealed class GribParsingResult {
    data class Success(val gribDataMap: GribDataMap) : GribParsingResult()
    object Error : GribParsingResult()
}