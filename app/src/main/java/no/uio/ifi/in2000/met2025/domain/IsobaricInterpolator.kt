package no.uio.ifi.in2000.met2025.domain

import no.uio.ifi.in2000.met2025.data.models.Angle
import no.uio.ifi.in2000.met2025.data.models.CartesianIsobaricValues
import no.uio.ifi.in2000.met2025.data.remote.forecast.LocationForecastRepository
import no.uio.ifi.in2000.met2025.data.remote.isobaric.IsobaricRepository
import no.uio.ifi.in2000.met2025.domain.helpers.get
import org.apache.commons.math3.linear.RealVector
import no.uio.ifi.in2000.met2025.data.models.CoordinateBoundaries.isWithinBounds
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
import no.uio.ifi.in2000.met2025.data.models.cos
import no.uio.ifi.in2000.met2025.data.models.grib.GribDataMap
import no.uio.ifi.in2000.met2025.data.models.grib.GribDataResult
import no.uio.ifi.in2000.met2025.data.models.sin
import no.uio.ifi.in2000.met2025.domain.helpers.calculateAltitude
import no.uio.ifi.in2000.met2025.domain.helpers.calculatePressureAtAltitude
import org.apache.commons.math3.linear.Array2DRowRealMatrix
import java.time.Instant


