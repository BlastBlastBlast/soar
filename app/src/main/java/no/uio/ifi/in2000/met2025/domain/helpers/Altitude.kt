package no.uio.ifi.in2000.met2025.domain.helpers

import android.util.Log
import no.uio.ifi.in2000.met2025.data.models.Constants.Companion.EARTH_AIR_MOLAR_MASS
import no.uio.ifi.in2000.met2025.data.models.Constants.Companion.GRAVITY
import no.uio.ifi.in2000.met2025.data.models.Constants.Companion.CELSIUS_TO_KELVIN
import no.uio.ifi.in2000.met2025.data.models.Constants.Companion.UNIVERSAL_GAS_CONSTANT
import no.uio.ifi.in2000.met2025.data.models.Constants.Companion.TEMPERATURE_LAPSE_RATE
import kotlin.math.ln
import kotlin.math.pow

/**
 * Uses the barometric formula to calculate the altitude based on the pressure, reference pressure, reference air temperature, and reference altitude.
 * Expects temperature in Kelvin.
 * Above 10 km, the formula changes to a different form where temperature is assumed to be constant.
 */
fun calculateAltitude(
    pressure: Double,
    referencePressure: Double,
    referenceAirTemperature: Double,
    referenceAltitude: Double
): Double {
    return referenceAltitude + if (referenceAltitude < 10000) {
        val exponent =
            (UNIVERSAL_GAS_CONSTANT * TEMPERATURE_LAPSE_RATE) /
                (GRAVITY * EARTH_AIR_MOLAR_MASS)

        (referenceAirTemperature / TEMPERATURE_LAPSE_RATE) *
                (1 - (pressure / referencePressure).pow(exponent))
    } else {
        ln(referencePressure / pressure) *
                (UNIVERSAL_GAS_CONSTANT * referenceAirTemperature) /
                (GRAVITY * EARTH_AIR_MOLAR_MASS)
    }
}