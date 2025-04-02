package no.uio.ifi.in2000.met2025.domain

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import no.uio.ifi.in2000.met2025.data.models.Constants
import no.uio.ifi.in2000.met2025.data.models.CoordinateBoundaries
import no.uio.ifi.in2000.met2025.data.models.ForecastData
import no.uio.ifi.in2000.met2025.data.models.GribDataMap
import no.uio.ifi.in2000.met2025.data.models.GribVectors
import no.uio.ifi.in2000.met2025.data.models.IsobaricData
import no.uio.ifi.in2000.met2025.data.models.IsobaricDataItem
import no.uio.ifi.in2000.met2025.data.models.IsobaricDataValues
import no.uio.ifi.in2000.met2025.data.remote.forecast.LocationForecastRepository
import no.uio.ifi.in2000.met2025.data.remote.isobaric.IsobaricRepository
import no.uio.ifi.in2000.met2025.domain.helpers.RoundDoubleToXDecimals
import no.uio.ifi.in2000.met2025.domain.helpers.calculateAltitude
import no.uio.ifi.in2000.met2025.domain.helpers.calculatePressureAtAltitude
import no.uio.ifi.in2000.met2025.domain.helpers.roundToPointXFive
import javax.inject.Inject
import kotlin.math.atan2
import kotlin.math.sqrt

class WeatherModel @Inject constructor(
    private val locationForecastRepository: LocationForecastRepository,
    private val isobaricRepository: IsobaricRepository
) { //Businesslogikk for konsolidering av forskjellig type data

    suspend fun getCurrentIsobaricData(lat: Double, lon: Double) : Result<IsobaricData> {
        val gribResult = try {
            isobaricRepository.getCurrentIsobaricGribData()
        } catch (exception: Exception) {
            return Result.failure(exception)
        }
        val forecastResult = locationForecastRepository.getForecastData(lat, lon, 1) //TODO: change to find one that matches the time
        if (forecastResult.isFailure) {
            return Result.failure(forecastResult.exceptionOrNull() ?: Exception("Unknown error"))
        }
        val forecastData = forecastResult.fold(
            onFailure = {
                return Result.failure(it)
            },
            onSuccess = { forecastData ->
                forecastData
            }
        )

        return convertGribToIsobaricData(lat, lon, forecastData).fold(
            onFailure = { exception: Throwable ->
                Result.failure(exception)
            },
            onSuccess = { isobaricData: IsobaricData ->
                combinedDataResult(isobaricData, lat, lon)
            }
        )
    }

    private suspend fun convertGribToIsobaricData(lat: Double, lon: Double, forecastData: ForecastData): Result<IsobaricData> {

        println("lat $lat, lon $lon")
        if(CoordinateBoundaries.isWithinBounds(lat, lon)) {
        println("Coordinate is within bounds")

            return try {
                val updatedLat = lat.roundToPointXFive()
                val updatedLon = lon.roundToPointXFive()
                println("updated lat $updatedLat, updated lon $updatedLon")
                val update2Lat = RoundDoubleToXDecimals(updatedLat, 2)
                val update2Lon = RoundDoubleToXDecimals(updatedLon, 2)
                println("updated lat $update2Lat, updated lon $update2Lon")
                val gribResult: GribDataMap = isobaricRepository.getCurrentIsobaricGribData()
                val dataMap: Map<Int, GribVectors>? = gribResult[
                    Pair(
                        update2Lat,
                        update2Lon
                    )
                ]
                if (dataMap != null) {
                    println("rounding success")
                } else {
                    println("rounding failure")
                }
                val pressureValues = Constants.layerPressureValues
                val isobaricData = IsobaricData(
                    timeSeries = listOf(
                        IsobaricDataItem(
                            valuesAtLayer = pressureValues.associateWith { pressure ->
                                val gribVectors = dataMap?.get(pressure)
                                val uComponentWind = (gribVectors?.uComponentWind ?: 0.0).toDouble()
                                val vComponentWind = (gribVectors?.vComponentWind ?: 0.0).toDouble()
                                IsobaricDataValues(
                                    altitude = calculateAltitude(pressure.toDouble(), forecastData.timeSeries[0].values.airPressureAtSeaLevel),
                                    windSpeed = sqrt(uComponentWind * uComponentWind + vComponentWind * vComponentWind),
                                    windFromDirection = Math.toDegrees(
                                        atan2(
                                            uComponentWind,
                                            vComponentWind
                                        )
                                    )
                                )
                            }
                        )
                    )
                )
                Result.success(isobaricData)
            } catch (exception: Exception) {
                Result.failure(exception)
            }
        } else {
            return Result.failure(Exception("Coordinate not within bounds"))
        }
    }

    private fun combinedDataResult(isobaricData: IsobaricData, lat: Double, lon: Double): Result<IsobaricData> {
        val forecastResult = runBlocking {
            withContext(Dispatchers.IO) {
                locationForecastRepository.getForecastData(lat, lon, isobaricData.timeSeries.size/* * 3*/)
            }
        }
        return forecastResult.fold(
            onFailure = { exception: Throwable ->
                Result.failure(exception)
            },
            onSuccess = { forecastData: ForecastData ->
                Result.success(
                    IsobaricData(
                        updatedAt = /*if (Instant.parse(isobaricData.updatedAt).isBefore(Instant.parse(forecastData.updatedAt)))
                            isobaricData.updatedAt
                        else*/
                            forecastData.updatedAt,
                        timeSeries = isobaricData.timeSeries.map { isobaricItem ->
//                            val forecastItem = forecastData.timeSeries.firstOrNull {
//                                it.time == isobaricItem.time
//                            }
                            val forecastItem = forecastData.timeSeries.first()
                            IsobaricDataItem(
                                time = forecastItem.time,
                                valuesAtLayer = if (forecastItem == null) {
                                    isobaricItem.valuesAtLayer
                                } else {
                                    isobaricItem.valuesAtLayer.plus(
                                        calculatePressureAtAltitude(forecastData.altitude, forecastItem.values.airPressureAtSeaLevel).toInt() to
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