package no.uio.ifi.in2000.met2025

import SunriseResponse
import io.ktor.client.*
import io.ktor.client.call.body
import io.ktor.client.engine.mock.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.client.request.*
import io.ktor.http.*
import kotlinx.coroutines.runBlocking
import no.uio.ifi.in2000.met2025.data.remote.sunrise.SunriseDataSource
import org.junit.Test
import kotlin.test.assertEquals

class SunriseDataSourceTest {

    @Test
    fun sunriseApiParsingTest() = runBlocking {
        //Mock-client setup
        val mockClient = createSunriseMockClientWithJson()
        val dataSource = SunriseDataSource(mockClient)
        val response = dataSource.fetchSunriseData(59.9,10.7,"2025-04-25")

        val sunriseTime = response.fold(
                onSuccess = {
                    it.properties.sunrise.time
                },
                onFailure = {
                    "Error"
                }
        )

        assertEquals("2025-04-25T04:31+01:00", sunriseTime, "Sunrise time should match the mock data")
    }
}
