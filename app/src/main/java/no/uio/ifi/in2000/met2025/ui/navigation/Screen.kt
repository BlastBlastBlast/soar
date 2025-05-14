package no.uio.ifi.in2000.met2025.ui.navigation

import android.os.Bundle
import androidx.navigation.NavType

/**
 * DoubleNavType
 *
 * Custom NavType for passing Double arguments via Navigation routes.
 * Used for passing latitude and longitude values to weather screen.
 */
object DoubleNavType : NavType<Double>(isNullableAllowed = false) {
    override fun get(bundle: Bundle, key: String): Double? = bundle.getDouble(key)
    override fun parseValue(value: String): Double = value.toDouble()
    override fun put(bundle: Bundle, key: String, value: Double) = bundle.putDouble(key, value)
}

/**
 * Screen
 *
 * Defines all navigation routes in the app as sealed objects.
 */
sealed class Screen(val route: String) {
    data object Maps               : Screen("maps")
    data object Weather            : Screen("weather/{lat}/{lon}") {
        fun createRoute(lat: Double, lon: Double) = "weather/$lat/$lon"
    }
    data object LaunchSite         : Screen("launch_site")
    data object Configs           : Screen("settings")
    data object WeatherConfigList         : Screen("weather_list")
    data object WeatherConfigEdit         : Screen("weather_edit/{weatherId}") {
        fun createRoute(id: Int) = "weather_edit/$id"
    }
    data object RocketConfigList   : Screen("rocket_list")
    data object RocketConfigEdit   : Screen("rocket_edit/{rocketName}/{rocketId}") {
        fun createRoute(name: String, id: Int) = "rocket_edit/$name/$id"
    }
}
