package no.uio.ifi.in2000.met2025.data.remote.sunrise

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import no.uio.ifi.in2000.met2025.data.models.SunTime


import javax.inject.Inject

class SunriseDataSource @Inject constructor(
    private val client: HttpClient
) {
    suspend fun fetchSunriseData(
        lat: Double,
        lon: Double,
        date: String,
        offset: String = "+00:00"
    ): Result<SunTime> {
        val url = "https://api.met.no/weatherapi/sunrise/3.0/sun?lat=$lat&lon=$lon&date=$date&offset=$offset"
        return try {
            val response = client.get(url)
            Result.success(response.body())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}