package no.uio.ifi.in2000.met2025.data.remote.isobaric

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.url
import no.uio.ifi.in2000.met2025.data.models.grib.DataEntry
import no.uio.ifi.in2000.met2025.data.models.grib.GribAvailabilityResponse
import javax.inject.Inject
import javax.inject.Named

class IsobaricDataSource @Inject constructor(
    @Named("gribClient") private val gribClient: HttpClient,
    @Named("jsonClient") private val jsonClient: HttpClient
) {
    private val availUrl = "https://in2000.api.met.no/weatherapi/isobaricgrib/1.0/available.json?type=grib2"

    suspend fun fetchIsobaricGribData(uri: String): Result<ByteArray> {
        return try {
            Result.success(gribClient.get {
                url(uri)
            }.body())
        } catch (e: Exception) {
            val errorMessage = "Error fetching grib data: ${e.message}"
            Result.failure(Exception(errorMessage))
        }
    }

    suspend fun fetchAvailabilityData(): Result<GribAvailabilityResponse> {
        return try {
            val response: List<DataEntry> = jsonClient.get {
                url(availUrl)
            }.body()
            Result.success(GribAvailabilityResponse(response))
        } catch (e: Exception) {
            val errorMessage = "Error fetching grib availability data: ${e.message}"
            Result.failure(Exception(errorMessage))
        }
    }
}