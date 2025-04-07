package no.uio.ifi.in2000.met2025.domain

import no.uio.ifi.in2000.met2025.data.models.Constants
import no.uio.ifi.in2000.met2025.data.models.Constants.Companion.layerPressureValues
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
        val gribResult = isobaricRepository.getIsobaricGribData(time).fold(
            onFailure = { return Result.failure(it) },
            onSuccess = { it }
        )

        val forecastResult = locationForecastRepository.getForecastDataAtTime(lat, lon, time, 3, 1)
        val forecastData = forecastResult.fold(
            onFailure = { return Result.failure(it) },
            onSuccess = { it }
        )
        val forecastItem = combinedForecastDataItems(forecastData.timeSeries)

        val groundLevelPressure = calculatePressureAtAltitude(forecastData.altitude, forecastItem.values.airPressureAtSeaLevel, forecastItem.values.airTemperature).toInt()

        return convertGribToIsobaricData(lat, lon, gribResult, forecastItem, groundLevelPressure, forecastData.altitude)
//        return isobaricResult.fold(
//            onFailure = { exception: Throwable ->
//                Result.failure(exception)
//            },
//            onSuccess = { isobaricItem: IsobaricData ->
//                Result.success(combinedDataResult(isobaricItem, forecastItem, forecastData.altitude))
//            }
//        )
    }

    private fun convertGribToIsobaricData(
        lat: Double,
        lon: Double,
        gribDataMap: GribDataMap,
        forecastItem: ForecastDataItem,
        groundLevelPressure: Int,
        groundAltitude: Double
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

                val dataMap: Map<Int, GribVectors>? = gribDataMap.map[Pair(update2Lat, update2Lon)]
                println(if (dataMap != null) "rounding success" else "rounding failure")

                val isobaricData = IsobaricData(
                    time = gribDataMap.time,
                    valuesAtLayer = buildValuesAtLayerR(
                        pressureValues = layerPressureValues.reversed(),
                        previousPressure = groundLevelPressure,
                        gribVectorsMap = dataMap!!,
                        result = mapOf(
                            groundLevelPressure to IsobaricDataValues(
                                altitude = groundAltitude,
                                airTemperature = forecastItem.values.airTemperature,
                                windSpeed = forecastItem.values.windSpeed,
                                windFromDirection = forecastItem.values.windFromDirection
                            )
                        )
                    )
                )
                Result.success(isobaricData)
            } catch (exception: Exception) {
                Result.failure(exception)
            }
        } else {
            Result.failure(Exception("Coordinate ($lat, $lon) not within bounds"))
        }
    }

    private fun buildValuesAtLayerR(
        pressureValues: List<Int>,
        previousPressure: Int,
        gribVectorsMap: Map<Int, GribVectors>,
        result: Map<Int, IsobaricDataValues>
    ): Map<Int, IsobaricDataValues> {

        if (pressureValues.isEmpty()) {
            return result
        }

        val pressure = pressureValues.first()
        val previousIsobaricDataValues = result[previousPressure]!!
        val gribVectors = gribVectorsMap[pressure]
        val uComponentWind = (gribVectors?.uComponentWind ?: 0.0).toDouble()
        val vComponentWind = (gribVectors?.vComponentWind ?: 0.0).toDouble()

        return buildValuesAtLayerR(
            pressureValues = pressureValues.drop(1),
            previousPressure = pressure,
            gribVectorsMap = gribVectorsMap,
            result = result + (pressure to IsobaricDataValues(
                        altitude = calculateAltitude(
                            pressure = pressure.toDouble(),
                            referencePressure = previousPressure.toDouble(),
                            referenceAirTemperature = previousIsobaricDataValues.airTemperature,
                            referenceAltitude = previousIsobaricDataValues.altitude
                        ),
                        airTemperature = (gribVectorsMap[pressure]?.temperature)?.toDouble() ?: 0.0,
                        windSpeed = sqrt(uComponentWind * uComponentWind + vComponentWind * vComponentWind),
                        windFromDirection = Math.toDegrees(
                            atan2(uComponentWind, vComponentWind))
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