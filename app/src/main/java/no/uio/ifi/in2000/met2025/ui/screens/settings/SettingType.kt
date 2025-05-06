package no.uio.ifi.in2000.met2025.ui.screens.settings

sealed class SettingType(val route: String, val label: String) {
    object Weather: SettingType("weather_settings", "Weather Settings")
    object Rocket:  SettingType("rocket_settings", "Rocket Settings")
}
