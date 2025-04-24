package no.uio.ifi.in2000.met2025.domain.helpers

import org.apache.commons.math3.linear.RealVector

// Addition operator
operator fun RealVector.plus(other: RealVector): RealVector = this.add(other)

// Subtraction operator
operator fun RealVector.minus(other: RealVector): RealVector = this.subtract(other)

// Scalar multiplication operator
operator fun RealVector.times(scalar: Double): RealVector = this.mapMultiply(scalar)

// Scalar division operator
operator fun RealVector.div(scalar: Double): RealVector = this.mapDivide(scalar)

// Negation operator
operator fun RealVector.unaryMinus(): RealVector = this.mapMultiply(-1.0)

// Dot product operator
operator fun RealVector.times(other: RealVector): Double = this.dotProduct(other)

operator fun RealVector.get(index: Int): Double = this.getEntry(index)

// Scalar multiplication operator
operator fun Double.times(vector: RealVector): RealVector = vector.mapMultiply(this)
