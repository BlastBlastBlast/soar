package no.uio.ifi.in2000.met2025.domain.helpers

import no.uio.ifi.in2000.met2025.data.models.Constants.Companion.EARTH_AIR_MOLAR_MASS
import no.uio.ifi.in2000.met2025.data.models.Constants.Companion.GRAVITY
import no.uio.ifi.in2000.met2025.data.models.Constants.Companion.CELSIUS_TO_KELVIN
import no.uio.ifi.in2000.met2025.data.models.Constants.Companion.UNIVERSAL_GAS_CONSTANT
import no.uio.ifi.in2000.met2025.data.models.Constants.Companion.TEMPERATURE_LAPSE_RATE
import kotlin.math.pow

fun calculateAltitude(pressure: Double, referencePressure: Double, referenceAirTemperature: Double, referenceAltitude: Double): Double {

    val exponent = (UNIVERSAL_GAS_CONSTANT * TEMPERATURE_LAPSE_RATE / (GRAVITY * EARTH_AIR_MOLAR_MASS))
    val altitude = referenceAltitude + ((referenceAirTemperature + CELSIUS_TO_KELVIN )/ TEMPERATURE_LAPSE_RATE) *
            (1 - (pressure / referencePressure).pow(exponent))

    return altitude
}