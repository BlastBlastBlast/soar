package no.uio.ifi.in2000.met2025.data.remote.isobaric

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.url
import no.uio.ifi.in2000.met2025.data.models.IsobaricAvailabilityResponse
import javax.inject.Inject
import javax.inject.Named

class IsobaricDataSource @Inject constructor(
    @Named("gribClient") private val gribClient: HttpClient,
    @Named("jsonClient") private val jsonClient: HttpClient
) {
    private val gribUrl = "https://api.met.no/weatherapi/isobaricgrib/1.0/grib2?area=southern_norway"
    private val availUrl = "https://api.met.no/weatherapi/isobaricgrib/1.0/available.json?type=grib2"

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

    suspend fun fetchAvailabilityData(): Result<IsobaricAvailabilityResponse> {
        return try {
            Result.success(jsonClient.get {
                url(availUrl)
            }.body())
        } catch (e: Exception) {
            val errorMessage = "Error fetching grib availability data: ${e.message}"
            Result.failure(Exception(errorMessage))
        }
    }

    //TODO: Finn ut hvor i koden man skal legge logikk for henting av timestamp
    //timestamp bruker Zulu tid, og hentes i 3'er verdier (0, 3, 6, 9, 12, 15, 18, 21)
    //suspend fun fetchSpecificIsobaricgribData(timestamp : String) : ByteArray {
    //    return httpClient.get("$url&time=$timestamp").body<ByteArray>()
    //}
}