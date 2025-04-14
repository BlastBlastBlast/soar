package no.uio.ifi.in2000.met2025.data.models

enum class RocketParameter {
    APOGEE,
    LAUNCH_DIRECTION,
    LAUNCH_ANGLE,
    THRUST_NEWTONS,
    BURN_TIME,
    DRY_WEIGHT,
    WET_WEIGHT,
    RESOLUTION
}

data class RocketParameters(val valueMap: Map<String, Double>)

fun getDefaultRocketParameters(): RocketParameters {
    val map = hashMapOf(
        RocketParameter.APOGEE.name to 5000.0,
        RocketParameter.LAUNCH_DIRECTION.name to 90.0,
        RocketParameter.LAUNCH_ANGLE.name to 80.0,
        RocketParameter.THRUST_NEWTONS.name to 4500.0,
        RocketParameter.BURN_TIME.name to 12.0,
        RocketParameter.DRY_WEIGHT.name to 100.0,
        RocketParameter.WET_WEIGHT.name to 130.0,
        RocketParameter.RESOLUTION.name to 1.0
    )
    return RocketParameters(map)
}

data class RocketSpecs(
    val id: Int = 0,
    val name: String,
    val apogee: Double,
    val launchDirection: Double,
    val launchAngle: Double,
    val thrust: Double,
    val burnTime: Double,
    val dryWeight: Double,
    val wetWeight: Double,
    val resolution: Double,
    val isDefault: Boolean = false
)

fun mapToDatabaseObject(
    name: String,
    values: RocketParameters,
    isDefault: Boolean = false
): RocketSpecs {
    val map = values.valueMap
    return RocketSpecs(
        name = name,
        apogee = map[RocketParameter.APOGEE.name] ?: 0.0,
        launchDirection = map[RocketParameter.LAUNCH_DIRECTION.name] ?: 0.0,
        launchAngle = map[RocketParameter.LAUNCH_ANGLE.name] ?: 0.0,
        thrust = map[RocketParameter.THRUST_NEWTONS.name] ?: 0.0,
        burnTime = map[RocketParameter.BURN_TIME.name] ?: 0.0,
        dryWeight = map[RocketParameter.DRY_WEIGHT.name] ?: 0.0,
        wetWeight = map[RocketParameter.WET_WEIGHT.name] ?: 0.0,
        resolution = map[RocketParameter.RESOLUTION.name] ?: 0.0,
        isDefault = isDefault
    )
}