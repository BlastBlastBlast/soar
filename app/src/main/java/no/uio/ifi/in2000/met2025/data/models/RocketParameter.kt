package no.uio.ifi.in2000.met2025.data.models

import no.uio.ifi.in2000.met2025.data.local.database.RocketConfig
import kotlin.math.PI
import kotlin.math.pow

// Parameter names aligned with TrajectoryCalculator inputs
enum class RocketParameterType {
    LAUNCH_AZIMUTH,                  // clockwise from north
    LAUNCH_PITCH,                    // elevation angle (upward = 90°)
    LAUNCH_RAIL_LENGTH,              // meters
    WET_MASS,                        // kg
    DRY_MASS,                        // kg
    BURN_TIME,                       // seconds
    THRUST,                          // newtons
    STEP_SIZE,                       // integration step (seconds)
    CROSS_SECTIONAL_AREA,            // m²
    DRAG_COEFFICIENT,                // unitless
    PARACHUTE_CROSS_SECTIONAL_AREA,  // m²
    PARACHUTE_DRAG_COEFFICIENT       // unitless
}

// Holds a map of parameter values
data class RocketParameterValues(val valueMap: Map<String, Double>)

// Default values matching previous defaults (with cross-sectional area computed from 0.2 m diameter)
fun getDefaultRocketParameterValues(): RocketParameterValues {
    val defaultDiameter = 0.2                          // meters
    val defaultArea = PI * (defaultDiameter / 2).pow(2)

    val map = hashMapOf(
        RocketParameterType.LAUNCH_AZIMUTH.name                   to 90.0,
        RocketParameterType.LAUNCH_PITCH.name                     to 80.0,
        RocketParameterType.LAUNCH_RAIL_LENGTH.name               to 1.0,
        RocketParameterType.WET_MASS.name                         to 130.0,
        RocketParameterType.DRY_MASS.name                         to 100.0,
        RocketParameterType.BURN_TIME.name                        to 12.0,
        RocketParameterType.THRUST.name                           to 4500.0,
        RocketParameterType.STEP_SIZE.name                        to 1.0,
        RocketParameterType.CROSS_SECTIONAL_AREA.name             to defaultArea,
        RocketParameterType.DRAG_COEFFICIENT.name                 to 0.75,
        RocketParameterType.PARACHUTE_CROSS_SECTIONAL_AREA.name   to 5.0,
        RocketParameterType.PARACHUTE_DRAG_COEFFICIENT.name       to 1.5
    )
    return RocketParameterValues(map)
}

// Map parameter values into the database model
fun mapToRocketConfig(
    name: String,
    values: RocketParameterValues,
    isDefault: Boolean = false
): RocketConfig {
    val m = values.valueMap
    return RocketConfig(
        name                         = name,
        launchAzimuth                = m[RocketParameterType.LAUNCH_AZIMUTH.name] ?: 0.0,
        launchPitch                  = m[RocketParameterType.LAUNCH_PITCH.name] ?: 0.0,
        launchRailLength             = m[RocketParameterType.LAUNCH_RAIL_LENGTH.name] ?: 0.0,
        wetMass                      = m[RocketParameterType.WET_MASS.name] ?: 0.0,
        dryMass                      = m[RocketParameterType.DRY_MASS.name] ?: 0.0,
        burnTime                     = m[RocketParameterType.BURN_TIME.name] ?: 0.0,
        thrust                       = m[RocketParameterType.THRUST.name] ?: 0.0,
        stepSize                     = m[RocketParameterType.STEP_SIZE.name] ?: 0.0,
        crossSectionalArea           = m[RocketParameterType.CROSS_SECTIONAL_AREA.name] ?: 0.0,
        dragCoefficient              = m[RocketParameterType.DRAG_COEFFICIENT.name] ?: 0.0,
        parachuteCrossSectionalArea  = m[RocketParameterType.PARACHUTE_CROSS_SECTIONAL_AREA.name] ?: 0.0,
        parachuteDragCoefficient     = m[RocketParameterType.PARACHUTE_DRAG_COEFFICIENT.name] ?: 0.0,
        isDefault                    = isDefault
    )
}