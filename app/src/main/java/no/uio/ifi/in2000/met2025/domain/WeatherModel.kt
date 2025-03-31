package no.uio.ifi.in2000.met2025.domain

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import no.uio.ifi.in2000.met2025.data.models.ForecastData
import no.uio.ifi.in2000.met2025.data.models.IsobaricData
import no.uio.ifi.in2000.met2025.data.models.IsobaricDataItem
import no.uio.ifi.in2000.met2025.data.models.IsobaricDataValues
import no.uio.ifi.in2000.met2025.data.remote.forecast.LocationForecastRepository
import no.uio.ifi.in2000.met2025.data.remote.isobaric.IsobaricRepository
import java.time.Instant
import javax.inject.Inject

class WeatherModel @Inject constructor(
    private val locationForecastRepository: LocationForecastRepository,
    private val isobaricRepository: IsobaricRepository
) { //Businesslogikk for konsolidering av forskjellig type data

    suspend fun getCurrentIsobaricData(lat: Double, lon: Double) : Result<IsobaricData>
    = Result.failure(Exception("Connection between WeatherModel and IsobaricRepository not yet made"))/*withContext(Dispatchers.IO) {
        isobaricRepository.getCurrentIsobaricGribData().fold(
            onFailure = { exception: Throwable ->
                Result.failure<IsobaricData>(exception)
            },
            onSuccess = { isobaricData: IsobaricData ->
                combinedDataResult(isobaricData, lat, lon)
            }
        )
    }*/

    private fun combinedDataResult(isobaricData: IsobaricData, lat: Double, lon: Double): Result<IsobaricData> {
        val forecastResult = runBlocking {
            withContext(Dispatchers.IO) {
                locationForecastRepository.getForecastData(lat, lon, isobaricData.timeSeries.size * 3)
            }
        }
        return forecastResult.fold(
            onFailure = { exception: Throwable ->
                Result.failure(exception)
            },
            onSuccess = { forecastData: ForecastData ->
                Result.success(
                    IsobaricData(
                        updatedAt = if (Instant.parse(isobaricData.updatedAt)
                                .isBefore(Instant.parse(forecastData.updatedAt))
                        )
                            isobaricData.updatedAt
                        else
                            forecastData.updatedAt,
                        timeSeries = isobaricData.timeSeries.map { isobaricItem ->
                            val forecastItem = forecastData.timeSeries.firstOrNull {
                                it.time == isobaricItem.time
                            }
                            IsobaricDataItem(
                                time = isobaricItem.time,
                                valuesAtLayer = if (forecastItem == null) {
                                    isobaricItem.valuesAtLayer
                                } else {
                                    isobaricItem.valuesAtLayer.plus(
                                        forecastItem.values.airPressureAtSeaLevel to
                                                IsobaricDataValues(
                                                    altitude = forecastData.altitude,
                                                    windSpeed = forecastItem.values.windSpeed,
                                                    windFromDirection = forecastItem.values.windFromDirection
                                                )
                                    )
                                }
                            )
                        }
                    )
                )
            }
        )
    }
}