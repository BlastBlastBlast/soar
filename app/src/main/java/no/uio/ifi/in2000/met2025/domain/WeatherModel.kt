package no.uio.ifi.in2000.met2025.domain

import no.uio.ifi.in2000.met2025.data.models.Constants
import no.uio.ifi.in2000.met2025.data.models.CoordinateBoundaries
import no.uio.ifi.in2000.met2025.data.models.ForecastDataItem
import no.uio.ifi.in2000.met2025.data.models.ForecastDataValues
import no.uio.ifi.in2000.met2025.data.models.GribDataMap
import no.uio.ifi.in2000.met2025.data.models.GribVectors
//import no.uio.ifi.in2000.met2025.data.models.IsobaricData
import no.uio.ifi.in2000.met2025.data.models.IsobaricData
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

    suspend fun getCurrentIsobaricData(lat: Double, lon: Double, time: Instant) : Result<IsobaricData> {
        val gribResult = try {
            isobaricRepository.getIsobaricGribData(time)
        } catch (exception: Exception) {
            return Result.failure(exception)
        }

        val forecastResult = locationForecastRepository.getForecastDataAtTime(lat, lon, time, 3, 1)
        val forecastData = forecastResult.fold(
            onFailure = { return Result.failure(it) },
            onSuccess = { it }
        )
        val forecastItem = combinedForecastDataItems(forecastData.timeSeries)

        val isobaricResult = convertGribToIsobaricData(lat, lon, gribResult, forecastItem)
        return isobaricResult.fold(
            onFailure = { exception: Throwable ->
                Result.failure(exception)
            },
            onSuccess = { isobaricItem: IsobaricData ->
                Result.success(combinedDataResult(isobaricItem, forecastItem, forecastData.altitude))
            }
        )
    }

    private fun convertGribToIsobaricData(
        lat: Double,
        lon: Double,
        gribResult: GribDataMap,
        forecastItem: ForecastDataItem
    ): Result<IsobaricData> {
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
                val isobaricData = IsobaricData(
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
                Result.success(isobaricData)
            } catch (exception: Exception) {
                Result.failure(exception)
            }
        } else {
            Result.failure(Exception("Coordinate ($lat, $lon) not within bounds"))
        }
    }

    private fun combinedDataResult(isobaricData: IsobaricData, forecastItem: ForecastDataItem, altitude: Double): IsobaricData {
        return IsobaricData(
            time = isobaricData.time,
            valuesAtLayer = isobaricData.valuesAtLayer.plus(
            calculatePressureAtAltitude(altitude, forecastItem.values.airPressureAtSeaLevel).toInt()
                    to
                    IsobaricDataValues(
                        altitude = altitude,
                        windSpeed = forecastItem.values.windSpeed,
                        windFromDirection = forecastItem.values.windFromDirection
                    )
            )
        )
    }

    private fun combinedForecastDataItems(timeSeries: List<ForecastDataItem>) : ForecastDataItem {
        return ForecastDataItem(
            time = timeSeries.first().time,
            values = ForecastDataValues(
                airPressureAtSeaLevel = timeSeries.maxOfOrNull { it.values.airPressureAtSeaLevel } ?: 0.0,
                airTemperature = timeSeries.maxOfOrNull { it.values.airTemperature } ?: 0.0,
                relativeHumidity = timeSeries.maxOfOrNull { it.values.relativeHumidity } ?: 0.0,
                windSpeed = timeSeries.maxOfOrNull { it.values.windSpeed } ?: 0.0,
                windSpeedOfGust = timeSeries.maxOfOrNull { it.values.windSpeedOfGust } ?: 0.0,
                windFromDirection = timeSeries.map { it.values.windFromDirection }.average(),
                fogAreaFraction = timeSeries.maxOfOrNull { it.values.fogAreaFraction } ?: 0.0,
                dewPointTemperature = timeSeries.maxOfOrNull { it.values.dewPointTemperature } ?: 0.0,
                cloudAreaFraction = timeSeries.maxOfOrNull { it.values.cloudAreaFraction } ?: 0.0,
                cloudAreaFractionHigh = timeSeries.maxOfOrNull { it.values.cloudAreaFractionHigh } ?: 0.0,
                cloudAreaFractionLow = timeSeries.maxOfOrNull { it.values.cloudAreaFractionLow } ?: 0.0,
                cloudAreaFractionMedium = timeSeries.maxOfOrNull { it.values.cloudAreaFractionMedium } ?: 0.0,
                precipitationAmount = timeSeries.maxOfOrNull { it.values.precipitationAmount } ?: 0.0,
                probabilityOfThunder = timeSeries.maxOfOrNull { it.values.probabilityOfThunder } ?: 0.0
            )
        )
    }
}