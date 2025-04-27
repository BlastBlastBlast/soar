package no.uio.ifi.in2000.met2025

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import kotlinx.coroutines.runBlocking
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
import kotlin.test.assertEquals

class WeatherCardViewmodelTest {

    @Test
    fun getValidSunTimesListTest() = runBlocking {
        val database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            AppDatabase::class.java
        )
            .allowMainThreadQueries()
            .build()

        try {
            val mockClient = createSunriseMockClientWithJson()

            val sunriseDataSource = SunriseDataSource(mockClient)
            val locationForecastDataSource = LocationForecastDataSource(mockClient)
            val configProfileDao = database.configProfileDao()
            val launchSitesDao = database.launchSiteDao()
            val isobaricDao = database.gribDataDao()
            val updatedDao = database.gribUpdatedDao()
            val isobaricDataSource = IsobaricDataSource(mockClient, mockClient)

            val sunriseRepository = SunriseRepository(sunriseDataSource)
            val locationForecastRepository = LocationForecastRepository(locationForecastDataSource)
            val configProfileRepository = ConfigProfileRepository(configProfileDao)
            val launchSitesRepository = LaunchSitesRepository(launchSitesDao)
            val isobaricRepository = IsobaricRepository(isobaricDataSource, isobaricDao, updatedDao)

            val weatherModel = WeatherModel(locationForecastRepository, isobaricRepository)

            val viewModel = WeatherCardViewmodel(
                locationForecastRepository = locationForecastRepository,
                configProfileRepository = configProfileRepository,
                launchSitesRepository = launchSitesRepository,
                weatherModel = weatherModel,
                sunriseRepository = sunriseRepository
            )

            val validSunTimesList = viewModel.getValidSunTimesList()

            assert(validSunTimesList.isNotEmpty())
            assertEquals(4, validSunTimesList.size)

        } finally {
            database.close() // always close even if test fails
        }
    }

}
