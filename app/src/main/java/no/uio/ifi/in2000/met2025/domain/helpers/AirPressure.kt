package no.uio.ifi.in2000.met2025.domain.helpers

import no.uio.ifi.in2000.met2025.data.models.Constants.Companion.EARTH_AIR_MOLAR_MASS
import no.uio.ifi.in2000.met2025.data.models.Constants.Companion.GRAVITY
import no.uio.ifi.in2000.met2025.data.models.Constants.Companion.CELSIUS_TO_KELVIN
import no.uio.ifi.in2000.met2025.data.models.Constants.Companion.UNIVERSAL_GAS_CONSTANT
import no.uio.ifi.in2000.met2025.data.models.Constants.Companion.TEMPERATURE_LAPSE_RATE
import kotlin.math.pow

fun calculatePressureAtAltitude(altitude: Double, referencePressure: Double, referenceAirTemperature: Double, referenceAltitude: Double = 0.0): Double {
    // probably not correct yet
    val exponent = (GRAVITY * EARTH_AIR_MOLAR_MASS / (UNIVERSAL_GAS_CONSTANT * TEMPERATURE_LAPSE_RATE))
    val pressure = referencePressure * (1 - (TEMPERATURE_LAPSE_RATE * (altitude - referenceAltitude)) / referenceAirTemperature).pow(exponent)

    return pressure
}