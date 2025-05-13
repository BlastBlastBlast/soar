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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import no.uio.ifi.in2000.met2025.ui.screens.mapScreen.MapScreen
import no.uio.ifi.in2000.met2025.ui.screens.launchSiteScreen.LaunchSiteScreen
import no.uio.ifi.in2000.met2025.ui.screens.weatherScreen.*
import no.uio.ifi.in2000.met2025.ui.navigation.Screen.*
import no.uio.ifi.in2000.met2025.ui.screens.config.ConfigScreen
import no.uio.ifi.in2000.met2025.ui.screens.config.ConfigViewModel
import no.uio.ifi.in2000.met2025.ui.screens.config.rocketConfig.RocketConfigEditScreen
import no.uio.ifi.in2000.met2025.ui.screens.config.rocketConfig.RocketConfigListScreen
import no.uio.ifi.in2000.met2025.ui.screens.config.weatherConfig.WeatherConfigEditScreen
import no.uio.ifi.in2000.met2025.ui.screens.config.weatherConfig.WeatherConfigListScreen
import no.uio.ifi.in2000.met2025.ui.screens.mapScreen.MapScreenViewModel

/**
 * Navigate to [route] in the normal way, but if the
 * top of the back-stack is already [route], don’t push a new copy.
 */
fun NavHostController.navigateSingleTopTo(route: String) {
    this.navigate(route) {
        // don’t add a duplicate if it’s already on top
        launchSingleTop = true
        // optional: if you want to restore state when re-visiting
        restoreState = true
    }
}

@Composable
fun NavigationGraph(
    navController: NavHostController,
    innerPadding: PaddingValues,
    mapScreenViewModel: MapScreenViewModel,
    weatherCardViewModel: WeatherViewModel,
    configViewModel: ConfigViewModel,
) {
    NavHost(
        navController    = navController,
        startDestination = Maps.route,
        modifier         = Modifier
            .padding(innerPadding)
            .windowInsetsPadding(WindowInsets.ime)
    ) {
        // — Map —
        composable(Maps.route) {
            MapScreen(
                viewModel = mapScreenViewModel,
                onNavigateToWeather = { lat, lon ->
                    navController.navigateSingleTopTo(Weather.createRoute(lat, lon))
                },
                onNavigateToRocketConfig = { navController.navigateSingleTopTo(RocketConfigList.route) }
            )
        }

        // — Weather —
        composable(
            route     = Weather.route,
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

            WeatherScreen(
                viewModel     = weatherCardViewModel,
                navController = navController
            )
        }

        // — Launch Site —
        composable(LaunchSite.route) {
            LaunchSiteScreen()
        }

        // — Settings —
        composable(Configs.route) {
            ConfigScreen(
                onWeatherConfigsClick = {
                    navController.navigateSingleTopTo(WeatherConfigList.route)
                },
                onRocketConfigsClick = {
                    navController.navigateSingleTopTo(RocketConfigList.route)
                }
            )
        }

        // — Weather Config List —
        composable(WeatherConfigList.route) {
            WeatherConfigListScreen(
                onEditConfig   = { cfg ->
                    navController.navigateSingleTopTo(WeatherConfigEdit.createRoute(cfg.id))
                },
                onAddConfig    = {
                    navController.navigateSingleTopTo(WeatherConfigEdit.createRoute(-1))
                },
                onSelectConfig = { cfg ->
                    weatherCardViewModel.setActiveConfig(cfg)
                }
            )
        }

        // — Weather Config Edit —
        composable(
            route     = WeatherConfigEdit.route,
            arguments = listOf(navArgument("weatherId") {
                type         = NavType.IntType
                defaultValue = -1
            })
        ) { back ->
            val id by remember { mutableStateOf(back.arguments?.getInt("weatherId") ?: -1) }
            val config by configViewModel
                .getWeatherConfig(id)
                .collectAsState(initial = null)

            WeatherConfigEditScreen(
                weatherConfig         = config,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        // — Rocket Config List —
        composable(RocketConfigList.route) {
            RocketConfigListScreen(
                onEditRocketConfig = { rocket ->
                    navController.navigateSingleTopTo(
                        RocketConfigEdit.createRoute(rocket.name, rocket.id)
                    )
                },
                onAddRocketConfig = {
                    navController.navigateSingleTopTo(
                        RocketConfigEdit.createRoute("new", -1)
                    )
                },
                onSelectRocketConfig = { /* … */ }
            )
        }

        // — Rocket Config Edit —
        composable(
            route     = RocketConfigEdit.route,
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
            val rocketParams by configViewModel
                .getRocketConfig(rocketId)
                .collectAsState(initial = null)

            RocketConfigEditScreen(
                rocketParameters = rocketParams,
                onNavigateBack   = { navController.popBackStack() }
            )
        }
    }
}