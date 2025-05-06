package no.uio.ifi.in2000.met2025.ui.navigation

import android.os.Bundle
import androidx.navigation.NavType

object DoubleNavType : NavType<Double>(isNullableAllowed = false) {
    override fun get(bundle: Bundle, key: String): Double? = bundle.getDouble(key)
    override fun parseValue(value: String): Double = value.toDouble()
    override fun put(bundle: Bundle, key: String, value: Double) = bundle.putDouble(key, value)
}

// in no.uio.ifi.in2000.met2025.ui.navigation.Screen.kt

sealed class Screen(val route: String) {
    object Home               : Screen("home")
    object Weather            : Screen("weather/{lat}/{lon}") {
        fun createRoute(lat: Double, lon: Double) = "weather/$lat/$lon"
    }
    object LaunchSite         : Screen("launch_site")
    object Settings           : Screen("settings")
    object ConfigList         : Screen("config_list")
    object ConfigEdit         : Screen("config_edit/{configId}") {
        fun createRoute(id: Int) = "config_edit/$id"
    }
    object RocketConfigList   : Screen("rocket_list")
    object RocketConfigEdit   : Screen("rocket_edit/{rocketName}/{rocketId}") {
        fun createRoute(name: String, id: Int) = "rocket_edit/$name/$id"
    }
}
