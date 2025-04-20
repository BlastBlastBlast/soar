package no.uio.ifi.in2000.met2025.domain
import kotlin.math.PI
import kotlin.math.pow

/**
 * Domain model for simulation parameters.
 * Renamed to avoid collision with DB RocketConfig.
 */
data class SimulationConfig(
    val mass: Double,
    val crossSectionArea: Double,
    val dragCoefficient: Double,
    val parachuteArea: Double,
    val parachuteDragCoefficient: Double,
    val thrustCurve: (Double) -> Double,
    val timeStep: Double
)

/** 3D vector for physics math */
data class Vec3(val x: Double, val y: Double, val z: Double) {
    operator fun plus(o: Vec3) = Vec3(x + o.x, y + o.y, z + o.z)
    operator fun minus(o: Vec3) = Vec3(x - o.x, y - o.y, z - o.z)
    operator fun times(s: Double) = Vec3(x * s, y * s, z * s)
    operator fun div(s: Double) = Vec3(x / s, y / s, z / s)
    fun length() = kotlin.math.sqrt(x * x + y * y + z * z)
    fun normalize() = this / (length().takeIf { it > 0 } ?: 1.0)
}

/** Domain Point3D, annotated with time and parachute flag */
class Point3D(
    private val x: Double,
    private val y: Double,
    private val z: Double,
    private val timeSeconds: Double,
    private val parachuted: Boolean = false
) {
    fun getX() = x
    fun getY() = y
    fun getZ() = z
    fun isParachuted() = parachuted
    override fun toString() =
        "(x: ${x.toInt()}, y: ${y.toInt()}, z: ${z.toInt()}, t: ${timeSeconds.toInt()}, parachuted=$parachuted)"
}
