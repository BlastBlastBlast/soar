package no.uio.ifi.in2000.met2025.domain

import android.util.Log
import io.ktor.client.plugins.logging.Logging
import no.uio.ifi.in2000.met2025.data.models.Angle
import no.uio.ifi.in2000.met2025.data.models.CartesianIsobaricValues
import no.uio.ifi.in2000.met2025.data.remote.forecast.LocationForecastRepository
import no.uio.ifi.in2000.met2025.data.remote.isobaric.IsobaricRepository
import no.uio.ifi.in2000.met2025.domain.helpers.get
import org.apache.commons.math3.linear.RealVector
import no.uio.ifi.in2000.met2025.data.models.CoordinateBoundaries.MIN_LATITUDE
import no.uio.ifi.in2000.met2025.data.models.CoordinateBoundaries.MIN_LONGITUDE
import no.uio.ifi.in2000.met2025.data.models.CoordinateBoundaries.RESOLUTION
import no.uio.ifi.in2000.met2025.domain.helpers.div
import no.uio.ifi.in2000.met2025.domain.helpers.times
import no.uio.ifi.in2000.met2025.domain.helpers.plus
import no.uio.ifi.in2000.met2025.domain.helpers.minus
import org.apache.commons.math3.linear.ArrayRealVector
import no.uio.ifi.in2000.met2025.data.models.Constants.Companion.CELSIUS_TO_KELVIN
import no.uio.ifi.in2000.met2025.data.models.Constants.Companion.TEMPERATURE_LAPSE_RATE
import no.uio.ifi.in2000.met2025.data.models.Constants.Companion.layerPressureValues
import no.uio.ifi.in2000.met2025.data.models.CoordinateBoundaries.MAX_LATITUDE
import no.uio.ifi.in2000.met2025.data.models.CoordinateBoundaries.MAX_LONGITUDE
import no.uio.ifi.in2000.met2025.data.models.CoordinateBoundaries.isWithinBounds
import no.uio.ifi.in2000.met2025.data.models.cos
import no.uio.ifi.in2000.met2025.data.models.grib.GribDataMap
import no.uio.ifi.in2000.met2025.data.models.grib.GribDataResult
import no.uio.ifi.in2000.met2025.data.models.sin
import no.uio.ifi.in2000.met2025.domain.helpers.calculateAltitude
import no.uio.ifi.in2000.met2025.domain.helpers.calculatePressureAtAltitude
import no.uio.ifi.in2000.met2025.domain.helpers.roundToDecimals
import org.apache.commons.math3.linear.Array2DRowRealMatrix
import java.time.Instant
import kotlin.math.ceil


