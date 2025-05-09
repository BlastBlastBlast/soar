package no.uio.ifi.in2000.met2025

import io.ktor.client.HttpClient
import junit.framework.Assert.assertEquals
import junit.framework.Assert.assertTrue
import io.ktor.client.engine.mock.*
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import no.uio.ifi.in2000.met2025.data.remote.sunrise.SunriseRepository
import no.uio.ifi.in2000.met2025.ui.screens.weathercardscreen.WeatherViewModel
import org.junit.Test
import no.uio.ifi.in2000.met2025.data.local.configprofiles.ConfigProfileRepository
import no.uio.ifi.in2000.met2025.data.remote.forecast.LocationForecastDataSource
import no.uio.ifi.in2000.met2025.data.remote.sunrise.SunriseDataSource
import no.uio.ifi.in2000.met2025.data.local.launchsites.LaunchSitesRepository
import no.uio.ifi.in2000.met2025.data.remote.forecast.LocationForecastRepository
import no.uio.ifi.in2000.met2025.data.remote.isobaric.IsobaricDataSource
import no.uio.ifi.in2000.met2025.data.remote.isobaric.IsobaricRepository
import no.uio.ifi.in2000.met2025.domain.WeatherModel
import no.uio.ifi.in2000.met2025.fakes.FakeConfigProfileDao
import no.uio.ifi.in2000.met2025.fakes.FakeGribDataDAO
import no.uio.ifi.in2000.met2025.fakes.FakeGribUpdatedDAO
import no.uio.ifi.in2000.met2025.fakes.FakeLaunchSiteDAO
import no.uio.ifi.in2000.met2025.helpers.exampleJson
import org.junit.Before

class WeatherViewModelTest {

    private lateinit var viewModel: WeatherViewModel

    @Before
    fun setup() {
        val mockClient = createTestHttpClient()

        val isobaricDataSource = IsobaricDataSource(mockClient, mockClient)

        val sunriseRepository = SunriseRepository(SunriseDataSource(mockClient))
        val locationForecastRepository = LocationForecastRepository(LocationForecastDataSource(mockClient))

        val configProfileRepository = ConfigProfileRepository(FakeConfigProfileDao())
        val launchSitesRepository = LaunchSitesRepository(FakeLaunchSiteDAO())
        val isobaricRepository = IsobaricRepository(
            isobaricDataSource,
            FakeGribDataDAO(),
            FakeGribUpdatedDAO()
        )

        val weatherModel = WeatherModel(locationForecastRepository, isobaricRepository)

        viewModel = WeatherViewModel(
            locationForecastRepository,
            configProfileRepository,
            launchSitesRepository,
            weatherModel,
            sunriseRepository
        )
    }

    @Test
    fun getValidSunTimesMapTest() = runBlocking {
        viewModel.getValidSunTimesList(59.942, 10.726)

        val sunTimesMap = viewModel.validSunTimesMap

        assertTrue(sunTimesMap.isNotEmpty())
        assertEquals(4, sunTimesMap.size)
        sunTimesMap.keys.forEach { key ->
            assertTrue(key.contains("59.942"))
            assertTrue(key.contains("10.726"))
        }
    }
}

// --- Helper to create a fake HttpClient ---
private fun createTestHttpClient(): HttpClient {
    return HttpClient(MockEngine) {
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
            json(Json { ignoreUnknownKeys = true })
        }
    }
}
