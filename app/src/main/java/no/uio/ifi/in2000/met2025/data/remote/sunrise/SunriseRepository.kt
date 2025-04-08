package no.uio.ifi.in2000.met2025.data.remote.sunrise

import no.uio.ifi.in2000.met2025.data.models.SunTime
import javax.inject.Inject

class SunriseRepository @Inject constructor(
    private val sunriseDataSource: SunriseDataSource
) {
    /*
     * Hent soloppgangsdata for angitt dato og koordinater.
     */
    suspend fun getSunTimes(
        lat: Double,
        lon: Double,
        date: String,
        offset: String = "+00:00"
    ): Result<SunTime> {
        return sunriseDataSource.fetchSunriseData(lat, lon, date, offset)
    }

}
