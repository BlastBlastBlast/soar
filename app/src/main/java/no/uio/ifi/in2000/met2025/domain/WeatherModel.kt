package no.uio.ifi.in2000.met2025.domain

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import no.uio.ifi.in2000.met2025.data.models.Constants
import no.uio.ifi.in2000.met2025.data.models.ForecastData
import no.uio.ifi.in2000.met2025.data.models.IsobaricData
import no.uio.ifi.in2000.met2025.data.models.IsobaricDataItem
import no.uio.ifi.in2000.met2025.data.models.IsobaricDataValues
import no.uio.ifi.in2000.met2025.data.remote.forecast.LocationForecastRepository
import no.uio.ifi.in2000.met2025.data.remote.isobaric.IsobaricRepository
import java.time.Instant
import javax.inject.Inject
import kotlin.math.round

class WeatherModel @Inject constructor(
    private val locationForecastRepository: LocationForecastRepository,
    private val isobaricRepository: IsobaricRepository
) { //Businesslogikk for konsolidering av forskjellig type data

    private fun Double.roundToPointXFive(): Double {
        return round(this * 20) / 20
    }

    private suspend fun convertGribData(lat: Double, lon: Double): Result<IsobaricData> {
        return try {
            val gribResult: GribDataMap = getCurrentIsobaricData()
            val dataMap: Map<Float, GribVectors> = isobaricResult[Pair(lat.roundToPointXFive(), lon.roundToPointXFive())]
            val pressureValues = Constants.layerPressureValues
            val isobaricData = IsobaricData(
                timeSeries = [
                    IsobaricDataItem(
                        valuesAtLayer = pressureValues.associate { pressure ->
                            val gribVectors = dataMap[pressure]
                            val uComponentWind = gribVectors?.uComponentWind ?: 0.0
                            val vComponentWind = gribVectors?.vComponentWind ?: 0.0
                            IsobaricDataValues(
                                altitude = pressure,
                                windSpeed = Math.sqrt(uComponentWind * uComponentWind + vComponentWind * vComponentWind),
                                windFromDirection = Math.toDegrees(Math.atan2(uComponentWind, vComponentWind)).roundToPointXFive()
                            )
                        }
                    )
                ]
            )
            Result.success(isobaricData)
        } catch (exception: Exception) {
            Result.failure(exception)
        }
    }

    suspend fun getCurrentIsobaricData(lat: Double, lon: Double) : Result<IsobaricData>
    = withContext(Dispatchers.IO) {
        convertGribData(lat, lon).fold(
            onFailure = { exception: Throwable ->
                Result.failure(exception)
            },
            onSuccess = { isobaricData: IsobaricData ->
                combinedDataResult(isobaricData, lat, lon)
            }
        )
    }

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
//                            val forecastItem = forecastData.timeSeries.firstOrNull {
//                                it.time == isobaricItem.time
//                            }
                            val forecastItem = forecastData.timeSeries.first()
                            IsobaricDataItem(
                                time = forecastItem.time,
//                                time = isobaricItem.time,
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