class IsobaricInterpolator(
    private val locationForecastRepository: LocationForecastRepository,
    private val isobaricRepository: IsobaricRepository
) {
    private val catmullRomMatrix = Array2DRowRealMatrix(
        arrayOf(
            doubleArrayOf(0.0, 1.0, 0.0, 0.0),
            doubleArrayOf(-0.5, 0.0, 0.5, 0.0),
            doubleArrayOf(1.0, -2.5, 2.0, -0.5),
            doubleArrayOf(-0.5, 1.5, -1.5, 0.5),
        )
    )

    private var gribMap: GribDataMap? = null

    // key is indices for [lat, lon, pressure]
    private val pointCache: MutableMap<List<Int>, CartesianIsobaricValues> = mutableMapOf()

    // key is indices [lat, lon, pressure]
    private val surfaceCache: MutableMap<List<Int>, (Double, Double) -> CartesianIsobaricValues> = mutableMapOf()

    private var lastVisitedIsobaricIndex: Int? = null

    private val maxLatIndex = ceil((MAX_LATITUDE - MIN_LATITUDE) * RESOLUTION).toInt()
    private val maxLonIndex = ceil((MAX_LONGITUDE - MIN_LONGITUDE) * RESOLUTION).toInt()

    suspend fun getCartesianIsobaricValues(position: RealVector, time: Instant): Result<CartesianIsobaricValues> {
        if (gribMap == null) {
            when (val gribDataResult = isobaricRepository.getIsobaricGribData(time)) {
                is GribDataResult.Success -> {
                    gribMap = gribDataResult.gribDataMap
                }
                else -> {
                    return Result.failure(Exception("Unknown error")) // TODO: Handle error properly
                }
            }
        }

        //assert(isWithinBounds(position[0], position[1])) { "Coordinates are outside the permitted bounds" }

        return getValuesAtAppropriateLevel(
            isobaricIndex = lastVisitedIsobaricIndex ?: 0,
            coordinates = position,
            time = time
        )
    }

    private tailrec suspend fun getValuesAtAppropriateLevel(
        isobaricIndex: Int,
        coordinates: RealVector,
        time: Instant
    ): Result<CartesianIsobaricValues> {
        val latIndex = coordinates[0].toGridIndex(MIN_LATITUDE)
        val lonIndex = coordinates[1].toGridIndex(MIN_LONGITUDE)

        val fractionalParts = coordinates.getSubVector(0, 2).toGridFractionalParts()
        val latFractional = fractionalParts[0]
        val lonFractional = fractionalParts[1]
        val altitude = coordinates[2]

        val lowerSurface = getSurface(listOf(latIndex, lonIndex, isobaricIndex), time)
            .fold(
                onSuccess = { it },
                onFailure = { return Result.failure(it) }
            )
        if (altitude < lowerSurface(latFractional, lonFractional).altitude) {
            if (isobaricIndex == 0) {
                Log.i("IsobaricInterpolator", "lowerSurface altitude: ${lowerSurface(latFractional, lonFractional).altitude}")
                return Result.success(
                    lowerSurface(latFractional, lonFractional)
                        .also {
                            lastVisitedIsobaricIndex = 0
                        }
                )
            }

            Log.i("IsobaricInterpolator", "lowerSurface: ${lowerSurface(latFractional, lonFractional)}")
            return getValuesAtAppropriateLevel(isobaricIndex - 1, coordinates, time)
        }

        val upperSurface = getSurface(listOf(latIndex, lonIndex, isobaricIndex + 1), time)
            .fold(
                onSuccess = { it },
                onFailure = { return Result.failure(it) }
            )
        if (altitude > upperSurface(latFractional, lonFractional).altitude) {
            if (isobaricIndex == layerPressureValues.size - 1) {
                Log.i("IsobaricInterpolator", "upperSurface: ${upperSurface(latFractional, lonFractional)}")
                return Result.success(
                    upperSurface(latFractional, lonFractional)
                        .also {
                            lastVisitedIsobaricIndex = layerPressureValues.size - 1
                        }
                )
            }

            Log.i("IsobaricInterpolator", "upperSurface: ${upperSurface(latFractional, lonFractional)}")
            return getValuesAtAppropriateLevel(isobaricIndex + 1, coordinates, time)
        }

        val lowestSurface = getSurface(listOf(latIndex, lonIndex, isobaricIndex - 1), time)
            .fold(
                onSuccess = { it },
                onFailure = { return Result.failure(it) }
            )
        val highestSurface = getSurface(listOf(latIndex, lonIndex, isobaricIndex + 2), time)
            .fold(
                onSuccess = { it },
                onFailure = { return Result.failure(it) }
            )

        return Result.success(
            interpolate(
                coordinates,
                listOf(
                    lowestSurface,
                    lowerSurface,
                    upperSurface,
                    highestSurface
                )
            )
        ).also {
            lastVisitedIsobaricIndex = isobaricIndex
        }
    }

    private suspend fun getSurface(indices: List<Int>, time: Instant): Result<(Double, Double) -> CartesianIsobaricValues> {

        Log.i("IsobaricInterpolator", "getSurface: $indices")

        return Result.success(
            surfaceCache[indices] ?: interpolatedSurface(
                Array<Array<CartesianIsobaricValues>>(4) { col ->
                    Array(4) { row ->
                        getPoint(
                            indices = listOf(indices[0] + col - 1, indices[1] + row - 1, indices[2]),
                            time = time
                        ).fold(
                            onSuccess = { it },
                            onFailure = { return Result.failure(it) }
                        )
                    }
                }
            ).also {
                surfaceCache[indices] = it
            }
        )
    }

    private var howManyAPICalls = 0

    private suspend fun getPoint(indices: List<Int>, time: Instant): Result<CartesianIsobaricValues> {

        Log.i("IsobaricInterpolator", "getPoint: $indices")

        return Result.success(
            pointCache[indices] ?:
            when {
                indices[0] < 0 ->
                    handleOutOfBounds(indices, time, 0, true)
                        .fold(onSuccess = { it }, onFailure = { return Result.failure(it) })
                indices[0] > maxLatIndex ->
                    handleOutOfBounds(indices, time, 0, false)
                        .fold(onSuccess = { it }, onFailure = { return Result.failure(it) })
                indices[1] < 0 ->
                    handleOutOfBounds(indices, time, 1, true)
                        .fold(onSuccess = { it }, onFailure = { return Result.failure(it) })
                indices[1] > maxLonIndex ->
                    handleOutOfBounds(indices, time, 1, false)
                        .fold(onSuccess = { it }, onFailure = { return Result.failure(it) })
                indices[2] < 0 ->
                    handleOutOfBounds(indices, time, 2, true)
                        .fold(onSuccess = { it }, onFailure = { return Result.failure(it) })
                indices[2] > layerPressureValues.size ->
                    handleOutOfBounds(indices, time, 2, false)
                        .fold(onSuccess = { it }, onFailure = { return Result.failure(it) })
                indices[2] == 0 -> {
                    val forecastData = locationForecastRepository.getForecastData(
                        lat = indices[0].toCoordinate(MIN_LATITUDE),
                        lon = indices[1].toCoordinate(MIN_LONGITUDE),
                        time = time,
                        cacheResponse = false
                    ).fold(
                        onSuccess = { it },
                        onFailure = { return Result.failure(it) }
                    )
                    howManyAPICalls += 1
                    Log.i("IsobaricInterpolator", "API call number: $howManyAPICalls")

                    val forecastDataValues = forecastData.timeSeries[0].values

                    val airTemperatureAtSeaLevel = forecastDataValues.airTemperature - forecastData.altitude * TEMPERATURE_LAPSE_RATE + CELSIUS_TO_KELVIN //in Kelvin

                    val groundPressure = calculatePressureAtAltitude(
                        altitude = forecastData.altitude,
                        referencePressure = forecastDataValues.airPressureAtSeaLevel,
                        referenceAirTemperature = airTemperatureAtSeaLevel,
                    )

                    val windSpeed = forecastDataValues.windSpeed
                    val windFromDirection = Angle(forecastDataValues.windFromDirection)

                    CartesianIsobaricValues(
                        altitude = forecastData.altitude,
                        pressure = groundPressure,
                        temperature = forecastDataValues.airTemperature,
                        windXComponent = cos(windFromDirection) * windSpeed,
                        windYComponent = sin(windFromDirection) * windSpeed
                    )
                }
                else -> {
                    val valuesBelow = getPoint(
                        listOf(indices[0], indices[1], indices[2] - 1),
                        time
                    ).fold(
                        onSuccess = { it },
                        onFailure = { return Result.failure(it) }
                    )
                    val pressure = (layerPressureValues).reversed()[indices[2] - 1]
                    val altitude = calculateAltitude(
                        pressure = pressure.toDouble(),
                        referencePressure = valuesBelow.pressure,
                        referenceAltitude = valuesBelow.altitude,
                        referenceAirTemperature = valuesBelow.temperature + CELSIUS_TO_KELVIN
                    )
                    // 1) make sure we have data at all
                    val grib = gribMap
                        ?: return Result.failure(Exception("GRIB data not initialized"))

                    val latIdx = indices[0].coerceIn(0, maxLatIndex)
                    val lonIdx = indices[1].coerceIn(0, maxLonIndex)
                    val pressureInt = pressure.toInt()

                    val lat  = latIdx.toCoordinate(MIN_LATITUDE)
                    val lon  = lonIdx.toCoordinate(MIN_LONGITUDE)
                    val key  = Pair(lat, lon)

                    val levelMap = grib.map[key]
                        ?: return Result.failure(Exception("No GRIB cell at [$lat, $lon]"))

                    val slice = levelMap[pressureInt]
                        ?: return Result.failure(Exception("No GRIB data for pressure level $pressureInt"))

                    val gribVector = slice

                    CartesianIsobaricValues(
                        altitude = altitude,
                        pressure = pressure.toDouble(),
                        temperature = gribVector.temperature.toDouble() - CELSIUS_TO_KELVIN,
                        windXComponent = gribVector.uComponentWind.toDouble(),
                        windYComponent = gribVector.vComponentWind.toDouble()
                    )
                }
            }.also {
                pointCache[indices] = it
                Log.i("IsobaricInterpolator", "point: $indices, value: $it")
            }
        )
    }

    private suspend fun handleOutOfBounds(
        indices: List<Int>,
        time: Instant,
        coordinate: Int,
        isLowerBound: Boolean
    ): Result<CartesianIsobaricValues> {

        Log.i("IsobaricInterpolator.handleOutOfBounds", "handleOutOfBounds: $indices, isLowerBound: $isLowerBound")

        val indexAdjustment = if (isLowerBound) 1 else -2
        Log.i("IsobaricInterpolator.handleOutOfBounds", "indexAdjustment: $indexAdjustment")
        Log.i("IsobaricInterpolator.handleOutOfBounds", "calling getPoint")
        val p1 = getPoint(
            indices.mapIndexed{ i, value -> value + if (i == coordinate) indexAdjustment else 0 },
            time
        ).fold(
            onSuccess = { it },
            onFailure = { return Result.failure(it) }
        ).toRealVector()

        Log.i("IsobaricInterpolator.handleOutOfBounds", "calling getPoint")
        val p2 = getPoint(
            indices.mapIndexed{ i, value -> value + if (i == coordinate) indexAdjustment + 1 else 0 },
            time
        ).fold(
            onSuccess = { it },
            onFailure = { return Result.failure(it) }
        ).toRealVector()

        Log.i("IsobaricInterpolator.handleOutOfBounds", "p1.altitude: ${p1[0]}, p2.altitude: ${p2[0]}")

        val extrapolatedPoint = if (isLowerBound) (2.0 * p1) - p2 else (2.0 * p2) - p1

        Log.i("IsobaricInterpolator.handleOutOfBounds", "extrapolatedPoint.altitude: ${extrapolatedPoint[0]}")

        return Result.success(
            CartesianIsobaricValues(
                altitude = extrapolatedPoint[0],
                pressure = extrapolatedPoint[1],
                temperature = extrapolatedPoint[2],
                windXComponent = extrapolatedPoint[3],
                windYComponent = extrapolatedPoint[4]
            )
        )
    }

    private fun interpolatedSurface(points: Array<Array<CartesianIsobaricValues>>): (Double, Double) -> CartesianIsobaricValues {
        assert(points.size == 4) { "Need four sets of points to interpolate over" }
        points.forEach { assert(it.size == 4) { "Need four points to interpolate over" } }

        return { t0, t1 ->
            if (t0 !in 0.0..1.0 || t1 !in 0.0..1.0) {
                Log.i("IsobaricInterpolator", "Fractional parts out of bounds: t0 = $t0, t1 = $t1")
                Log.i("IsobaricInterpolator", "Points: ${points.map { it.toList() }}")
                Log.i("IsobaricInterpolator", "Likely bad coordinate input or rounding issue")
            }

            assert(t0 in 0.0..1.0) { "Latitude fractional part out of bounds: $t0" }
            assert(t1 in 0.0..1.0) { "Longitude fractional part out of bounds: $t1" }
            assert(t0 in 0.0..1.0) { "Latitude fractional part out of bounds" }
            assert(t1 in 0.0..1.0) { "Longitude fractional part out of bounds" }

            val latVector = ArrayRealVector(doubleArrayOf(1.0, t0, t0 * t0, t0 * t0 * t0))
            val lonVector = ArrayRealVector(doubleArrayOf(1.0, t1, t1 * t1, t1 * t1 * t1))

            val altitudeMatrix = Array2DRowRealMatrix(points.map { it.map { it.altitude }.toDoubleArray() }.toTypedArray())
            val temperatureMatrix = Array2DRowRealMatrix(points.map { it.map { it.temperature }.toDoubleArray() }.toTypedArray())
            val windXMatrix = Array2DRowRealMatrix(points.map { it.map { it.windXComponent }.toDoubleArray() }.toTypedArray())
            val windYMatrix = Array2DRowRealMatrix(points.map { it.map { it.windYComponent }.toDoubleArray() }.toTypedArray())

            val altitude = latVector * catmullRomMatrix * altitudeMatrix * catmullRomMatrix.transpose() * lonVector
            val temperature = latVector * catmullRomMatrix * temperatureMatrix * catmullRomMatrix.transpose() * lonVector
            val windX = latVector * catmullRomMatrix * windXMatrix * catmullRomMatrix.transpose() * lonVector
            val windY = latVector * catmullRomMatrix * windYMatrix * catmullRomMatrix.transpose() * lonVector

//            Log.i("IsobaricInterpolator", "t0 = $t0, t1 = $t1")
//            Log.i("IsobaricInterpolator", "altitudeMatrix = $altitudeMatrix")
//            Log.i("IsobaricInterpolator", "temperatureMatrix = $temperatureMatrix")
//            Log.i("IsobaricInterpolator", "windXMatrix = $windXMatrix")
//            Log.i("IsobaricInterpolator", "windYMatrix = $windYMatrix")
//            Log.i("IsobaricInterpolator", "Interpolated values: altitude = $altitude, temperature = $temperature, windX = $windX, windY = $windY")

            CartesianIsobaricValues(
                altitude = altitude,
                pressure = points[0][0].pressure,
                temperature = temperature,
                windXComponent = windX,
                windYComponent = windY
            )
        }
    }

    private fun interpolate(coordinates: RealVector, surfaces: List<(Double, Double) -> CartesianIsobaricValues>): CartesianIsobaricValues {
        assert(coordinates.dimension == 3) { "Not a coordinate vector of dimension 3" }
        assert(surfaces.size == 4) { "Need four surfaces to interpolate over" }

        val horizontalVector = coordinates.getSubVector(0, 2)
        val fractionalParts = horizontalVector.toGridFractionalParts()
        val latFractional = fractionalParts[0]
        val lonFractional = fractionalParts[1]

        val splinePoints = surfaces.map{ it(latFractional, lonFractional).toRealVector() }
        val altitude = coordinates[2]
        val interpolatedVector = catmullRomInterpolation(altitude, splinePoints)

        return CartesianIsobaricValues(
            altitude = altitude,
            pressure = interpolatedVector[0],
            temperature = interpolatedVector[1],
            windXComponent = interpolatedVector[2],
            windYComponent = interpolatedVector[3]
        )
    }

    private fun RealVector.toGridFractionalParts(): RealVector {
        val lat = this[0]
        val lon = this[1]

        assert(dimension == 2) { "Not a coordinate vector of dimension 2" }
        assert(isWithinBounds(lat, lon)) { "Coordinates out of bounds" }

        val latFractional = lat.toGridValue(MIN_LATITUDE) - lat.toGridIndex(MIN_LATITUDE)
        val lonFractional = lon.toGridValue(MIN_LONGITUDE) - lon.toGridIndex(MIN_LONGITUDE)

        return ArrayRealVector(doubleArrayOf(latFractional, lonFractional))
    }

    private fun Double.toGridValue(lowerBound: Double) = (this - lowerBound) * RESOLUTION

    private fun Double.toGridIndex(lowerBound: Double) = toGridValue(lowerBound).toInt()

    private fun Int.toCoordinate(lowerBound: Double) = (this / RESOLUTION + lowerBound).roundToDecimals(2)
}

