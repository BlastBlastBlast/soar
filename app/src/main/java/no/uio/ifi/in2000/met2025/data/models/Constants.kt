package no.uio.ifi.in2000.met2025.data.models

class Constants {
    companion object {
        val layerPressureValues = listOf(100, 150, 200, 225, 250, 275, 300, 350, 400, 450, 500, 600, 700, 750, 850)
        //const val AIR_TEMPERATURE_AT_SEA_LEVEL = 288.15 // Standard temperature at sea level in Kelvin
        const val TEMPERATURE_LAPSE_RATE = 0.0065 // Standard temperature lapse rate in K/m
        const val UNIVERSAL_GAS_CONSTANT = 8.3144598 // 287.0 // Specific gas constant for dry air in J/(kg·K)
        const val GRAVITY = 9.80665 // Acceleration due to gravity in m/s²
        const val EARTH_AIR_MOLAR_MASS = 0.0289644 // kg/mol
        const val CELSIUS_TO_KELVIN = 273.15 // Conversion factor from Kelvin to Celsius

        const val CAUTION_THRESHOLD = 0.9 // Threshold for caution
        const val UNSAFE_THRESHOLD = 1.1 // Threshold for unsafe conditions
    }
}