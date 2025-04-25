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
import org.junit.Test
import kotlin.test.assertEquals

class SunriseApiTest {

    @Test
    fun testSunriseApiParsing() = runBlocking {
        //Mock-client setup
        val mockClient = createMockClientWithJson()

        val response: SunriseResponse = mockClient.get("https://api.met.no/weatherapi/sunrise/3.0/sun?lat=59.9&lon=10.7&date=2025-04-25").body()

        val sunriseTime = response.properties.sunrise.time
        assertEquals("2025-04-25T04:31+01:00", sunriseTime, "Sunrise time should match the mock data")
    }
}
