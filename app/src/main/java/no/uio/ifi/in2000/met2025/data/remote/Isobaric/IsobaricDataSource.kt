package no.uio.ifi.in2000.met2025.data.remote.Isobaric

import android.os.Build
import androidx.annotation.RequiresApi
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.url
import java.math.RoundingMode
import java.time.Instant
import javax.inject.Inject
import javax.inject.Named

class IsobaricDataSource @Inject constructor(
    @Named("gribClient") private val httpClient: HttpClient
) { //TODO: Spesifiser GRIB client
    //Order of operations:
    // - Hente "available data" som viser hvilke tidsrom det er data ute for
    // - Formulere

    private val dsUrl = "https://api.met.no/weatherapi/isobaricgrib/1.0/grib2?area=southern_norway"

    suspend fun fetchCurrentIsobaricgribData() : Result<ByteArray>{
        return try {
            Result.success(httpClient.get {
                url(dsUrl)
            }.body())
        } catch (e: Exception) {
            val errorMessage = "Error fetching grib data: ${e.message}"
            Result.failure(Exception(errorMessage))
        }
    }

    //TODO: Finn ut hvor i koden man skal legge logikk for henting av timestamp
    //timestamp bruker Zulu tid, og hentes i 3'er verdier (0, 3, 6, 9, 12, 15, 18, 21)
    //suspend fun fetchSpecificIsobaricgribData(timestamp : String) : ByteArray {
    //    return httpClient.get("$url&time=$timestamp").body<ByteArray>()
    //}
}