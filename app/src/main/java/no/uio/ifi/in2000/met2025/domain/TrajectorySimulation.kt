package no.uio.ifi.in2000.met2025.domain

// -------------------------------
// domain/TrajectorySimulation.kt
// -------------------------------

import no.uio.ifi.in2000.met2025.data.models.isobaric.IsobaricData

private const val G = 9.80665
private const val R_SPECIFIC = 287.05

/** Pull and interpolate wind from isobaric layers */
fun interpolateWind(data: IsobaricData, z: Double): Vec3 {
    val layers = data.valuesAtLayer.values.sortedBy { it.altitude }
    val lo = layers.last { it.altitude <= z }
    val hi = layers.first { it.altitude >= z }
    val f = ((z - lo.altitude) / (hi.altitude - lo.altitude)).coerceIn(0.0, 1.0)
    val speed = lo.windSpeed + (hi.windSpeed - lo.windSpeed) * f
    val dirFrom = lo.windFromDirection + (hi.windFromDirection - lo.windFromDirection) * f
    val theta = Math.toRadians((dirFrom + 180) % 360)
    return Vec3(speed * kotlin.math.sin(theta), speed * kotlin.math.cos(theta), 0.0)
}

/** Compute air density via ideal gas from interpolated layer values */
fun airDensityAt(data: IsobaricData, z: Double): Double {
    val entries = data.valuesAtLayer.entries.sortedBy { it.value.altitude }
    val (p1, v1) = entries.last { it.value.altitude <= z }
    val (p2, v2) = entries.first { it.value.altitude >= z }
    val f = ((z - v1.altitude) / (v2.altitude - v1.altitude)).coerceIn(0.0, 1.0)
    val pressurePa = (p1 * 100) + (p2 * 100 - p1 * 100) * f
    val tempK = (v1.airTemperature + 273.15) + (v2.airTemperature - v1.airTemperature) * f
    return pressurePa / (R_SPECIFIC * tempK)
}

/** Euler integration from launch until landing */
fun simulateTrajectory(
    config: SimulationConfig,
    windData: IsobaricData,
    originLat: Double,
    originLon: Double
): List<Point3D> {
    val pts = mutableListOf<Point3D>()
    var t = 0.0
    var pos = Vec3(0.0, 0.0, 0.0)
    var vel = Vec3(0.0, 0.0, 0.0)
    var deployed = false

    while (pos.z >= 0.0 || t == 0.0) {
        val wind = interpolateWind(windData, pos.z)
        val rho = airDensityAt(windData, pos.z)
        val vRel = vel - wind
        val vMag = vRel.length()

        if (!deployed && vel.z <= 0 && t > 0) deployed = true

        val area = if (deployed) config.parachuteArea else config.crossSectionArea
        val cd = if (deployed) config.parachuteDragCoefficient else config.dragCoefficient
        val fDrag = 0.5 * rho * vMag * vMag * cd * area
        val drag = vRel.normalize() * -fDrag
        val grav = Vec3(0.0, 0.0, -config.mass * G)
        val thrustMag = config.thrustCurve(t)
        val thrust = Vec3(0.0, 0.0, thrustMag)
        val acc = (thrust + drag + grav) / config.mass

        vel = vel + acc * config.timeStep
        pos = pos + vel * config.timeStep
        t += config.timeStep

        pts += Point3D(pos.x, pos.y, pos.z, t, deployed)
        if (deployed && pos.z <= 0) break
    }
    return pts
}