package no.uio.ifi.in2000.met2025.data.remote.forecast

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import no.uio.ifi.in2000.met2025.data.models.locationforecast.ForecastData
import no.uio.ifi.in2000.met2025.data.models.locationforecast.ForecastDataItem
import no.uio.ifi.in2000.met2025.data.models.locationforecast.ForecastDataResponse
import no.uio.ifi.in2000.met2025.data.models.locationforecast.ForecastDataValues
import no.uio.ifi.in2000.met2025.data.models.locationforecast.TimeSeries
import java.time.Duration
import java.time.Instant
import javax.inject.Inject

// LocationForecastRepository.kt
class LocationForecastRepository @Inject constructor(
    private val locationForecastDataSource: LocationForecastDataSource
) {
    // Cache both the forecast response and the coordinates used to fetch it.
    private var cachedForecastDataResponse: ForecastDataResponse? = null
    private var cachedCoordinates: Pair<Double, Double>? = null

    private suspend fun fetchForecastDataResponse(
        lat: Double,
        lon: Double,
    ): Result<ForecastDataResponse> {
        Mutex().withLock {
            // Invalidate the cache if the coordinates have changed.
            if (cachedCoordinates == null || cachedCoordinates != Pair(lat, lon)) {
                cachedForecastDataResponse = null
                cachedCoordinates = Pair(lat, lon)
            }
            // If we have a cached result and it was updated within the last hour, use it.
            return if (cachedForecastDataResponse != null &&
                Instant.parse(cachedForecastDataResponse!!.properties.meta.updatedAt) > Instant.now() - Duration.ofHours(1)
            ) {
                Result.success(cachedForecastDataResponse!!)
            } else {
                locationForecastDataSource.fetchForecastDataResponse(lat, lon).also { result ->
                    result.onSuccess { response ->
                        cachedForecastDataResponse = response
                    }
                }
            }
        }
    }

    suspend fun getForecastData(
        lat: Double,
        lon: Double,
        timeSpanInHours: Int,
        time: Instant? = null,
        frequencyInHours: Int = 1
    ): Result<ForecastData> {
        val response = fetchForecastDataResponse(lat, lon).fold(
            onFailure = { return Result.failure(it) },
            onSuccess = { it }
        )
        // (The rest of your filtering logic remains the same.)
        val responseItems = response.properties.timeSeries
        val filteredItems = if (time != null) {
            responseItems.filter { Instant.parse(it.time) >= time.minus(Duration.ofHours(1)) }
        } else {
            responseItems
        }.filterIndexed { index, _ -> index % frequencyInHours == 0 }
            .takeUntilDurationExceeds(timeSpanInHours)

        return Result.success(
            ForecastData(
                updatedAt = response.properties.meta.updatedAt,
                altitude = response.geometry.coordinates[2],
                timeSeries = filteredItems.map { ts ->
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

    private fun List<TimeSeries>.takeUntilDurationExceeds(threshold: Int): List<TimeSeries> {
        val lastHour = Instant.parse(this.first().time) + Duration.ofHours(threshold.toLong())
        return this.takeWhile { timeSeries ->
            Instant.parse(timeSeries.time) <= lastHour
        }
    }
}