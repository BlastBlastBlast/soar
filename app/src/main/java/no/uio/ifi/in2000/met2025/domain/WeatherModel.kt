package no.uio.ifi.in2000.met2025.domain

import no.uio.ifi.in2000.met2025.data.models.Constants.Companion.CELSIUS_TO_KELVIN
import no.uio.ifi.in2000.met2025.data.models.Constants.Companion.TEMPERATURE_LAPSE_RATE
import no.uio.ifi.in2000.met2025.data.models.Constants.Companion.layerPressureValues
import no.uio.ifi.in2000.met2025.data.models.CoordinateBoundaries
import no.uio.ifi.in2000.met2025.data.models.locationforecast.ForecastDataItem
import no.uio.ifi.in2000.met2025.data.models.locationforecast.ForecastDataValues
import no.uio.ifi.in2000.met2025.data.models.grib.GribDataMap
import no.uio.ifi.in2000.met2025.data.models.grib.GribDataResult
import no.uio.ifi.in2000.met2025.data.models.grib.GribVectors
import no.uio.ifi.in2000.met2025.data.models.isobaric.IsobaricData
import no.uio.ifi.in2000.met2025.data.models.isobaric.IsobaricDataResult
import no.uio.ifi.in2000.met2025.data.models.isobaric.IsobaricDataValues
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


/**
 * WeatherModel
 *
 * Core business logic for combining forecast and isobaric data into a unified
 * IsobaricDataResult, handling unit conversions, interpolation, and boundary checks.
 *
 * Special notes:
 * - Applies the standard atmospheric lapse rate to adjust temperatures to sea level.
 * - Converts lat/lon to grid-aligned coordinates before lookup.
 * - Builds a vertical profile of isobaric values up from the surface.
 */
