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
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import javax.inject.Inject

/**
* LocationForecastRepository
*
* Fetches location forecast data from the remote API, applies simple caching,
* and provides functions to filter and time‐zone adjust the results.
*
* Special notes:
* - Caches a single response per coordinate pair for up to one hour.
* - Uses a Mutex to ensure thread‐safe cache access.
*/
class LocationForecastRepository @Inject constructor(
    private val locationForecastDataSource: LocationForecastDataSource
) {
    private var cachedForecastDataResponse: ForecastDataResponse? = null
    private var cachedCoordinates: Pair<Double, Double>? = null

    /**
     * Fetches the raw ForecastDataResponse, using the cache if the same
     * coordinates were requested within the last hour.
     */
    private suspend fun fetchForecastDataResponse(
        lat: Double,
        lon: Double,
        cacheResponse: Boolean
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
                        if (cacheResponse) {
                            // Cache the response if requested.
                            cachedForecastDataResponse = response
                        }
                    }
                }
            }
        }
    }

    /**
     * Returns processed forecast data, filtered by time span and frequency.
     */
    suspend fun getForecastData(
        lat: Double,
        lon: Double,
        timeSpanInHours: Int = 0,
        time: Instant? = null,
        frequencyInHours: Int = 1,
        cacheResponse: Boolean = true
    ): Result<ForecastData> {
        val response = fetchForecastDataResponse(lat, lon, cacheResponse).fold(
            onFailure = { return Result.failure(it) },
            onSuccess = { it }
        )
        val responseItems = response.properties.timeSeries
        // Filter by start time if provided
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
                            probabilityOfThunder = ts.data.next1Hours?.details?.probabilityOfThunder,
                            symbolCode = ts.data.next1Hours?.summary?.symbolCode

                        )
                    )
                }
            )
        )
    }

    /**
     * Converts all timestamp strings in the forecast to Europe/Oslo local time,
     * formatted as ISO_OFFSET_DATE_TIME.
     */
    suspend fun getTimeZoneAdjustedForecast(
        lat: Double,
        lon: Double,
        timeSpanInHours: Int
    ): Result<ForecastData> {

        val response = getForecastData(lat, lon, timeSpanInHours).fold(
            onFailure = { return Result.failure(it) },
            onSuccess = { it }
        )

        val osloZone = ZoneId.of("Europe/Oslo")
        val formatter = DateTimeFormatter.ISO_OFFSET_DATE_TIME

        // Convert each entry's time from UTC to Oslo time with offset
        val adjustedTimeSeries = response.timeSeries.map { item ->
            val osloTime = Instant.parse(item.time).atZone(osloZone)
            val formattedTime = formatter.format(osloTime)

            item.copy(time = formattedTime)
        }

        val result = ForecastData(response.updatedAt, response.altitude, adjustedTimeSeries)

        return Result.success(result)
    }

    /**
     * Takes entries while their timestamp does not exceed the given span
     * from the first entry.
     */
    private fun List<TimeSeries>.takeUntilDurationExceeds(threshold: Int): List<TimeSeries> {
        val lastHour = Instant.parse(this.first().time) + Duration.ofHours(threshold.toLong())
        return this.takeWhile { timeSeries ->
            Instant.parse(timeSeries.time) <= lastHour
        }
    }
}