package no.uio.ifi.in2000.met2025.domain

import android.util.Log
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

/**
 * This class is responsible for interpolating isobaric weather data.
 * It fetches data points at different atmospheric levels and calculates
 * values like altitude, pressure, temperature, and wind components at
 * a given coordinate and time.
 * The interpolation utilizes Catmull-Rom splines and caching to optimize
 * data retrieval from weather repositories.
 * Assumes GRIB data is available for the given coordinates.
 *
 * For a more detailed explanation of the interpolation process, see INTERPOLATION.md
 */
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

    // data from GRIB and LocationForecast API are stored as CartesianIsobaricValues
    // Coordinates are converted to grid indices using the resolution of the GRIB data
    // key is indices for [lat, lon, pressure]
    private val pointCache: MutableMap<List<Int>, CartesianIsobaricValues> = mutableMapOf()

    // surfaceCache is used to cache the interpolated surfaces for each pressure level
    // a surface is a representation of a horizontal slice of the atmosphere at a given pressure level,
    // bounded by four indices in the latitude and longitude dimensions
    // key is indices [lat, lon, pressure]
    private val surfaceCache: MutableMap<List<Int>, (Double, Double) -> CartesianIsobaricValues> = mutableMapOf()

    // saving the last visited index to avoid having to search for the correct index each time getCartesianIsobaricValues is called
    private var lastVisitedIsobaricIndex: Int? = null

    private val maxLatIndex = ceil((MAX_LATITUDE - MIN_LATITUDE) * RESOLUTION).toInt()
    private val maxLonIndex = ceil((MAX_LONGITUDE - MIN_LONGITUDE) * RESOLUTION).toInt()

    /**
     * Initiates the interpolation process.
     * @return Result containing the interpolated values at the specified position and time, or an exception if an error occurs.
     */
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

        return getValuesAtAppropriateLevel(
            isobaricIndex = lastVisitedIsobaricIndex ?: 0,
            coordinates = position,
            time = time
        )
    }

    /**
     * Recursively finds the appropriate isobaric levels for the given coordinates and time.
     * Once the appropriate levels are found, it interpolates the values between them, at the target coordinates.
     * @return Result containing the interpolated values at the specified position and time, or an exception if an error occurs.
     */
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

    /**
     * Retrieves a 2D surface for the given indices and time.
     * @return Result containing the interpolated surface data or an exception if an error occurs.
     */
    private suspend fun getSurface(indices: List<Int>, time: Instant): Result<(Double, Double) -> CartesianIsobaricValues> {

        Log.i("IsobaricInterpolator", "getSurface: $indices")

        return Result.success(
            surfaceCache[indices] ?: interpolatedSurface(
                Array(4) { col ->
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

    // for debugging purposes
    private var howManyAPICalls = 0

    /**
     * Retrieves a point for the given indices and time as a CartesianIsobaricValues object.
     * This method handles out-of-bounds indices by extrapolating the values.
     * If the indices are within bounds, it fetches the data from the GRIB map or the location forecast repository.
     * For points computed at GRIB indices, the values of the point directly below are used,
     * in similar fashion to how WeatherModel calculates altitude.
     * @return Result containing the point data or an exception if an error occurs.
     */
    private suspend fun getPoint(indices: List<Int>, time: Instant): Result<CartesianIsobaricValues> {

        Log.i("IsobaricInterpolator", "getPoint: $indices")

        return Result.success(
            pointCache[indices] ?:
            when {
                indices[0] < 0 ->
                    extrapolatedPoint(indices, time, 0, true)
                        .fold(onSuccess = { it }, onFailure = { return Result.failure(it) })
                indices[0] > maxLatIndex ->
                    extrapolatedPoint(indices, time, 0, false)
                        .fold(onSuccess = { it }, onFailure = { return Result.failure(it) })
                indices[1] < 0 ->
                    extrapolatedPoint(indices, time, 1, true)
                        .fold(onSuccess = { it }, onFailure = { return Result.failure(it) })
                indices[1] > maxLonIndex ->
                    extrapolatedPoint(indices, time, 1, false)
                        .fold(onSuccess = { it }, onFailure = { return Result.failure(it) })
                indices[2] < 0 ->
                    extrapolatedPoint(indices, time, 2, true)
                        .fold(onSuccess = { it }, onFailure = { return Result.failure(it) })
                indices[2] > layerPressureValues.size ->
                    extrapolatedPoint(indices, time, 2, false)
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
                    val windInDirection = Angle((forecastDataValues.windFromDirection + 180.0) % 360.0)

                    CartesianIsobaricValues(
                        altitude = forecastData.altitude,
                        pressure = groundPressure,
                        temperature = forecastDataValues.airTemperature,
                        windXComponent = cos(windInDirection) * windSpeed,
                        windYComponent = sin(windInDirection) * windSpeed
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
                        temperature = gribVector.temperature.toDouble() - CELSIUS_TO_KELVIN,    // in Celsius
                        windXComponent = -gribVector.uComponentWind.toDouble(),                  // this makes the drift of the rocket align with the wind data from the apis
                        windYComponent = -gribVector.vComponentWind.toDouble()                   // not sure why
                    )
                }
            }.also {
                pointCache[indices] = it
                Log.i("IsobaricInterpolator", "point: $indices, value: $it")
            }
        )
    }

    /**
     * Handles out-of-bounds indices by extrapolating the values from nearby points.
     * @return Result containing the extrapolated point data or an exception if an error occurs.
     */
    private suspend fun extrapolatedPoint(
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

    /**
     * Interpolates a surface using Catmull-Rom splines.
     * Uses 2D uniform interpolation.
     * @return A function that takes two fractional parts and returns the interpolated values.
     */
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

            val altitudeMatrix = Array2DRowRealMatrix(points.map { it.map { values -> values.altitude }.toDoubleArray() }.toTypedArray())
            val temperatureMatrix = Array2DRowRealMatrix(points.map { it.map { values -> values.temperature }.toDoubleArray() }.toTypedArray())
            val windXMatrix = Array2DRowRealMatrix(points.map { it.map { values -> values.windXComponent }.toDoubleArray() }.toTypedArray())
            val windYMatrix = Array2DRowRealMatrix(points.map { it.map { values -> values.windYComponent }.toDoubleArray() }.toTypedArray())

            val altitude = latVector * catmullRomMatrix * altitudeMatrix * catmullRomMatrix.transpose() * lonVector
            val temperature = latVector * catmullRomMatrix * temperatureMatrix * catmullRomMatrix.transpose() * lonVector
            val windX = latVector * catmullRomMatrix * windXMatrix * catmullRomMatrix.transpose() * lonVector
            val windY = latVector * catmullRomMatrix * windYMatrix * catmullRomMatrix.transpose() * lonVector

            CartesianIsobaricValues(
                altitude = altitude,
                pressure = points[0][0].pressure,
                temperature = temperature,
                windXComponent = windX,
                windYComponent = windY
            )
        }
    }

    /**
     * Prepares data for non-uniform Catmull-Rom interpolation.
     * @return A CartesianIsobaricValues object containing the interpolated values.
     */
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
 * Performs non-uniform Catmull-Rom interpolation at a value t given 4 control points
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

