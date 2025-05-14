package no.uio.ifi.in2000.met2025.data.models.isobaric

/**
 * IsobaricDataResult
 * Represents the result of fetching and calculating isobaric data.
 * Each subclass represents a different possible outcome, that can be handled in
 * the UI accordingly.
 */
sealed class IsobaricDataResult {
    data class Success(val isobaricData: Result<IsobaricData>) : IsobaricDataResult()
    object GribAvailabilityError : IsobaricDataResult()
    object GribFetchingError : IsobaricDataResult()
    object LocationForecastFetchingError: IsobaricDataResult()
    object OutOfBoundsError: IsobaricDataResult()
    object DataParsingError: IsobaricDataResult()
}