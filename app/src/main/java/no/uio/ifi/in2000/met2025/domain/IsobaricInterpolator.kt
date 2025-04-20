package no.uio.ifi.in2000.met2025.domain

import no.uio.ifi.in2000.met2025.data.models.CartesianIsobaricValues
import no.uio.ifi.in2000.met2025.data.models.Vector3D
import no.uio.ifi.in2000.met2025.data.remote.forecast.LocationForecastRepository
import no.uio.ifi.in2000.met2025.data.remote.isobaric.IsobaricRepository
import org.apache.commons.math3.linear.RealVector

class IsobaricInterpolator(
    private val locationForecastRepository: LocationForecastRepository,
    private val isobaricRepository: IsobaricRepository
) {
    // FIXME: This function is a placeholder and should be implemented to return actual values.
    fun getCartesianIsobaricValues(position: RealVector/*, velocity: D1Array<Double>*/): CartesianIsobaricValues {
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
}