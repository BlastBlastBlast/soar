package no.uio.ifi.in2000.met2025.domain.helpers

import no.uio.ifi.in2000.met2025.data.models.Constants
import kotlin.math.pow

fun calculatePressureAtAltitude(altitude: Double, seaLevelPressure: Double): Double {

    val exponent = (Constants.gravity / (Constants.specificGasConstantForDryAir * Constants.temperatureLapseRate))
    val pressure = seaLevelPressure * (1 - (Constants.temperatureLapseRate * altitude) / Constants.temperatureAtSeaLevel).pow(
        exponent
    )

    return pressure
}