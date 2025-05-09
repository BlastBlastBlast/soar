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
import no.uio.ifi.in2000.met2025.ui.screens.home.*
import no.uio.ifi.in2000.met2025.ui.screens.launchsite.LaunchSiteScreen
import no.uio.ifi.in2000.met2025.ui.screens.weatherScreen.*
import no.uio.ifi.in2000.met2025.ui.navigation.Screen.*
import no.uio.ifi.in2000.met2025.ui.screens.settings.SettingsScreen
import no.uio.ifi.in2000.met2025.ui.screens.settings.SettingsViewModel
import no.uio.ifi.in2000.met2025.ui.screens.settings.rocketconfig.RocketConfigEditScreen
import no.uio.ifi.in2000.met2025.ui.screens.settings.rocketconfig.RocketConfigListScreen
import no.uio.ifi.in2000.met2025.ui.screens.settings.weathersettings.ConfigEditScreen
import no.uio.ifi.in2000.met2025.ui.screens.settings.weathersettings.ConfigListScreen

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
    homeScreenViewModel: HomeScreenViewModel,
    weatherCardViewModel: WeatherViewModel,
    settingsViewModel: SettingsViewModel,
) {
    NavHost(
        navController    = navController,
        startDestination = Home.route,
        modifier         = Modifier
            .padding(innerPadding)
            .windowInsetsPadding(WindowInsets.ime)
    ) {
        // — Home —
        composable(Home.route) {
            HomeScreen(
                viewModel = homeScreenViewModel,
                onNavigateToWeather = { lat, lon ->
                    navController.navigateSingleTopTo(Weather.createRoute(lat, lon))
                },
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

        // — Settings (NEW) —
        composable(Settings.route) {
            SettingsScreen(
                onWeatherConfigsClick = {
                    navController.navigateSingleTopTo(ConfigList.route)
                },
                onRocketConfigsClick = {
                    navController.navigateSingleTopTo(RocketConfigList.route)
                }
            )
        }

        // — Config List —
        composable(ConfigList.route) {
            ConfigListScreen(
                onEditConfig   = { cfg ->
                    navController.navigateSingleTopTo(ConfigEdit.createRoute(cfg.id))
                },
                onAddConfig    = {
                    navController.navigateSingleTopTo(ConfigEdit.createRoute(-1))
                },
                onSelectConfig = { cfg ->
                    weatherCardViewModel.setActiveConfig(cfg)
                    navController.popBackStack()
                }
            )
        }

        // — Config Edit —
        composable(
            route     = ConfigEdit.route,
            arguments = listOf(navArgument("configId") {
                type         = NavType.IntType
                defaultValue = -1
            })
        ) { back ->
            val id by remember { mutableStateOf(back.arguments?.getInt("configId") ?: -1) }
            val config by settingsViewModel
                .getWeatherConfig(id)
                .collectAsState(initial = null)

            ConfigEditScreen(
                config         = config,
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
            val rocketParams by settingsViewModel
                .getRocketConfig(rocketId)
                .collectAsState(initial = null)

            RocketConfigEditScreen(
                rocketParameters = rocketParams,
                onNavigateBack   = { navController.popBackStack() }
            )
        }
    }
}