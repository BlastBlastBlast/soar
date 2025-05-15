package no.uio.ifi.in2000.met2025
/*
import kotlinx.coroutines.runBlocking
import no.uio.ifi.in2000.met2025.data.models.*
import no.uio.ifi.in2000.met2025.data.remote.forecast.LocationForecastDataSource
import no.uio.ifi.in2000.met2025.data.remote.forecast.LocationForecastRepository
import org.junit.Test
import java.time.Instant

class LocationForecastRepositoryTest {

    // En dummy datasource som returnerer forhåndsdefinert værdata
    private val fakeDataSource = object : LocationForecastDataSource {
        override suspend fun fetchForecastDataResponse(lat: Double, lon: Double): Result<ForecastDataResponse> {
            val now = Instant.now().toString()
            return Result.success(
                ForecastDataResponse(
                    type = "Feature",
                    geometry = Geometry("Point", listOf(lon, lat, 15.0)),
                    properties = Properties(
                        meta = Meta(updatedAt = now, units = dummyUnits()),
                        timeSeries = listOf(
                            TimeSeries(
                                time = now,
                                data = Data(
                                    instant = Instant(details = Details(
                                        airTemperature = 10.0,
                                        relativeHumidity = 80.0,
                                        windSpeed = 5.0,
                                        windSpeedOfGust = 7.0,
                                        windFromDirection = 200.0,
                                        fogAreaFraction = 10.0,
                                        dewPointTemperature = 5.0,
                                        cloudAreaFraction = 60.0
                                    )),
                                    next1Hours = NextHours(
                                        details = NextHoursDetails(
                                            precipitationAmount = 1.2,
                                            probabilityOfThunder = 0.0
                                        )
                                    )
                                )
                            )
                        )
                    )
                )
            )
        }
    }

    private fun dummyUnits(): Units = Units(
        airTemperature = "C",
        relativeHumidity = "%",
        windSpeed = "m/s",
        windSpeedOfGust = "m/s",
        windFromDirection = "deg",
        precipitationAmount = "mm",
        fogAreaFraction = "%",
        dewPointTemperature = "C",
        cloudAreaFraction = "%",
        probabilityOfThunder = "%"
    )

    @Test
    fun `getForecastData returns correct data`() = runBlocking {
        val repo = LocationForecastRepository(fakeDataSource)

        val result = repo.getForecastData(59.0, 10.0, timeSpanInHours = 1)

        assert(result.isSuccess)
        val forecastData = result.getOrNull()
        assert(forecastData != null)
        assert(forecastData!!.timeSeries.size == 1)
        val item = forecastData.timeSeries.first()
        assert(item.values.airTemperature == 10.0)
        assert(item.values.precipitationAmount == 1.2)
        assert(item.values.cloudAreaFraction == 60.0)
    }
}
*/