class IsobaricInterpolator(
    private val locationForecastRepository: LocationForecastRepository,
    private val isobaricRepository: IsobaricRepository
) {
    val catmullRomMatrix = Array2DRowRealMatrix(
        arrayOf(
            doubleArrayOf(0.0, 1.0, 0.0, 0.0),
            doubleArrayOf(-0.5, 0.0, 0.5, 0.0),
            doubleArrayOf(1.0, -2.5, 2.0, -0.5),
            doubleArrayOf(-0.5, 1.5, -1.5, 0.5),
        )
    )

    var gribMap: GribDataMap? = null

    // key is indices for [lat, lon, pressure]
    val pointCache: MutableMap<IntArray, CartesianIsobaricValues> = mutableMapOf()

    // key is indices [lat, lon, pressure]
    val surfaceCache: MutableMap<IntArray, (Double, Double) -> CartesianIsobaricValues> = mutableMapOf()

    var lastVisitedIsobaricIndex: Int? = null

    val maxLatIndex = ((MAX_LATITUDE - MIN_LATITUDE) * RESOLUTION).toInt()
    val maxLonIndex = ((MAX_LONGITUDE - MIN_LONGITUDE) * RESOLUTION).toInt()

    suspend fun getCartesianIsobaricValues(position: RealVector, time: Instant): Result<CartesianIsobaricValues> {
        if (gribMap == null) {
            val gribDataResult = isobaricRepository.getIsobaricGribData(time)

            when (gribDataResult) {
                is GribDataResult.Success -> {
                    gribMap = gribDataResult.gribDataMap
                }
                else -> {
                    return Result.failure(Exception("Unknown error")) // TODO: Handle error properly
                }
            }
        }

        assert(isWithinBounds(position[0], position[1])) { "Coordinates are outside the permitted bounds" }

        return Result.success(
            getValuesAtAppropriateLevel(
                isobaricIndex = lastVisitedIsobaricIndex ?: 0,
                coordinates = position,
                time = time
            ).fold(
                onSuccess = { it },
                onFailure = { return Result.failure(it) }
            )
        )
    }

    tailrec suspend fun getValuesAtAppropriateLevel(
        isobaricIndex: Int,
        coordinates: RealVector,
        time: Instant
    ): Result<CartesianIsobaricValues> {
        if (isobaricIndex < 0 || isobaricIndex >= layerPressureValues.size) {
            return Result.failure(Exception("No appropriate level found"))
        }

        val latIndex = coordinates[0].toGridIndex(MIN_LATITUDE)
        val lonIndex = coordinates[1].toGridIndex(MIN_LONGITUDE)

        val fractionalParts = coordinates.getSubVector(0, 2).toGridFractionalParts()
        val latFractional = fractionalParts[0]
        val lonFractional = fractionalParts[1]
        val altitude = coordinates[2]

        val lowerSurface = getSurface(intArrayOf(latIndex, lonIndex, isobaricIndex), time)
            .fold(
                onSuccess = { it },
                onFailure = { return Result.failure(it) }
            )
        if (altitude < lowerSurface(latFractional, lonFractional).altitude) {
            return getValuesAtAppropriateLevel(isobaricIndex - 1, coordinates, time)
        }

        val upperSurface = getSurface(intArrayOf(latIndex, lonIndex, isobaricIndex + 1), time)
            .fold(
                onSuccess = { it },
                onFailure = { return Result.failure(it) }
            )
        if (altitude > upperSurface(latFractional, lonFractional).altitude) {
            return getValuesAtAppropriateLevel(isobaricIndex + 1, coordinates, time)
        }

        val lowestSurface = getSurface(intArrayOf(latIndex, lonIndex, isobaricIndex - 1), time)
            .fold(
                onSuccess = { it },
                onFailure = { return Result.failure(it) }
            )
        val highestSurface = getSurface(intArrayOf(latIndex, lonIndex, isobaricIndex + 2), time)
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

    suspend fun getSurface(indices: IntArray, time: Instant): Result<(Double, Double) -> CartesianIsobaricValues> {
//        if (indices[2] == -1)
        return Result.success(
            surfaceCache[indices] ?: interpolatedSurface(
                Array<Array<CartesianIsobaricValues>>(4) { col ->
                    Array(4) { row ->
                        getPoint(
                            indices = intArrayOf(indices[0] + col - 1, indices[1] + row - 1, indices[2]),
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

    suspend fun getPoint(indices: IntArray, time: Instant): Result<CartesianIsobaricValues> {
        suspend fun getPointR(indices: IntArray): Result<CartesianIsobaricValues> {
            return Result.success(
                pointCache[indices] ?:
                (if (indices[2] == 0) {
                    val forecastData = locationForecastRepository.getForecastData(
                        lat = indices[0].toCoordinate(MIN_LATITUDE),
                        lon = indices[1].toCoordinate(MIN_LONGITUDE),
                        time = time,
                        cacheResponse = false
                    ).fold(
                        onSuccess = { it },
                        onFailure = { return Result.failure(it) }
                    )

                    val forecastDataValues = forecastData.timeSeries[0].values

                    val airTemperatureAtSeaLevel = forecastDataValues.airTemperature - forecastData.altitude * TEMPERATURE_LAPSE_RATE + CELSIUS_TO_KELVIN //in Kelvin

                    val groundPressure = calculatePressureAtAltitude(
                        altitude = forecastData.altitude,
                        referencePressure = forecastDataValues.airPressureAtSeaLevel,
                        referenceAirTemperature = airTemperatureAtSeaLevel
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
                } else {
                    val valuesBelow = getPointR(
                        intArrayOf(indices[0], indices[1], indices[2] - 1)
                    ).fold(
                        onSuccess = { it },
                        onFailure = { return Result.failure(it) }
                    )
                    val pressure = (layerPressureValues).reversed()[indices[2].toInt() - 1]
                    val altitude = calculateAltitude(
                        pressure = pressure.toDouble(),
                        referencePressure = valuesBelow.pressure,
                        referenceAltitude = valuesBelow.altitude,
                        referenceAirTemperature = valuesBelow.temperature
                    )
                    val gribVector =
                        gribMap!!.map[Pair(indices[0].toCoordinate(MIN_LATITUDE), indices[1].toCoordinate(MIN_LONGITUDE))]!![pressure.toInt()]!!

                    CartesianIsobaricValues(
                        altitude = altitude,
                        pressure = pressure.toDouble(),
                        temperature = gribVector.temperature.toDouble(),
                        windXComponent = gribVector.uComponentWind.toDouble(),
                        windYComponent = gribVector.vComponentWind.toDouble()
                    )
                }).also {
                    pointCache[indices] = it
                }
            )
        }

        if (indices[0] == -1) {
            val p1 = getPoint(intArrayOf(0, indices[1], indices[2]), time)
                .fold(
                    onSuccess = { it },
                    onFailure = { return Result.failure(it) }
                ).toRealVector()
            val p2 = getPoint(intArrayOf(1, indices[1], indices[2]), time)
                .fold(
                    onSuccess = { it },
                    onFailure = { return Result.failure(it) }
                ).toRealVector()

            val p0 = 2.0 * p1 - p2

            return Result.success(
                CartesianIsobaricValues(
                    altitude = p0[0],
                    pressure = p0[1],
                    temperature = p0[2],
                    windXComponent = p0[3],
                    windYComponent = p0[4]
                ).also {
                    pointCache[indices] = it
                }
            )
        }
        else if (indices[0] == maxLatIndex) {
            val p1 = getPoint(intArrayOf(maxLatIndex - 2, indices[1], indices[2]), time)
                .fold(
                    onSuccess = { it },
                    onFailure = { return Result.failure(it) }
                ).toRealVector()
            val p2 = getPoint(intArrayOf(maxLatIndex - 1, indices[1], indices[2]), time)
                .fold(
                    onSuccess = { it },
                    onFailure = { return Result.failure(it) }
                ).toRealVector()

            val p3 = 2.0 * p2 - p1

            return Result.success(
                CartesianIsobaricValues(
                    altitude = p3[0],
                    pressure = p3[1],
                    temperature = p3[2],
                    windXComponent = p3[3],
                    windYComponent = p3[4]
                ).also {
                    pointCache[indices] = it
                }
            )
        }

        if (indices[1] == -1) {
            val p1 = getPoint(intArrayOf(indices[0], 0, indices[2]), time)
                .fold(
                    onSuccess = { it },
                    onFailure = { return Result.failure(it) }
                ).toRealVector()
            val p2 = getPoint(intArrayOf(indices[0], 1, indices[2]), time)
                .fold(
                    onSuccess = { it },
                    onFailure = { return Result.failure(it) }
                ).toRealVector()

            val p0 = 2.0 * p1 - p2

            return Result.success(
                CartesianIsobaricValues(
                    altitude = p0[0],
                    pressure = p0[1],
                    temperature = p0[2],
                    windXComponent = p0[3],
                    windYComponent = p0[4]
                ).also {
                    pointCache[indices] = it
                }
            )
        }
        else if (indices[1] == maxLonIndex) {
            val p1 = getPoint(intArrayOf(indices[0], maxLonIndex - 2, indices[2]), time)
                .fold(
                    onSuccess = { it },
                    onFailure = { return Result.failure(it) }
                ).toRealVector()
            val p2 = getPoint(intArrayOf(indices[0], maxLonIndex - 1, indices[2]), time)
                .fold(
                    onSuccess = { it },
                    onFailure = { return Result.failure(it) }
                ).toRealVector()

            val p3 = 2.0 * p2 - p1

            return Result.success(
                CartesianIsobaricValues(
                    altitude = p3[0],
                    pressure = p3[1],
                    temperature = p3[2],
                    windXComponent = p3[3],
                    windYComponent = p3[4]
                ).also {
                    pointCache[indices] = it
                }
            )
        }

        if (indices[2] == -1) {
            val p1 = getPoint(intArrayOf(indices[0], indices[1], 0), time)
                .fold(
                    onSuccess = { it },
                    onFailure = { return Result.failure(it) }
                ).toRealVector()
            val p2 = getPoint(intArrayOf(indices[0], indices[1], 1), time)
                .fold(
                    onSuccess = { it },
                    onFailure = { return Result.failure(it) }
                ).toRealVector()

            val p0 = 2.0 * p1 - p2

            return Result.success(
                CartesianIsobaricValues(
                    altitude = p0[0],
                    pressure = p0[1],
                    temperature = p0[2],
                    windXComponent = p0[3],
                    windYComponent = p0[4]
                ).also {
                    pointCache[indices] = it
                }
            )
        }
        else if (indices[2] == layerPressureValues.size + 1) {
            val p1 = getPoint(intArrayOf(indices[0], indices[1], layerPressureValues.size - 1), time)
                .fold(
                    onSuccess = { it },
                    onFailure = { return Result.failure(it) }
                ).toRealVector()
            val p2 = getPoint(intArrayOf(indices[0], indices[1], layerPressureValues.size), time)
                .fold(
                    onSuccess = { it },
                    onFailure = { return Result.failure(it) }
                ).toRealVector()

            val p3 = 2.0 * p2 - p1

            return Result.success(
                CartesianIsobaricValues(
                    altitude = p3[0],
                    pressure = p3[1],
                    temperature = p3[2],
                    windXComponent = p3[3],
                    windYComponent = p3[4]
                ).also {
                    pointCache[indices] = it
                }
            )
        }

        return Result.success(
            getPointR(indices).fold(
                onSuccess = { it },
                onFailure = { return Result.failure(it) }
            )
        )
    }

    fun interpolatedSurface(points: Array<Array<CartesianIsobaricValues>>): (Double, Double) -> CartesianIsobaricValues {
        assert(points.size == 4) { "Need four sets of points to interpolate over" }
        points.forEach { assert(it.size == 4) { "Need four points to interpolate over" } }

        return { t0, t1 ->
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

            CartesianIsobaricValues(
                altitude = altitude,
                pressure = points[0][0].pressure,
                temperature = temperature,
                windXComponent = windX,
                windYComponent = windY
            )
        }
    }

    fun interpolate(coordinates: RealVector, surfaces: List<(Double, Double) -> CartesianIsobaricValues>): CartesianIsobaricValues {
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
            pressure = interpolatedVector[1],
            temperature = interpolatedVector[2],
            windXComponent = interpolatedVector[3],
            windYComponent = interpolatedVector[4]
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

    private fun Int.toCoordinate(lowerBound: Double) = this / RESOLUTION + lowerBound
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

    return c
}

