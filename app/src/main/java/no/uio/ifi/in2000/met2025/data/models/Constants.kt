package no.uio.ifi.in2000.met2025.data.models

class Constants {
    companion object {
        val layerPressureValues = listOf(100, 150, 200, 225, 250, 275, 300, 350, 400, 450, 500, 600, 700, 750, 850)
        val temperatureAtSeaLevel = 288.15 // Standard temperature at sea level in Kelvin
        val temperatureLapseRate = 0.0065 // Standard temperature lapse rate in K/m
        val specificGasConstantForDryAir = 287.0 // Specific gas constant for dry air in J/(kg·K)
        val gravity = 9.80665 // Acceleration due to gravity in m/s²
    }
}