package no.uio.ifi.in2000.met2025.domain.helpers

import android.util.Log
import no.uio.ifi.in2000.met2025.data.models.Constants.Companion.EARTH_AIR_MOLAR_MASS
import no.uio.ifi.in2000.met2025.data.models.Constants.Companion.GRAVITY
import no.uio.ifi.in2000.met2025.data.models.Constants.Companion.CELSIUS_TO_KELVIN
import no.uio.ifi.in2000.met2025.data.models.Constants.Companion.UNIVERSAL_GAS_CONSTANT
import no.uio.ifi.in2000.met2025.data.models.Constants.Companion.TEMPERATURE_LAPSE_RATE
import kotlin.math.ln
import kotlin.math.pow

fun calculateAltitude(pressure: Double, referencePressure: Double, referenceAirTemperature: Double, referenceAltitude: Double): Double {
    // expects temperature in Kelvin
    Log.i("calculateAltitude:", "pressure = $pressure, referencePressure = $referencePressure, referenceAirTemperature = ${(referenceAirTemperature - CELSIUS_TO_KELVIN).roundToDecimals(2)}, referenceAltitude = $referenceAltitude")

    return referenceAltitude + if (referenceAltitude < 10000) { // barometric formula for altitudes below 10 km
        val exponent =
            (UNIVERSAL_GAS_CONSTANT * TEMPERATURE_LAPSE_RATE) /
                (GRAVITY * EARTH_AIR_MOLAR_MASS)

        (referenceAirTemperature / TEMPERATURE_LAPSE_RATE) *
                (1 - (pressure / referencePressure).pow(exponent))
    } else { // above 10 km, the temperature lapse rate is assumed to be constant
        ln(referencePressure / pressure) *
                (UNIVERSAL_GAS_CONSTANT * referenceAirTemperature) /
                (GRAVITY * EARTH_AIR_MOLAR_MASS)
    }
}