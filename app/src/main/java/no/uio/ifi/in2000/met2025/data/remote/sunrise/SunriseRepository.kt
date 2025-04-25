package no.uio.ifi.in2000.met2025.data.remote.sunrise

import SunriseResponse
import javax.inject.Inject

class SunriseRepository @Inject constructor(
    private val sunriseDataSource: SunriseDataSource
) {
    /*
     * Hent soloppgangsdata for angitt dato og koordinater.
     */
    val cache = mutableMapOf<String, SunriseResponse>()

    suspend fun getSunTimes(
        lat: Double,
        lon: Double,
        date: String,
        offset: String = "+00:00"
    ): Result<SunriseResponse> {
        val res = sunriseDataSource.fetchSunriseData(lat, lon, date, offset)
        if (res.isSuccess) {
            cache[date] = res.getOrThrow()
        }
        return res
    }



}
