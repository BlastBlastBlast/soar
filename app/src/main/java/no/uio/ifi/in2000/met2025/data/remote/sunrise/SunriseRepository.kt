package no.uio.ifi.in2000.met2025.data.remote.sunrise

import SunriseResponse
import no.uio.ifi.in2000.met2025.data.models.sunrise.ValidSunTimes
import no.uio.ifi.in2000.met2025.domain.helpers.formatter
import java.time.Duration
import java.time.OffsetDateTime
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

    suspend fun getValidSunTimes(
        lat: Double,
        lon: Double,
        date: String,
        offset: String = "+00:00"
    ) : ValidSunTimes {
        //checks cache for data, if not there it fetches and puts it in cache
        val fetchRes = cache.getOrPut(date) { getSunTimes(lat, lon, date, offset).getOrThrow() }

        val sunrise = fetchRes.properties.sunrise.time
        val sunset = fetchRes.properties.sunset.time
        val offsetSunrise = OffsetDateTime.parse(sunrise, formatter)
        val offsetSunset = OffsetDateTime.parse(sunset, formatter)
        val instantRise = offsetSunrise.toInstant()
        val instantSet = offsetSunset.toInstant()

        val result = ValidSunTimes(
            instantRise,
            instantSet,
            instantRise.plus(Duration.ofHours(1)),
            instantSet.minus(Duration.ofHours(1))
        )
        return result
    }

}
