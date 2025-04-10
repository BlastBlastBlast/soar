package no.uio.ifi.in2000.met2025.data.remote.forecast

import androidx.compose.animation.core.rememberTransition
import no.uio.ifi.in2000.met2025.data.models.ForecastData
import no.uio.ifi.in2000.met2025.data.models.ForecastDataItem
import no.uio.ifi.in2000.met2025.data.models.ForecastDataValues
import java.time.Duration
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
                        altitude = response.geometry.coordinates[2],
                        timeSeries = response.properties.timeSeries.take(timeSpanInHours).map { ts ->
                            ForecastDataItem(
                                time = ts.time,
                                values = ForecastDataValues(
                                    airPressureAtSeaLevel = ts.data.instant.details.airPressureAtSeaLevel,
                                    airTemperature = ts.data.instant.details.airTemperature,
                                    relativeHumidity = ts.data.instant.details.relativeHumidity,
                                    windSpeed = ts.data.instant.details.windSpeed,
                                    // Nullable fields
                                    windSpeedOfGust = ts.data.instant.details.windSpeedOfGust,
                                    windFromDirection = ts.data.instant.details.windFromDirection,
                                    fogAreaFraction = ts.data.instant.details.fogAreaFraction,
                                    dewPointTemperature = ts.data.instant.details.dewPointTemperature,
                                    cloudAreaFraction = ts.data.instant.details.cloudAreaFraction,
                                    cloudAreaFractionHigh = ts.data.instant.details.cloudAreaFractionHigh,
                                    cloudAreaFractionLow = ts.data.instant.details.cloudAreaFractionLow,
                                    cloudAreaFractionMedium = ts.data.instant.details.cloudAreaFractionMedium,
                                    // Nullable fields Next1Hours
                                    precipitationAmount = ts.data.next1Hours?.details?.precipitationAmount,
                                    probabilityOfThunder = ts.data.next1Hours?.details?.probabilityOfThunder
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
            .filterIndexed { index, _ -> index % frequencyInHours == 0 }
            .take(items)

        return Result.success(
            ForecastData(
                updatedAt = response.properties.meta.updatedAt,
                altitude = response.geometry.coordinates[2],
                timeSeries = responseItems.map { ts ->
                    ForecastDataItem(
                        time = ts.time,
                        values = ForecastDataValues(
                            airPressureAtSeaLevel = ts.data.instant.details.airPressureAtSeaLevel,
                            airTemperature = ts.data.instant.details.airTemperature,
                            relativeHumidity = ts.data.instant.details.relativeHumidity,
                            windSpeed = ts.data.instant.details.windSpeed,
                            windSpeedOfGust = ts.data.instant.details.windSpeedOfGust,
                            windFromDirection = ts.data.instant.details.windFromDirection,
                            fogAreaFraction = ts.data.instant.details.fogAreaFraction,
                            dewPointTemperature = ts.data.instant.details.dewPointTemperature,
                            cloudAreaFraction = ts.data.instant.details.cloudAreaFraction,
                            cloudAreaFractionHigh = ts.data.instant.details.cloudAreaFractionHigh,
                            cloudAreaFractionLow = ts.data.instant.details.cloudAreaFractionLow,
                            cloudAreaFractionMedium = ts.data.instant.details.cloudAreaFractionMedium,
                            precipitationAmount = ts.data.next1Hours?.details?.precipitationAmount,
                            probabilityOfThunder = ts.data.next1Hours?.details?.probabilityOfThunder
                        )
                    )
                }
            )
        )
    }
}