/**
 * Performs Catmull-Rom interpolation at a value t given 4 control points
 * v0 = (t0, p0),
 * v1 = (t1, p1),
 * v2 = (t2, p2),
 * v3 = (t3, p3),
 * with the constraint that t0 < t1 < t < t2 < t3.
 *
 * The ti's need not be equidistant.
 *
 * The pi's must have the same dimension; the dimension is also arbitrary.
 * The interpolation approximates a function f where f(ti) = pi for each i.
 *
 * The function returns a vector with the same dimension as the pi's.
 **/
fun catmullRomInterpolation(t: Double, points: List<RealVector>): RealVector {
    assert(points.size == 4) { "Need four points to execute hermite spline interpolation" }

    val (t0, t1, t2, t3) = points.map { it[0] }
    val (p0, p1, p2, p3) = points.map { it.getSubVector(1, it.dimension - 1) }

    assert(t in t1..t2) { "Value t must be in range" }

    val t21 = 1 / (t2 - t1)

    val a1 = ((t1 - t) * p0 + (t - t0) * p1) / (t1 - t0)
    val a2 = ((t2 - t) * p1 + (t - t1) * p2) * t21
    val a3 = ((t3 - t) * p2 + (t - t2) * p3) / (t3 - t2)

    val b1 = ((t2 - t) * a1 + (t - t0) * a2) / (t2 - t0)
    val b2 = ((t3 - t) * a2 + (t - t1) * a3) / (t3 - t1)

    val c = ((t2 - t) * b1 + (t - t1) * b2) * t21

    Log.i("IsobaricInterpolator", "catmullRomInterpolation: t = $t, points = $points, c = $c")

    return c
}

