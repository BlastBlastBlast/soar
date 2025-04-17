package no.uio.ifi.in2000.met2025.data.models.isobaric

sealed class IsobaricDataResult {
    data class Success(val isobaricData: Result<IsobaricData>) : IsobaricDataResult()
    object GribAvailabilityError : IsobaricDataResult()
    object GribFetchingError : IsobaricDataResult()
    object LocationForecastFetchingError: IsobaricDataResult()
    object OutOfBoundsError: IsobaricDataResult()
    object DataParsingError: IsobaricDataResult()
}