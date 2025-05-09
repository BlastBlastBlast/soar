package no.uio.ifi.in2000.met2025.ui.screens.config

sealed class ConfigType(val route: String, val label: String) {
    object Weather: ConfigType("weather_settings", "Weather Settings")
    object Rocket:  ConfigType("rocket_settings", "Rocket Settings")
}
