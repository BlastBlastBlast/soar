package no.uio.ifi.in2000.met2025.domain.helpers

import no.uio.ifi.in2000.met2025.data.models.Constants.Companion.AIR_TEMPERATURE_AT_SEA_LEVEL
import no.uio.ifi.in2000.met2025.data.models.Constants.Companion.GRAVITY
import no.uio.ifi.in2000.met2025.data.models.Constants.Companion.SPECIFIC_GAS_CONSTANT_FOR_DRY_AIR
import no.uio.ifi.in2000.met2025.data.models.Constants.Companion.TEMPERATURE_LAPSE_RATE
import kotlin.math.pow

fun calculateAltitude(pressure: Double, seaLevelPressure: Double): Double {

    val exponent = SPECIFIC_GAS_CONSTANT_FOR_DRY_AIR * TEMPERATURE_LAPSE_RATE / GRAVITY
    val altitude = (AIR_TEMPERATURE_AT_SEA_LEVEL / TEMPERATURE_LAPSE_RATE) *
            (1 - (pressure / seaLevelPressure).pow(exponent))

    return altitude
}