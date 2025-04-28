package no.uio.ifi.in2000.met2025.domain.helpers

import org.apache.commons.math3.linear.Array2DRowRealMatrix
import org.apache.commons.math3.linear.RealMatrix
import org.apache.commons.math3.linear.RealVector

// Addition operator
operator fun RealVector.plus(other: RealVector): RealVector = add(other)

// Subtraction operator
operator fun RealVector.minus(other: RealVector): RealVector = subtract(other)

// Scalar multiplication operator
operator fun RealVector.times(scalar: Double): RealVector = mapMultiply(scalar)

// Scalar division operator
operator fun RealVector.div(scalar: Double): RealVector = mapDivide(scalar)

// Negation operator
operator fun RealVector.unaryMinus(): RealVector = mapMultiply(-1.0)

// Dot product operator
operator fun RealVector.times(other: RealVector): Double = dotProduct(other)

// vector element access operator
operator fun RealVector.get(index: Int): Double = getEntry(index)

// Scalar multiplication operator
operator fun Double.times(vector: RealVector): RealVector = vector.mapMultiply(this)

// matrix row vector access
operator fun RealMatrix.get(index: Int): RealVector = getRowVector(index)

// matrix element access
operator fun RealMatrix.get(row: Int, column: Int): Double = getEntry(row, column)

// matrix vector multiplication operator
operator fun RealMatrix.times(vector: RealVector): RealVector = operate(vector)

// vector matrix multiplication operator
operator fun RealVector.times(matrix: RealMatrix): RealVector = matrix.operate(this)

// matrix multiplication operator
operator fun RealMatrix.times(other: RealMatrix): RealMatrix = multiply(other)