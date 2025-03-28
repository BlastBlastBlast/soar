package no.uio.ifi.in2000.met2025.ui.navigation

import android.os.Bundle
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.hilt.navigation.compose.hiltViewModel
import no.uio.ifi.in2000.met2025.ui.screens.home.HomeScreen
import no.uio.ifi.in2000.met2025.ui.screens.home.HomeScreenViewModel
import no.uio.ifi.in2000.met2025.ui.screens.weathercardscreen.WeatherCardScreen
import no.uio.ifi.in2000.met2025.ui.screens.weathercardscreen.WeatherCardViewmodel

// If NavType.DoubleType is not available, define a custom one:
object DoubleNavType : NavType<Double>(isNullableAllowed = false) {
    override fun get(bundle: Bundle, key: String): Double? = bundle.getDouble(key)
    override fun parseValue(value: String): Double = value.toDouble()
    override fun put(bundle: Bundle, key: String, value: Double) = bundle.putDouble(key, value)
}

// Define your screen routes.
sealed class Screen(val route: String) {
    object Home : Screen("home")
    // Weather screen route with latitude and longitude as query parameters.
    object Weather : Screen("weather?lat={lat}&lon={lon}") {
        fun createRoute(lat: Double, lon: Double) = "weather?lat=$lat&lon=$lon"
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppNavLauncher() {
    val navController = rememberNavController()

    // Use a Scaffold to add a top bar (and add a bottom bar if needed).
    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Testbar") })
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Home.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            // Home destination
            composable(Screen.Home.route) {
                // Get the HomeScreenViewModel via Hilt.
                val homeViewModel: HomeScreenViewModel = hiltViewModel()
                HomeScreen(
                    viewModel = homeViewModel,
                    onNavigateToWeather = { lat, lon ->
                        navController.navigate(Screen.Weather.createRoute(lat, lon))
                    }
                )
            }
            // Weather destination with arguments.
            composable(
                route = Screen.Weather.route,
                arguments = listOf(
                    navArgument("lat") { type = DoubleNavType },
                    navArgument("lon") { type = DoubleNavType }
                )
            ) { backStackEntry ->
                // Retrieve the latitude and longitude arguments.
                val lat = backStackEntry.arguments?.getDouble("lat") ?: 0.0
                val lon = backStackEntry.arguments?.getDouble("lon") ?: 0.0

                // Get the WeatherCardViewmodel via Hilt.
                val weatherViewModel: WeatherCardViewmodel = hiltViewModel()

                // Trigger loading of the forecast when the Weather screen is entered.
                LaunchedEffect(lat, lon) {
                    weatherViewModel.loadForecast(lat, lon)
                }
                WeatherCardScreen(viewModel = weatherViewModel)
            }
        }
    }
}
