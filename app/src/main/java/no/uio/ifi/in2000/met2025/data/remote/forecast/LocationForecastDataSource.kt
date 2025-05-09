package no.uio.ifi.in2000.met2025.data.remote.forecast

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.url
import no.uio.ifi.in2000.met2025.data.models.locationforecast.ForecastDataResponse
import java.math.RoundingMode
import javax.inject.Inject

class LocationForecastDataSource @Inject constructor(
    private val httpClient: HttpClient
) {
    suspend fun fetchForecastDataResponse(lat: Double, lon: Double): Result<ForecastDataResponse> {
        return try {
            // Format latitude and longitude to 2 decimal places
            val formattedLat = lat.toBigDecimal().setScale(2, RoundingMode.HALF_UP).toDouble()
            val formattedLon = lon.toBigDecimal().setScale(2, RoundingMode.HALF_UP).toDouble()
            val requestUrl = "https://in2000.api.met.no/weatherapi/locationforecast/2.0/complete?lat=$formattedLat&lon=$formattedLon"

            // Optional: set or override headers here if needed.
            Result.success(httpClient.get {
                url(requestUrl)
                // You could also specify the User-Agent header here if needed:
                header("User-Agent", "MyWeatherApp/1.0 your-email@example.com")
            }.body())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}