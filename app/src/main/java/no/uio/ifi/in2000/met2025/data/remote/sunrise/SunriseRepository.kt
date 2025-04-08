package no.uio.ifi.in2000.met2025.data.remote.sunrise

import no.uio.ifi.in2000.met2025.data.models.SunTime
import javax.inject.Inject

class SunriseRepository @Inject constructor(
    private val sunriseDataSource: SunriseDataSource
) {
    /**
     * Hent første soloppgangsdata for angitt dato og koordinater.
     * @param lat Breddegrad
     * @param lon Lengdegrad
     * @param date ISO 8601-dato
     * @param offset Tidssoneforskyvning (f.eks. "+02:00")
     */
    suspend fun getSunTimes(
        lat: Double,
        lon: Double,
        date: String,
        offset: String = "+00:00"
    ): Result<SunTime> {
        return sunriseDataSource.fetchSunriseData(lat, lon, date, offset)
            .mapCatching { response ->
                response.location.time.first()
            }
    }
}