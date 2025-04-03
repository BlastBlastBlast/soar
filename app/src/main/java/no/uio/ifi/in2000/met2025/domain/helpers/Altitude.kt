package no.uio.ifi.in2000.met2025.domain.helpers

import no.uio.ifi.in2000.met2025.data.models.Constants
import kotlin.math.pow

fun calculateAltitude(pressure: Double, seaLevelPressure: Double): Double {

    val exponent = Constants.specificGasConstantForDryAir * Constants.temperatureLapseRate / Constants.gravity
    val altitude = (Constants.temperatureAtSeaLevel / Constants.temperatureLapseRate) *
            (1 - (pressure / seaLevelPressure).pow(exponent))

    return altitude
}