package no.uio.ifi.in2000.met2025.data.remote.sunrise

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import no.uio.ifi.in2000.met2025.data.models.sunrise.SunriseResponse

import javax.inject.Inject

/**
 * SunriseDataSource
 *
 * Responsible for fetching sunrise and sunset times from the MET Norway Sunrise API.
 *
 * Special notes:
 * - Constructs the request URL with latitude, longitude, date, and time offset.
 * - Returns a Result wrapping the parsed SunriseResponse or an exception on failure.
 */

class SunriseDataSource @Inject constructor(
    private val client: HttpClient
) {
    suspend fun fetchSunriseData(
        lat: Double,
        lon: Double,
        date: String,
        offset: String = "+01:00"
    ): Result<SunriseResponse> {
        val url = "https://in2000.api.met.no/weatherapi/sunrise/3.0/sun?lat=$lat&lon=$lon&date=$date&offset=$offset"
        return try {
            val response = client.get(url)
            Result.success(response.body())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}