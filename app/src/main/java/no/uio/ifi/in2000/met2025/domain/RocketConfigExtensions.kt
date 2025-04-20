package no.uio.ifi.in2000.met2025.domain

// -------------------------------
// domain/DbRocketConfigExtensions.kt
// -------------------------------
import no.uio.ifi.in2000.met2025.data.local.database.RocketConfig as DbRocketConfig
import kotlin.math.PI
import kotlin.math.pow

/** Map DB entity -> SimulationConfig */
fun DbRocketConfig.toSimulationConfig(): SimulationConfig {
    val area = PI * (bodyDiameter / 2).pow(2)
    val thrustCurve: (Double) -> Double = { t -> if (t <= burnTime) thrust else 0.0 }
    return SimulationConfig(
        mass = wetWeight,
        crossSectionArea = area,
        dragCoefficient = dragCoefficient,
        parachuteArea = parachuteArea,
        parachuteDragCoefficient = parachuteDragCoefficient,
        thrustCurve = thrustCurve,
        timeStep = resolution
    )
}