class WeatherModel @Inject constructor(
    private val locationForecastRepository: LocationForecastRepository,
    private val isobaricRepository: IsobaricRepository
) {

    /**
     * Retrieves and merges forecast and GRIB data for a specific time and location.
     */
    suspend fun getCurrentIsobaricData(
        lat: Double,
        lon: Double,
        time: Instant
    ): IsobaricDataResult {
        val gribResult = isobaricRepository.getIsobaricGribData(time)

        val gribDataMap : GribDataMap
        when (gribResult) {
            is GribDataResult.Success -> gribDataMap = gribResult.gribDataMap
            GribDataResult.AvailabilityError -> return IsobaricDataResult.GribAvailabilityError
            GribDataResult.FetchingError -> return IsobaricDataResult.GribFetchingError
            GribDataResult.ParsingError -> return IsobaricDataResult.DataParsingError
        }

        val forecastResult = locationForecastRepository.getForecastData(lat = lat, lon = lon, timeSpanInHours = 3, time = time)
        val forecastData = forecastResult.fold(
            onFailure = { return IsobaricDataResult.LocationForecastFetchingError },
            onSuccess = { it }
        )
        val forecastItem = combinedForecastDataItems(forecastData.timeSeries)

        val airTemperatureAtSeaLevel = forecastItem.values.airTemperature - forecastData.altitude * TEMPERATURE_LAPSE_RATE + CELSIUS_TO_KELVIN //in Kelvin

        val groundPressure = calculatePressureAtAltitude(
            altitude = forecastData.altitude,
            referencePressure = forecastItem.values.airPressureAtSeaLevel,
            referenceAirTemperature = airTemperatureAtSeaLevel
        ).toInt()

        return convertGribToIsobaricData(
            lat,
            lon,
            gribDataMap,
            forecastItem,
            groundPressure,
            forecastData.altitude,
            airTemperatureAtSeaLevel
        )
    }

    //TODO: Convert prints to logs
    /**
     * Combines GRIB and forecast into IsobaricDataResult.Success or OutOfBounds.
     */
    private fun convertGribToIsobaricData(
        lat: Double,
        lon: Double,
        gribDataMap: GribDataMap,
        forecastItem: ForecastDataItem,
        groundPressure: Int,
        groundAltitude: Double,
        airTemperatureAtSeaLevel: Double
    ): IsobaricDataResult {
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
                val gribVectorsMap = dataMap!!

                IsobaricDataResult.Success(
                    Result.success(
                        IsobaricData(
                            time = gribDataMap.time,
                            valuesAtLayer = buildValuesAtLayerR(
                                pressureValues = layerPressureValues.reversed(),
                                previousPressure = groundPressure,
                                gribVectorsMap = gribVectorsMap,
                                referenceTemperature = airTemperatureAtSeaLevel,
                                result = mapOf(
                                    groundPressure to IsobaricDataValues(
                                        altitude = groundAltitude,
                                        airTemperature = forecastItem.values.airTemperature,
                                        windSpeed = forecastItem.values.windSpeed,
                                        windFromDirection = forecastItem.values.windFromDirection
                                    )
                                )
                            )
                        )
                    )
                )
            } catch (exception: Exception) {
                return IsobaricDataResult.DataParsingError
            }
        } else {
            IsobaricDataResult.OutOfBoundsError
        }
    }

    /**
     * Recursively builds isobaric data from surface up through predefined pressure levels.
     */
    private fun buildValuesAtLayerR(
        pressureValues: List<Int>,
        previousPressure: Int,
        gribVectorsMap: Map<Int, GribVectors>,
        referenceTemperature: Double,
        result: Map<Int, IsobaricDataValues>
    ): Map<Int, IsobaricDataValues> {

        if (pressureValues.isEmpty()) {
            return result
        }

        val pressure = pressureValues.first()
        val previousIsobaricDataValues = result[previousPressure]!!
        val gribVectors = gribVectorsMap[pressure]
        val airTemperature = ((gribVectorsMap[pressure]?.temperature)?.toDouble()) ?: 0.0 //TODO: add support for null values
        val uComponentWind = (gribVectors?.uComponentWind ?: 0.0).toDouble()
        val vComponentWind = (gribVectors?.vComponentWind ?: 0.0).toDouble()

        return buildValuesAtLayerR(
            pressureValues = pressureValues.drop(1),
            previousPressure = pressure,
            gribVectorsMap = gribVectorsMap,
            referenceTemperature = airTemperature,
            result = result + (pressure to IsobaricDataValues(
                altitude = calculateAltitude(
                    pressure = pressure.toDouble(),
                    referencePressure = previousPressure.toDouble(),
                    referenceAirTemperature = referenceTemperature,
                    referenceAltitude = previousIsobaricDataValues.altitude
                ),
                airTemperature = airTemperature - CELSIUS_TO_KELVIN,
                windSpeed = sqrt(uComponentWind * uComponentWind + vComponentWind * vComponentWind),
                windFromDirection = (Math.toDegrees(
                    atan2(uComponentWind, vComponentWind)) + 180) % 360
            )
                    )
        )
    }

    /**
     * Aggregates a list of ForecastDataItem into a single representative item.
     *
     * Chooses maximum values (or average where appropriate) across the timespan.
     */
    private fun combinedForecastDataItems(timeSeries: List<ForecastDataItem>): ForecastDataItem {
        return ForecastDataItem(
            time = timeSeries.first().time,
            values = ForecastDataValues(
                airPressureAtSeaLevel = timeSeries.maxOfOrNull { it.values.airPressureAtSeaLevel } ?: 0.0,
                airTemperature = timeSeries.maxOfOrNull { it.values.airTemperature } ?: 0.0,
                relativeHumidity = timeSeries.maxOfOrNull { it.values.relativeHumidity } ?: 0.0,
                windSpeed = timeSeries.maxOfOrNull { it.values.windSpeed } ?: 0.0,
                // Nullable fields: no non-null value exists, null is returned.
                windSpeedOfGust = timeSeries.mapNotNull { it.values.windSpeedOfGust }.maxOrNull(),
                windFromDirection = timeSeries.map { it.values.windFromDirection }.average(),
                fogAreaFraction = timeSeries.mapNotNull { it.values.fogAreaFraction }.maxOrNull(),
                dewPointTemperature = timeSeries.mapNotNull { it.values.dewPointTemperature }.maxOrNull(),
                cloudAreaFraction = timeSeries.maxOfOrNull { it.values.cloudAreaFraction } ?: 0.0,
                cloudAreaFractionHigh = timeSeries.maxOfOrNull { it.values.cloudAreaFractionHigh } ?: 0.0,
                cloudAreaFractionLow = timeSeries.maxOfOrNull { it.values.cloudAreaFractionLow } ?: 0.0,
                cloudAreaFractionMedium = timeSeries.maxOfOrNull { it.values.cloudAreaFractionMedium } ?: 0.0,
                precipitationAmount = timeSeries.mapNotNull { it.values.precipitationAmount }.maxOrNull(),
                probabilityOfThunder = timeSeries.mapNotNull { it.values.probabilityOfThunder }.maxOrNull()
            )
        )
    }
}