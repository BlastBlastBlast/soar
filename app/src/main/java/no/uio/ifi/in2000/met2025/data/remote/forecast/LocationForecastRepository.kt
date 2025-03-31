package no.uio.ifi.in2000.met2025.data.remote.forecast

import no.uio.ifi.in2000.met2025.data.models.ForecastData
import no.uio.ifi.in2000.met2025.data.models.ForecastDataItem
import no.uio.ifi.in2000.met2025.data.models.ForecastDataValues
import javax.inject.Inject
import javax.inject.Named

class LocationForecastRepository @Inject constructor(
    private val locationForecastDataSource: LocationForecastDataSource
) {
    suspend fun getForecastData(lat: Double, lon: Double, timeSpanInHours: Int): Result<ForecastData> {
        locationForecastDataSource.fetchForecastDataResponse(lat, lon)
            .onFailure { exception ->
                return Result.failure(exception)
            }
            .onSuccess { response ->
                return Result.success(
                    ForecastData(
                        updatedAt = response.properties.meta.updatedAt,
                        altitude = response.geometry.coordinates[2],
                        timeSeries = response.properties.timeSeries.take(timeSpanInHours).map {
                            ForecastDataItem(
                                time = it.time,
                                values = ForecastDataValues(
                                    airPressureAtSeaLevel = it.data.instant.details.airPressureAtSeaLevel,
                                    airTemperature = it.data.instant.details.airTemperature,
                                    relativeHumidity = it.data.instant.details.relativeHumidity,
                                    windSpeed = it.data.instant.details.windSpeed,
                                    windSpeedOfGust = it.data.instant.details.windSpeedOfGust,
                                    windFromDirection = it.data.instant.details.windFromDirection,
                                    fogAreaFraction = it.data.instant.details.fogAreaFraction,
                                    dewPointTemperature = it.data.instant.details.dewPointTemperature,
                                    cloudAreaFraction = it.data.instant.details.cloudAreaFraction,
                                    precipitationAmount = if (it.data.next1Hours == null) 0.0 else
                                        it.data.next1Hours.details.precipitationAmount, // should probably not be 0.0 if null
                                    probabilityOfThunder = if (it.data.next1Hours == null) 0.0 else
                                        it.data.next1Hours.details.probabilityOfThunder
                                )
                            )
                        }
                    )
                )
            }
        return Result.failure(Exception("Ukjent feil i henting av værdata"))
    }
}