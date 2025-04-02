package no.uio.ifi.in2000.met2025.domain.helpers

import no.uio.ifi.in2000.met2025.data.models.Constants

fun calculateAltitude(pressure: Double, seaLevelPressure: Double = 101325.0): Double {

    val altitude = (Constants.temperatureAtSeaLevel / Constants.temperatureLapseRate) *
            (1 - Math.pow(pressure / seaLevelPressure, Constants.specificGasConstantForDryAir * Constants.temperatureLapseRate / Constants.gravity))

    return altitude
}