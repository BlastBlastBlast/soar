package no.uio.ifi.in2000.met2025.domain

import com.mapbox.maps.extension.style.expressions.dsl.generated.length
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
import org.apache.commons.math3.linear.ArrayRealVector
import no.uio.ifi.in2000.met2025.data.models.Constants
import no.uio.ifi.in2000.met2025.data.models.Constants.Companion.CELSIUS_TO_KELVIN
import no.uio.ifi.in2000.met2025.data.models.Constants.Companion.TEMPERATURE_LAPSE_RATE
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

    // key is [lat, lon, pressure]
    val pointCache: MutableMap<RealVector, CartesianIsobaricValues> = mutableMapOf()

    // key is [lat, lon, pressure]
    val surfaceCache: MutableMap<RealVector, (RealVector) -> CartesianIsobaricValues> = mutableMapOf()

    var lastVisitedGridIndex: RealVector? = null

    // FIXME: This function is a placeholder and should be implemented to return actual values.
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

        val lat = position[0]
        val lon = position[1]

        assert(isWithinBounds(lat, lon)) { "Coordinates are outside the permitted bounds" }

        val latIndex = lat.toGridIndex(MIN_LATITUDE)
        val lonIndex = lon.toGridIndex(MIN_LONGITUDE)

        val altitudeIndexToTry = if (lastVisitedGridIndex == null) {
            0.0
        }  else {
            lastVisitedGridIndex!![2]
        }

        val pressureValues = (Constants.layerPressureValues).reversed()
        val pressure = pressureValues[lastVisitedGridIndex!![2].toInt()]

        fun validPressureIndex(initialPressureIndex: Double): Double {
            // attempt to fetch the surface at latIndex, lonIndex and lastAltitudeIndex from the cache
            // if it does not exist, attempt to fetch the four points at latIndex (+1), lonIndex (+1) and lastAltitudeIndex from the cache
            // for each point that does not exist in the cache, check the cache for the point below it
            suspend fun getValuesAtPoint(indices: RealVector): Result<CartesianIsobaricValues> {
                return if (pointCache.containsKey(indices)) {
                    Result.success(pointCache[indices]!!)
                } else if (indices[2] == 0.0) {
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

                        val values = CartesianIsobaricValues(
                            altitude = forecastData.altitude,
                            pressure = groundPressure,
                            temperature = forecastDataValues.airTemperature,
                            windXComponent = cos(windFromDirection) * windSpeed,
                            windYComponent = sin(windFromDirection) * windSpeed
                        )

                        pointCache[indices] = values

                        Result.success(values)
                    } else {
                        val valuesBelow = getValuesAtPoint(
                            ArrayRealVector(
                                doubleArrayOf(
                                    indices[0],
                                    indices[1],
                                    indices[2] - 1
                                )
                            )
                        ).fold(
                            onSuccess = { it },
                            onFailure = { return Result.failure(it) }
                        )
                        val pressure = (Constants.layerPressureValues).reversed()[indices[2].toInt() - 1]
                        val altitude = calculateAltitude(
                            pressure = pressure.toDouble(),
                            referencePressure = valuesBelow.pressure,
                            referenceAltitude = valuesBelow.altitude,
                            referenceAirTemperature = valuesBelow.temperature
                        )
                        val gribVector =
                            gribMap!!.map[Pair(indices[0].toCoordinate(MIN_LATITUDE), indices[1].toCoordinate(MIN_LONGITUDE))]!![pressure.toInt()]!!

                        val values = CartesianIsobaricValues(
                            altitude = altitude,
                            pressure = pressure.toDouble(),
                            temperature = gribVector.temperature.toDouble(),
                            windXComponent = gribVector.uComponentWind.toDouble(),
                            windYComponent = gribVector.vComponentWind.toDouble()
                        )

                        pointCache[indices] = values

                        Result.success(values)
                    }
                }

            }

            return Result.success(
                CartesianIsobaricValues(
                    pressure = 0.0,
                    altitude = 0.0,
                    temperature = 0.0,
                    windXComponent = 0.0,
                    windYComponent = 0.0,
                )
            )
        }

    fun interpolatedSurface(points: Array<Array<CartesianIsobaricValues>>): (RealVector) -> CartesianIsobaricValues {
        assert(points.size == 4) { "Need four sets of points to interpolate over" }
        points.forEach { assert(it.size == 4) { "Need four points to interpolate over" } }

        return { fractionalParts ->
            assert(fractionalParts.dimension == 2) { "Need two fractional parts to interpolate over" }
            assert(fractionalParts[0] in 0.0..1.0) { "Latitude fractional part out of bounds" }
            assert(fractionalParts[1] in 0.0..1.0) { "Longitude fractional part out of bounds" }

            val t0 = fractionalParts[0]
            val t1 = fractionalParts[1]
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

        //strategy:
        //check cache for spline solid containing position
            // if found, return interpolated values
            // if not found, check for up to 4 spline surfaces in x,y and z directions
            // store the locations of the missing surfaces in some structure
                // choose the direction with the most splines to interpolate solid from
                // for each spline surface not present in cache:
                    // check for spline curves along the two directions parallel to the surface (the last direction is the one we checked for the surface along)
                    // choose the direction with the most spline curves to interpolate the surface from
                    // for each spline curve not present in cache:
                        // check for the four points to interpolate the curve from along the direction of the curve
                        // for each point not present in cache:
                            // check cache for points downward in the z direction
                            // if a point is found, use the altitude and pressure values at that point to calculate the altitude at the missing point above, cache the point,
                            // and continue calculating the altitude value of the next point using the pressure and altitude values of the previous point, caching all these points,
                            // until the point we want to interpolate from is reached
                        // when all four points are calculated and cached, use the four points to calculate the curve and cache it
                        // delete the two points overlapping the curve from the cache
                    // when all curves are calculated and cached, use the curves to calculate the surface and cache it
                    // delete the two curves overlapping the solid from the cache
                // when all surfaces are calculated and cached, use the surfaces to calculate the solid and cache it
                // delete the two surfaces overlapping the solid from the cache
                // for the two remaining directions:
                    // for the location of each missing surface

        // to check if a point lies within a cached solid:
        // check if there is a solid that the lat and lon of the point lies within
        // if such a solid exists, check if the altitude of the point lies within the min and max altitude of the solid at the point's lat and lon
        // if the point lies within the solid, return the interpolated values of the solid at the point
        // if the altitude of the point is above the max altitude of the solid, check if there is a solid above the point,
        // and continue until we reach a solid that contains the point or until there are no more solids higher up
        // similar for below
        // if there are no solids that contain the point's lat and lon, check for surfaces as described above
    }

    fun interpolate(coordinates: RealVector, surfaces: List<(RealVector) -> CartesianIsobaricValues>): CartesianIsobaricValues {
        assert(coordinates.dimension == 3) { "Not a coordinate vector of dimension 3" }
        assert(surfaces.size == 4) { "Need four surfaces to interpolate over" }

        val horizontalVector = coordinates.getSubVector(0, 2)
        val fractionalParts = horizontalVector.toGridFractionalParts()

        val splinePoints = surfaces.map{ it(fractionalParts).toRealVector() }
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

    private fun RealVector.isInLastVisitedArea(): Boolean = (toGridIndices() == lastVisitedGridIndex?.getSubVector(0,2))

    private fun RealVector.toGridIndices(): RealVector {
        val lat = this[0]
        val lon = this[1]

        assert(dimension == 2) { "Not a coordinate vector of dimension 2" }
        assert(isWithinBounds(lat, lon)) { "Coordinates out of bounds" }

        val latIndex = lat.toGridIndex(MIN_LATITUDE)
        val lonIndex = lon.toGridIndex(MIN_LONGITUDE)

        return ArrayRealVector(doubleArrayOf(latIndex, lonIndex))
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

    private fun Double.toGridIndex(lowerBound: Double) = toGridValue(lowerBound).toInt().toDouble()

    private fun Double.toCoordinate(lowerBound: Double) = this / RESOLUTION + lowerBound
}

/**
 * Computes Catmull-Rom interpolation at a value t given 4 control points
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
    val (p0, p1, p2, p3) = points.map { it.getSubVector(1,it.dimension - 1) }

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

