package no.uio.ifi.in2000.met2025.data.models

class Constants {
    companion object {
        val layerPressureValues = listOf(100, 150, 200, 225, 250, 275, 300, 350, 400, 450, 500, 600, 700, 750, 850)
        const val AIR_TEMPERATURE_AT_SEA_LEVEL = 288.15 // Standard temperature at sea level in Kelvin
        const val TEMPERATURE_LAPSE_RATE = 0.0065 // Standard temperature lapse rate in K/m
        const val SPECIFIC_GAS_CONSTANT_FOR_DRY_AIR = 287.0 // Specific gas constant for dry air in J/(kg·K)
        const val GRAVITY = 9.80665 // Acceleration due to gravity in m/s²
    }
}