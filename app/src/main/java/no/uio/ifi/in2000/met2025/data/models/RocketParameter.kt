package no.uio.ifi.in2000.met2025.data.models

import no.uio.ifi.in2000.met2025.data.local.database.RocketConfig

enum class RocketParameterType {
    APOGEE,
    LAUNCH_DIRECTION,
    LAUNCH_ANGLE,
    THRUST_NEWTONS,
    BURN_TIME,
    DRY_WEIGHT,
    WET_WEIGHT,
    RESOLUTION,
    BODY_DIAMETER,              // meters
    DRAG_COEFFICIENT,           // unitless
    PARACHUTE_AREA,             // m²
    PARACHUTE_DRAG_COEFFICIENT  // unitless
}

// This model holds the default (or custom) parameter values as a map.
data class RocketParameterValues(val valueMap: Map<String, Double>)

fun getDefaultRocketParameterValues(): RocketParameterValues {
    val map = hashMapOf(
        RocketParameterType.APOGEE.name                  to 5000.0,
        RocketParameterType.LAUNCH_DIRECTION.name        to 90.0,
        RocketParameterType.LAUNCH_ANGLE.name            to 80.0,
        RocketParameterType.THRUST_NEWTONS.name          to 4500.0,
        RocketParameterType.BURN_TIME.name               to 12.0,
        RocketParameterType.DRY_WEIGHT.name              to 100.0,
        RocketParameterType.WET_WEIGHT.name              to 130.0,
        RocketParameterType.RESOLUTION.name              to 1.0,
        RocketParameterType.BODY_DIAMETER.name           to 0.2,   // default 20 cm
        RocketParameterType.DRAG_COEFFICIENT.name        to 0.75,  // typical nose‑cone
        RocketParameterType.PARACHUTE_AREA.name          to 5.0,   // canopy area
        RocketParameterType.PARACHUTE_DRAG_COEFFICIENT.name to 1.5 // typical chute
    )
    return RocketParameterValues(map)
}

fun mapToRocketConfig(
    name: String,
    values: RocketParameterValues,
    isDefault: Boolean = false
): RocketConfig {
    val m = values.valueMap
    return RocketConfig(
        name                     = name,
        apogee                   = m[RocketParameterType.APOGEE.name] ?: 0.0,
        launchDirection          = m[RocketParameterType.LAUNCH_DIRECTION.name] ?: 0.0,
        launchAngle              = m[RocketParameterType.LAUNCH_ANGLE.name] ?: 0.0,
        thrust                   = m[RocketParameterType.THRUST_NEWTONS.name] ?: 0.0,
        burnTime                 = m[RocketParameterType.BURN_TIME.name] ?: 0.0,
        dryWeight                = m[RocketParameterType.DRY_WEIGHT.name] ?: 0.0,
        wetWeight                = m[RocketParameterType.WET_WEIGHT.name] ?: 0.0,
        resolution               = m[RocketParameterType.RESOLUTION.name] ?: 0.0,
        bodyDiameter             = m[RocketParameterType.BODY_DIAMETER.name] ?: 0.2,
        dragCoefficient          = m[RocketParameterType.DRAG_COEFFICIENT.name] ?: 0.75,
        parachuteArea            = m[RocketParameterType.PARACHUTE_AREA.name] ?: 5.0,
        parachuteDragCoefficient = m[RocketParameterType.PARACHUTE_DRAG_COEFFICIENT.name] ?: 1.5,
        isDefault                = isDefault
    )
}