package no.uio.ifi.in2000.met2025.data.remote.sunrise

import no.uio.ifi.in2000.met2025.data.models.sunrise.SunriseResponse
import no.uio.ifi.in2000.met2025.data.models.sunrise.ValidSunTimes
import no.uio.ifi.in2000.met2025.domain.helpers.formatter
import java.time.Duration
import java.time.OffsetDateTime
import javax.inject.Inject

/**
 * SunriseRepository
 *
 * Retrieves sunrise and sunset times for a given date and location,
 * caching raw API responses to avoid redundant network calls, and
 * provides a convenience API that returns validated and adjusted instants.
 *
 * Special notes:
 * - Caches by date string in memory.
 * - Parses ISO-8601 offset timestamps into Instant and applies ±1 hour margins.
 */

class SunriseRepository @Inject constructor(
    private val sunriseDataSource: SunriseDataSource
) {
    // Simple in-memory cache mapping date strings to API responses
    val cache = mutableMapOf<String, SunriseResponse>()

    /**
     * Fetches raw sunrise response from the data source and stores it in the cache.
     */
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

    /**
     * Returns sunrise and sunset instants adjusted with a one-hour safety margin.
     *
     * - Attempts to retrieve from cache; if missing, fetches and caches.
     * - Parses the API’s offset strings into OffsetDateTime via the shared formatter.
     * - Converts to Instant and then adds/subtracts one hour.
     *
     * @return ValidSunTimes containing:
     *   - actual sunrise/set instants
     *   - one hour after sunrise instant
     *   - one hour before sunset instant
     */
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
