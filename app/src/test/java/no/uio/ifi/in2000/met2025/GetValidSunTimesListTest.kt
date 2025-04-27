package no.uio.ifi.in2000.met2025

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
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
import no.uio.ifi.in2000.met2025.ui.screens.weathercardscreen.WeatherCardViewmodel
import org.junit.Test
import no.uio.ifi.in2000.met2025.data.local.configprofiles.ConfigProfileRepository
import no.uio.ifi.in2000.met2025.data.remote.forecast.LocationForecastDataSource
import no.uio.ifi.in2000.met2025.data.remote.sunrise.SunriseDataSource
import no.uio.ifi.in2000.met2025.data.local.database.AppDatabase
import no.uio.ifi.in2000.met2025.data.local.launchsites.LaunchSitesRepository
import no.uio.ifi.in2000.met2025.data.remote.forecast.LocationForecastRepository
import no.uio.ifi.in2000.met2025.data.remote.isobaric.IsobaricDataSource
import no.uio.ifi.in2000.met2025.data.remote.isobaric.IsobaricRepository
import no.uio.ifi.in2000.met2025.domain.WeatherModel
import org.junit.After
import org.junit.Before

class WeatherCardViewmodelTest {

    private lateinit var viewModel: WeatherCardViewmodel
    private lateinit var database: AppDatabase

    @Before
    fun setup() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            AppDatabase::class.java
        )
            .allowMainThreadQueries()
            .build()

        val mockClient = createTestHttpClient()

        val sunriseRepository = SunriseRepository(SunriseDataSource(mockClient))
        val locationForecastRepository = LocationForecastRepository(LocationForecastDataSource(mockClient))
        val configProfileRepository = ConfigProfileRepository(database.configProfileDao())
        val launchSitesRepository = LaunchSitesRepository(database.launchSiteDao())
        val isobaricRepository = IsobaricRepository(
            IsobaricDataSource(mockClient, mockClient),
            database.gribDataDao(),
            database.gribUpdatedDao()
        )

        val weatherModel = WeatherModel(locationForecastRepository, isobaricRepository)

        viewModel = WeatherCardViewmodel(
            locationForecastRepository,
            configProfileRepository,
            launchSitesRepository,
            weatherModel,
            sunriseRepository
        )
    }

    @After
    fun teardown() {
        database.close()
    }

    @Test
    fun getValidSunTimesMapTest() = runBlocking {
        viewModel.getValidSunTimesList(59.942, 10.726)

        val sunTimesMap = viewModel.validSunTimesMap

        assertTrue(sunTimesMap.isNotEmpty())
        assertEquals(4, sunTimesMap.size)
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
