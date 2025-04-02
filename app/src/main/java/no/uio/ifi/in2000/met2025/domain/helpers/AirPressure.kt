package no.uio.ifi.in2000.met2025.domain.helpers

import no.uio.ifi.in2000.met2025.data.models.Constants

fun calculatePressureAtAltitude(altitude: Double, seaLevelPressure: Double = 101325.0): Double {

    val exponent = (Constants.gravity / (Constants.specificGasConstantForDryAir * Constants.temperatureLapseRate))
    val pressure = seaLevelPressure * Math.pow(1 - (Constants.temperatureLapseRate * altitude) / Constants.temperatureAtSeaLevel, exponent)

    return pressure
}