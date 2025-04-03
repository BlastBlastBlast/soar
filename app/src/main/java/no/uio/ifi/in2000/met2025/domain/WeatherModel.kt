package no.uio.ifi.in2000.met2025.domain

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import no.uio.ifi.in2000.met2025.data.models.Constants
import no.uio.ifi.in2000.met2025.data.models.CoordinateBoundaries
import no.uio.ifi.in2000.met2025.data.models.ForecastData
import no.uio.ifi.in2000.met2025.data.models.ForecastDataItem
import no.uio.ifi.in2000.met2025.data.models.GribDataMap
import no.uio.ifi.in2000.met2025.data.models.GribVectors
//import no.uio.ifi.in2000.met2025.data.models.IsobaricData
import no.uio.ifi.in2000.met2025.data.models.IsobaricDataItem
import no.uio.ifi.in2000.met2025.data.models.IsobaricDataValues
import no.uio.ifi.in2000.met2025.data.remote.forecast.LocationForecastRepository
import no.uio.ifi.in2000.met2025.data.remote.isobaric.IsobaricRepository
import no.uio.ifi.in2000.met2025.domain.helpers.RoundDoubleToXDecimals
import no.uio.ifi.in2000.met2025.domain.helpers.calculateAltitude
import no.uio.ifi.in2000.met2025.domain.helpers.calculatePressureAtAltitude
import no.uio.ifi.in2000.met2025.domain.helpers.roundToPointXFive
import java.time.Instant
import javax.inject.Inject
import kotlin.math.atan2
import kotlin.math.sqrt

class WeatherModel @Inject constructor(
    private val locationForecastRepository: LocationForecastRepository,
    private val isobaricRepository: IsobaricRepository
) { //Businesslogikk for konsolidering av forskjellig type data

    suspend fun getCurrentIsobaricData(lat: Double, lon: Double, time: Instant) : Result<IsobaricDataItem> {
        val gribResult = try {
            isobaricRepository.getIsobaricGribData(time)
        } catch (exception: Exception) {
            return Result.failure(exception)
        }

        val forecastResult = locationForecastRepository.getForecastDataAtTime(lat, lon, time)
        val forecastItem = forecastResult.fold(
            onFailure = { return Result.failure(it) },
            onSuccess = { it }
        )

        val isobaricResult = convertGribToIsobaricData(lat, lon, gribResult, forecastItem)
        return isobaricResult.fold(
            onFailure = { exception: Throwable ->
                Result.failure(exception)
            },
            onSuccess = { isobaricItem: IsobaricDataItem ->
                Result.success(combinedDataResult(isobaricItem, forecastItem))
            }
        )
    }

    private fun convertGribToIsobaricData(
        lat: Double,
        lon: Double,
        gribResult: GribDataMap,
        forecastItem: ForecastDataItem
    ): Result<IsobaricDataItem> {
        println("lat $lat, lon $lon")

        return if (CoordinateBoundaries.isWithinBounds(lat, lon)) {
            println("Coordinate is within bounds")

            try {
                val updatedLat = lat.roundToPointXFive()
                val updatedLon = lon.roundToPointXFive()
                println("updated lat $updatedLat, updated lon $updatedLon")

                val update2Lat = RoundDoubleToXDecimals(updatedLat, 2)
                val update2Lon = RoundDoubleToXDecimals(updatedLon, 2)
                println("updated lat $update2Lat, updated lon $update2Lon")

                val dataMap: Map<Int, GribVectors>? = gribResult[Pair(update2Lat, update2Lon)]
                println(if (dataMap != null) "rounding success" else "rounding failure")

                val pressureValues = Constants.layerPressureValues
                val isobaricItem = IsobaricDataItem(
                    time = forecastItem.time,
                    valuesAtLayer = pressureValues.associateWith { pressure ->
                        val gribVectors = dataMap?.get(pressure)
                        val uComponentWind = (gribVectors?.uComponentWind ?: 0.0).toDouble()
                        val vComponentWind = (gribVectors?.vComponentWind ?: 0.0).toDouble()

                        IsobaricDataValues(
                            altitude = calculateAltitude(pressure.toDouble(), forecastItem.values.airPressureAtSeaLevel),
                            windSpeed = sqrt(uComponentWind * uComponentWind + vComponentWind * vComponentWind),
                            windFromDirection = Math.toDegrees(
                                atan2(uComponentWind, vComponentWind)
                                )
                            )
                        }
                    )
                Result.success(isobaricItem)
            } catch (exception: Exception) {
                Result.failure(exception)
            }
        } else {
            Result.failure(Exception("Coordinate ($lat, $lon) not within bounds"))
        }
    }

    private fun combinedDataResult(isobaricItem: IsobaricDataItem, forecastItem: ForecastDataItem): IsobaricDataItem {
        return IsobaricDataItem(
            time = isobaricItem.time,
            valuesAtLayer = isobaricItem.valuesAtLayer.plus(
            calculatePressureAtAltitude(forecastItem.altitude, forecastItem.values.airPressureAtSeaLevel).toInt()
                    to
                    IsobaricDataValues(
                        altitude = forecastItem.altitude,
                        windSpeed = forecastItem.values.windSpeed,
                        windFromDirection = forecastItem.values.windFromDirection
                    )
            )
        )
    }
}