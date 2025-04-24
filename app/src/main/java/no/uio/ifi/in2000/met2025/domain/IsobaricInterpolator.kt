package no.uio.ifi.in2000.met2025.domain

import com.mapbox.maps.extension.style.expressions.dsl.generated.length
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


class IsobaricInterpolator(
    private val locationForecastRepository: LocationForecastRepository,
    private val isobaricRepository: IsobaricRepository
) {
    var gribMap: GribDataMap? = null

    // key is [lat, lon, pressure]
    var pointCache: MutableMap<RealVector, CartesianIsobaricValues> = mutableMapOf()

    // key is [lat, lon, pressure]
    var surfaceCache: MutableMap<RealVector, (RealVector) -> CartesianIsobaricValues> = mutableMapOf()

    var lastVisitedGridIndex: RealVector? = null

    // FIXME: This function is a placeholder and should be implemented to return actual values.
    fun getCartesianIsobaricValues(position: RealVector, time: Instant): Result<CartesianIsobaricValues> {
        if (gribMap == null) {
            val gribDataResult = isobaricRepository.getGribData()

            when (gribDataResult) {
                is GribDataResult.Success -> {
                    gribMap = gribDataResult.gribDataMap.map
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

        fun pressureIndex(lastPressureIndex: Double): Double {
            // attempt to fetch the surface at latIndex, lonIndex and lastAltitudeIndex from the cache
            // if it does not exist, attempt to fetch the four points at latIndex (+1), lonIndex (+1) and lastAltitudeIndex from the cache
            // for each point that does not exist in the cache, check the cache for the point below it
            fun getValuesAtPoint(indices: RealVector): Result<CartesianIsobaricValues> {
                return if (pointCache.containsKey(indices)) {
                    pointCache[indices]!!
                } else if (indices[2] == 0) {
                        val forecastData = locationForecastRepository.getForecastData(
                            lat = indices[0].toCoordinate(MIN_LATITUDE),
                            lon = indices[1].toCoordinate(MIN_LONGITUDE),
                            time = time,
                            cacheResponse = false
                        ).fold(
                            onSuccess = { it },
                            onFailure = { return Result.failure(it) }
                        )

                        val forecastDataValues = forecastData.properties.timeseries[0].values

                        val airTemperatureAtSeaLevel = forecastValues.airTemperature - forecastData.altitude * TEMPERATURE_LAPSE_RATE + CELSIUS_TO_KELVIN //in Kelvin

                        val groundPressure = calculatePressureAtAltitude(
                            altitude = forecastData.altitude,
                            referencePressure = forecastItem.values.airPressureAtSeaLevel,
                            referenceAirTemperature = airTemperatureAtSeaLevel
                        )

                        val values = CartesianIsobaricValues(
                            altitude = forecastData.altitude,
                            pressure = groundPressure,
                            temperature = forecastDataValues.airTemperature,
                            windXComponent = forecastDataValues.windSpeed10mU,
                            windYComponent = forecastDataValues.windSpeed10mV
                        )

                        pointCache[indices] = values

                        Result.Success(values)
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
                        val altitude = calculateAltitudeAtPressure(
                            pressure = pressure,
                            referencePressure = valuesBelow.pressure,
                            referenceAltitude = valuesBelow.altitude,
                            referenceTemperature = valuesBelow.temperature
                        )
                        val gribVector = gribMap!![indices[0], indices[1]][pressure.toInt()]

                        val values = cartesianIsobaricValues = CartesianIsobaricValues(
                            altitude = altitude,
                            pressure = pressure,
                            temperature = gribVector.temperature,
                            windXComponent = gribVector.uComponentWind,
                            windYComponent = gribVector.vComponentWind
                        )

                        pointCache[indices] = values

                        Result.Success(values)
                    }
                }
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

        //

        return CartesianIsobaricValues(
            pressure = 0.0,
            altitude = 0.0,
            temperature = 0.0,
            windXComponent = 0.0,
            windYComponent = 0.0,
        )
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

