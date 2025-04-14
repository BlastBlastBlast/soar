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
    RESOLUTION
}

// This model holds the default (or custom) parameter values as a map.
data class RocketParameterValues(val valueMap: Map<String, Double>)

fun getDefaultRocketParameterValues(): RocketParameterValues {
    val map = hashMapOf(
        RocketParameterType.APOGEE.name to 5000.0,
        RocketParameterType.LAUNCH_DIRECTION.name to 90.0,
        RocketParameterType.LAUNCH_ANGLE.name to 80.0,
        RocketParameterType.THRUST_NEWTONS.name to 4500.0,
        RocketParameterType.BURN_TIME.name to 12.0,
        RocketParameterType.DRY_WEIGHT.name to 100.0,
        RocketParameterType.WET_WEIGHT.name to 130.0,
        RocketParameterType.RESOLUTION.name to 1.0
    )
    return RocketParameterValues(map)
}

// Mapping function: converts the model values to a database entity.
fun mapToRocketConfig(
    name: String,
    values: RocketParameterValues,
    isDefault: Boolean = false
): RocketConfig {
    val map = values.valueMap
    return RocketConfig(
        name = name,
        apogee = map[RocketParameterType.APOGEE.name] ?: 0.0,
        launchDirection = map[RocketParameterType.LAUNCH_DIRECTION.name] ?: 0.0,
        launchAngle = map[RocketParameterType.LAUNCH_ANGLE.name] ?: 0.0,
        thrust = map[RocketParameterType.THRUST_NEWTONS.name] ?: 0.0,
        burnTime = map[RocketParameterType.BURN_TIME.name] ?: 0.0,
        dryWeight = map[RocketParameterType.DRY_WEIGHT.name] ?: 0.0,
        wetWeight = map[RocketParameterType.WET_WEIGHT.name] ?: 0.0,
        resolution = map[RocketParameterType.RESOLUTION.name] ?: 0.0,
        isDefault = isDefault
    )
}
