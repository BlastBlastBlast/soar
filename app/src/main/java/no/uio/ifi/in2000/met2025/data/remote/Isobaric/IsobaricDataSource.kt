package no.uio.ifi.in2000.met2025.data.remote.Isobaric

import io.ktor.client.HttpClient
import javax.inject.Inject

class IsobaricDataSource @Inject constructor(val httpClient: HttpClient) { //TODO: Spesifiser GRIB client
    fun getIsobaricgrib() {
        // This is a dummy function
    }
}