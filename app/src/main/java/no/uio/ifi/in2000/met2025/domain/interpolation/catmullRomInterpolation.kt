package no.uio.ifi.in2000.met2025.domain.interpolation

import android.util.Log
import no.uio.ifi.in2000.met2025.domain.helpers.div
import no.uio.ifi.in2000.met2025.domain.helpers.get
import no.uio.ifi.in2000.met2025.domain.helpers.plus
import no.uio.ifi.in2000.met2025.domain.helpers.times
import org.apache.commons.math3.linear.RealVector

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

    Log.v("IsobaricInterpolator", "catmullRomInterpolation: t = $t, points = $points, c = $c")

    return c
}