package no.uio.ifi.in2000.met2025.data.remote.forecast

import androidx.compose.animation.core.rememberTransition
import no.uio.ifi.in2000.met2025.data.models.ForecastData
import no.uio.ifi.in2000.met2025.data.models.ForecastDataItem
import no.uio.ifi.in2000.met2025.data.models.ForecastDataValues
import java.time.Instant
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

                        timeSeries = response.properties.timeSeries.take(timeSpanInHours).map {
                            ForecastDataItem(
                                time = it.time,
                                altitude = response.geometry.coordinates[2],
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
                                    cloudAreaFractionHigh = it.data.instant.details.cloudAreaFractionHigh,
                                    cloudAreaFractionLow = it.data.instant.details.cloudAreaFractionLow,
                                    cloudAreaFractionMedium = it.data.instant.details.cloudAreaFractionMedium,
                                    precipitationAmount = it.data.next1Hours?.details?.precipitationAmount ?: 0.0,
                                    probabilityOfThunder = it.data.next1Hours?.details?.probabilityOfThunder ?: 0.0
                                )
                            )
                        }
                    )
                )
            }
        return Result.failure(Exception("Unknown error fetching forecast data"))
    }

    suspend fun getForecastDataAtTime(lat: Double, lon: Double, time: Instant, items: Int, frequencyInHours: Int = 1): Result<ForecastData> {
        val response = locationForecastDataSource.fetchForecastDataResponse(lat, lon).fold(
            onFailure = { return Result.failure(it) },
            onSuccess = { it }
        )

        val responseItems = response.properties.timeSeries
            .filter { Instant.parse(it.time) >= time.minus(Duration.ofHours(1)) }
            .filterIndexed{ index, _ -> index % frequencyInHours == 0 }
            .take(items)
            //?: return Result.failure(Exception("No forecast data available for the given time"))

        return Result.success(
            ForecastData(
                updatedAt = response.properties.meta.updatedAt,
                altitude = response.geometry.coordinates[2],
                timeSeries = responseItems.map { responseItem ->
                    ForecastDataItem(
                        time = responseItem.time,
                        altitude = response.geometry.coordinates[2],
                        values = ForecastDataValues(
                            airPressureAtSeaLevel = responseItem.data.instant.details.airPressureAtSeaLevel,
                            airTemperature = responseItem.data.instant.details.airTemperature,
                            relativeHumidity = responseItem.data.instant.details.relativeHumidity,
                            windSpeed = responseItem.data.instant.details.windSpeed,
                            windSpeedOfGust = responseItem.data.instant.details.windSpeedOfGust,
                            windFromDirection = responseItem.data.instant.details.windFromDirection,
                            fogAreaFraction = responseItem.data.instant.details.fogAreaFraction,
                            dewPointTemperature = responseItem.data.instant.details.dewPointTemperature,
                            cloudAreaFraction = responseItem.data.instant.details.cloudAreaFraction,
                            cloudAreaFractionHigh = responseItem.data.instant.details.cloudAreaFractionHigh,
                            cloudAreaFractionLow = responseItem.data.instant.details.cloudAreaFractionLow,
                            cloudAreaFractionMedium = responseItem.data.instant.details.cloudAreaFractionMedium,
                            precipitationAmount = responseItem.data.next1Hours?.details?.precipitationAmount ?: 0.0,
                            probabilityOfThunder = responseItem.data.next1Hours?.details?.probabilityOfThunder ?: 0.0
                        )
                    )
                }
            )
        )
    }
}
