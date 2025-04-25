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

//Sample of the sunrise api JSON response
private val exampleJson = """
{
  "copyright": "MET Norway",
  "licenseURL": "https://api.met.no/license_data.html",
  "type": "Feature",
  "geometry": {
    "type": "Point",
    "coordinates": [10.7, 59.9]
  },
  "when": {
    "interval": [
      "2025-04-24T23:15:00Z",
      "2025-04-25T23:17:00Z"
    ]
  },
  "properties": {
    "body": "Sun",
    "sunrise": {
      "time": "2025-04-25T04:31+01:00",
      "azimuth": 61.19
    },
    "sunset": {
      "time": "2025-04-25T20:00+01:00",
      "azimuth": 299.29
    },
    "solarnoon": {
      "time": "2025-04-25T12:15+01:00",
      "disc_centre_elevation": 43.45,
      "visible": true
    },
    "solarmidnight": {
      "time": "2025-04-25T00:15+01:00",
      "disc_centre_elevation": -16.91,
      "visible": false
    }
  }
}
""".trimIndent()

class SunriseApiTest {

    @Test
    fun testSunriseApiParsing() = runBlocking {
        //Mock-client setup
        val mockClient = HttpClient(MockEngine) {
            engine {
                addHandler { request ->
                    respond(
                        content = exampleJson,
                        status = HttpStatusCode.OK,
                        headers = headersOf(HttpHeaders.ContentType, "application/json")
                    )
                }
            }
            install(ContentNegotiation) {
                json()
            }
        }

        val response: SunriseResponse = mockClient.get("https://api.met.no/weatherapi/sunrise/3.0/sun?lat=59.9&lon=10.7&date=2025-04-25").body()

        val sunriseTime = response.properties.sunrise.time
        assertEquals("2025-04-25T04:31+01:00", sunriseTime, "Sunrise time should match the mock data")
    }
}
