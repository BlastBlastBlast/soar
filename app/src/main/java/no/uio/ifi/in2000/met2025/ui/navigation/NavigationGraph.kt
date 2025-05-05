package no.uio.ifi.in2000.met2025.ui.navigation

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import no.uio.ifi.in2000.met2025.ui.configprofiles.*
import no.uio.ifi.in2000.met2025.ui.screens.home.*
import no.uio.ifi.in2000.met2025.ui.screens.launchsite.LaunchSiteScreen
import no.uio.ifi.in2000.met2025.ui.screens.rocketconfig.*
import no.uio.ifi.in2000.met2025.ui.screens.weathercardscreen.*

@Composable
fun NavigationGraph(
    navController: NavHostController,
    innerPadding: PaddingValues,
    homeScreenViewModel: HomeScreenViewModel,
    weatherCardViewModel: WeatherCardViewmodel,
    configEditViewModel: ConfigEditViewModel,
    rocketConfigEditViewModel: RocketConfigEditViewModel
) {
    NavHost(
        navController   = navController,
        startDestination = Screen.Home.route,
        modifier         = Modifier
            .padding(innerPadding)
            .windowInsetsPadding(WindowInsets.ime)
    ) {
        // Home
        composable(Screen.Home.route) {
            HomeScreen(
                viewModel         = homeScreenViewModel,
                onNavigateToWeather = { lat, lon ->
                    navController.navigate(Screen.Weather.createRoute(lat, lon))
                }
            )
        }
        // Weather
        composable(
            route     = Screen.Weather.route,
            arguments = listOf(
                navArgument("lat") { type = DoubleNavType },
                navArgument("lon") { type = DoubleNavType }
            )
        ) { back ->
            val lat = back.arguments?.getDouble("lat") ?: 0.0
            val lon = back.arguments?.getDouble("lon") ?: 0.0

            LaunchedEffect(lat, lon) {
                weatherCardViewModel.loadForecast(lat, lon)
            }

            WeatherCardScreen(
                viewModel      = weatherCardViewModel,
                navController  = navController
            )
        }
        // Launch Site
        composable(Screen.LaunchSite.route) {
            LaunchSiteScreen()
        }
        // Config List
        composable(Screen.ConfigList.route) {
            ConfigListScreen(
                onEditConfig   = { cfg -> navController.navigate("config_edit/${cfg.id}") },
                onAddConfig    = { navController.navigate("config_edit/-1") },
                onSelectConfig = { cfg ->
                    weatherCardViewModel.setActiveConfig(cfg)
                    navController.popBackStack()
                }
            )
        }
        // Config Edit
        composable(
            route     = "config_edit/{configId}",
            arguments = listOf(navArgument("configId") {
                type         = NavType.IntType
                defaultValue = -1
            })
        ) { back ->
            val id = back.arguments?.getInt("configId") ?: -1
            val config by configEditViewModel
                .getConfigProfile(id)
                .collectAsState(initial = null)

            ConfigEditScreen(
                config        = config,
                onNavigateBack = { navController.popBackStack() }
            )
        }
        // Rocket Config List
        composable(Screen.RocketConfigList.route) {
            RocketConfigListScreen(
                onEditRocketConfig = { rocket ->
                    // Navigate using both name and id.
                    navController.navigate(Screen.RocketConfigEdit.createRoute(rocket.name, rocket.id))
                },
                onAddRocketConfig = {
                    // Use a placeholder name ("new") and -1 id for a new configuration.
                    navController.navigate(Screen.RocketConfigEdit.createRoute("new", -1))
                },
                onSelectRocketConfig = { rocket ->
                    // You may handle selection here.
                }
            )
        }
        // Rocket Config Edit
        composable(
            route     = Screen.RocketConfigEdit.route,
            arguments = listOf(
                navArgument("rocketName") { type = NavType.StringType },
                navArgument("rocketId") {
                    type         = NavType.IntType
                    defaultValue = -1
                }
            )
        ) { back ->
            val rocketId   = back.arguments?.getInt("rocketId") ?: -1
            val rocketName = back.arguments?.getString("rocketName") ?: ""
            val rocketParams by rocketConfigEditViewModel
                .getRocketConfig(rocketId)
                .collectAsState(initial = null)

            RocketConfigEditScreen(
                rocketParameters = rocketParams,
                onNavigateBack   = { navController.popBackStack() }
            )
        }
    }
}