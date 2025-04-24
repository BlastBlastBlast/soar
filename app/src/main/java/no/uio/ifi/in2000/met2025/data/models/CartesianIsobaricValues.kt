package no.uio.ifi.in2000.met2025.data.models

import org.apache.commons.math3.linear.ArrayRealVector
import org.apache.commons.math3.linear.RealVector

data class CartesianIsobaricValues(
    val altitude: Double,
    val pressure: Double,
    val temperature: Double,
    val windXComponent: Double,
    val windYComponent: Double,
) {
    fun toRealVector(): RealVector = ArrayRealVector(doubleArrayOf(altitude, pressure, temperature, windXComponent, windYComponent))
}
