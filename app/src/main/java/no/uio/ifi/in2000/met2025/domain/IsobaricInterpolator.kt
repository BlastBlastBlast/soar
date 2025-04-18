package no.uio.ifi.in2000.met2025.domain

import no.uio.ifi.in2000.met2025.data.models.CartesianIsobaricValues
import no.uio.ifi.in2000.met2025.data.remote.forecast.LocationForecastRepository
import no.uio.ifi.in2000.met2025.data.remote.isobaric.IsobaricRepository
import org.jetbrains.kotlinx.multik.ndarray.data.D1Array

class IsobaricInterpolator(
    private val locationForecastRepository: LocationForecastRepository,
    private val isobaricRepository: IsobaricRepository
) {
    // FIXME: This function is a placeholder and should be implemented to return actual values.
    fun getCartesianIsobaricValues(position: D1Array<Double>/*, velocity: D1Array<Double>*/): CartesianIsobaricValues {
        return CartesianIsobaricValues(
            pressure = 0.0,
            altitude = 0.0,
            temperature = 0.0,
            windXComponent = 0.0,
            windYComponent = 0.0,
        )
    }
}