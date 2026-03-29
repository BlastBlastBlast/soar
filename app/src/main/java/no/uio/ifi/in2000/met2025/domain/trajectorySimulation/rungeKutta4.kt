package no.uio.ifi.in2000.met2025.domain.trajectorySimulation

import no.uio.ifi.in2000.met2025.domain.helpers.div
import no.uio.ifi.in2000.met2025.domain.helpers.plus
import no.uio.ifi.in2000.met2025.domain.helpers.times
import org.apache.commons.math3.linear.RealVector

/**
 * Runge-Kutta 4th order method for numerical integration.
 * @param initialVector The initial vector (for instance position or velocity).
 * @param time The current time as a number.
 * @param stepSize The size of the time step.
 * @param derivative A function that calculates the derivative at a given time and vector.
 * For instance, if the vector is a position, the derivative will be the velocity.
 * @return The updated vector after applying the Runge-Kutta method.
 */
fun rungeKutta4(
    initialVector: RealVector,
    time: Double,
    stepSize: Double,
    derivative: (Double, RealVector) -> RealVector,
): RealVector {
    val k1 = derivative(time, initialVector)
    val k2 = derivative(time + stepSize / 2, initialVector + k1 * stepSize / 2.0)
    val k3 = derivative(time + stepSize / 2, initialVector + k2 * stepSize / 2.0)
    val k4 = derivative(time + stepSize, initialVector + k3 * stepSize)

    return initialVector + (k1 + 2.0 * k2 + 2.0 * k3 + k4) * stepSize / 6.0
}