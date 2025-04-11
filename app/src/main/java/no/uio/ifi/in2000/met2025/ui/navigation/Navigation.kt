package no.uio.ifi.in2000.met2025.ui.navigation

import android.os.Bundle
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavType
import androidx.navigation.compose.*
import androidx.navigation.navArgument
import kotlinx.coroutines.launch
import no.uio.ifi.in2000.met2025.R
import no.uio.ifi.in2000.met2025.ui.configprofiles.ConfigEditScreen
import no.uio.ifi.in2000.met2025.ui.configprofiles.ConfigEditViewModel
import no.uio.ifi.in2000.met2025.ui.configprofiles.ConfigListScreen
import no.uio.ifi.in2000.met2025.ui.screens.home.HomeScreen
import no.uio.ifi.in2000.met2025.ui.screens.home.HomeScreenViewModel
import no.uio.ifi.in2000.met2025.ui.screens.launchsite.LaunchSiteScreen
import no.uio.ifi.in2000.met2025.ui.screens.weathercardscreen.WeatherCardScreen
import no.uio.ifi.in2000.met2025.ui.screens.weathercardscreen.WeatherCardViewmodel

object DoubleNavType : NavType<Double>(isNullableAllowed = false) {
    override fun get(bundle: Bundle, key: String): Double? = bundle.getDouble(key)
    override fun parseValue(value: String): Double = value.toDouble()
    override fun put(bundle: Bundle, key: String, value: Double) = bundle.putDouble(key, value)
}

sealed class Screen(val route: String) {
    data object Home : Screen("home")
    data object Weather : Screen("weather?lat={lat}&lon={lon}") {
        fun createRoute(lat: Double, lon: Double) = "weather?lat=$lat&lon=$lon"
    }
    data object LaunchSite : Screen("launchsite")
    data object AtmosphericWind: Screen("atmosphericwind")
    data object ConfigList : Screen("config_list")
    data object ConfigEdit : Screen("config_edit")

}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppNavLauncher(
    darkTheme: Boolean,
    toggleTheme: () -> Unit
) {
    val navController = rememberNavController()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    // Determine current screen title based on the nav back stack.
    val currentBackStackEntry by navController.currentBackStackEntryAsState()
    val currentScreenTitle = when (currentBackStackEntry?.destination?.route) {
        Screen.Home.route -> "Home"
        Screen.LaunchSite.route -> "Launch Sites"
        Screen.AtmosphericWind.route -> "Atmospheric Wind"
        else -> if (currentBackStackEntry?.destination?.route?.startsWith("weather") == true) "Weather" else "Unknown"
    }
    // Disable gestures on Home so the drawer is only opened by tapping the logo.
    val gesturesEnabled = currentScreenTitle != "Home"
    val configEditViewModel : ConfigEditViewModel = hiltViewModel()
    val weatherCardViewModel : WeatherCardViewmodel = hiltViewModel()
    val homeScreenViewModel : HomeScreenViewModel = hiltViewModel()

    ModalNavigationDrawer(
        drawerState = drawerState,
        gesturesEnabled = gesturesEnabled,
        drawerContent = {
            ModalDrawerSheet {
                NavigationDrawerItem(
                    label = { Text("Home") },
                    selected = currentScreenTitle == "Home",
                    onClick = {
                        navController.navigate(Screen.Home.route) {
                            popUpTo(navController.graph.startDestinationId)
                            launchSingleTop = true
                        }
                        scope.launch { drawerState.close() }
                    },
                    modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                )
                NavigationDrawerItem(
                    label = { Text("Weather") },
                    selected = currentScreenTitle == "Weather",
                    onClick = {
                        navController.navigate(Screen.Weather.createRoute(0.0, 0.0)) {
                            popUpTo(navController.graph.startDestinationId)
                            launchSingleTop = true
                        }
                        scope.launch { drawerState.close() }
                    },
                    modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                )
                NavigationDrawerItem(
                    label = { Text("Launch Sites") },
                    selected = currentScreenTitle == "Launch Sites",
                    onClick = {
                        navController.navigate(Screen.LaunchSite.route) {
                            popUpTo(navController.graph.startDestinationId)
                            launchSingleTop = true
                        }
                        scope.launch { drawerState.close() }
                    },
                    modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                )
                NavigationDrawerItem(
                    label = { Text("Atmospheric Wind") },
                    selected = currentScreenTitle == "Atmospheric Wind",
                    onClick = {
                        navController.navigate(Screen.AtmosphericWind.route) {
                            popUpTo(navController.graph.startDestinationId)
                            launchSingleTop = true
                        }
                        scope.launch { drawerState.close() }
                    },
                    modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                )
            }
        }
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text(text = currentScreenTitle, color = Color.White) },
                    navigationIcon = {
                        IconButton(onClick = { scope.launch { drawerState.open() } }) {
                            Image(
                                painter = painterResource(id = R.drawable.placeholder_logo),
                                contentDescription = "Logo",
                                modifier = Modifier.size(40.dp)
                            )
                        }
                    },
                    actions = {
                        // Toggle theme button.
                        IconButton(onClick = toggleTheme) {
                            val iconRes = if (darkTheme) R.drawable.sun_icon else R.drawable.moon_icon
                            Icon(
                                painter = painterResource(id = iconRes),
                                contentDescription = "Toggle Theme",
                                tint = Color.White
                            )
                        }
                        // Launch Sites navigation button.
                        IconButton(onClick = { navController.navigate(Screen.LaunchSite.route) }) {
                            Icon(
                                painter = painterResource(id = R.drawable.fav_launchsites),
                                contentDescription = "Launch Sites",
                                tint = Color.White
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Black)
                )
            }
        ) { innerPadding ->
            NavHost(
                navController = navController,
                startDestination = Screen.Home.route,
                modifier = Modifier.padding(innerPadding)
            ) {
                composable(Screen.Home.route) {
                    HomeScreen(
                        viewModel = homeScreenViewModel,
                        onNavigateToWeather = { lat, lon ->
                            navController.navigate(Screen.Weather.createRoute(lat, lon))
                        }
                    )
                }
                composable(
                    route = Screen.Weather.route,
                    arguments = listOf(
                        navArgument("lat") { type = DoubleNavType },
                        navArgument("lon") { type = DoubleNavType }
                    )
                ) { backStackEntry ->
                    val lat = backStackEntry.arguments?.getDouble("lat") ?: 0.0
                    val lon = backStackEntry.arguments?.getDouble("lon") ?: 0.0
                    LaunchedEffect(lat, lon) {
                        weatherCardViewModel.loadForecast(lat, lon)
                    }
                    WeatherCardScreen(
                        weatherCardViewModel,
                        navController = navController
                    )
                }
                composable(Screen.LaunchSite.route) {
                    LaunchSiteScreen()
                }
                composable(Screen.ConfigList.route) {
                    ConfigListScreen(
                        onEditConfig = { config ->
                            // Navigate with the config id as part of the route.
                            navController.navigate("config_edit/${config.id}")
                        },
                        onAddConfig = {
                            // Using -1 to indicate a new configuration.
                            navController.navigate("config_edit/-1")
                        },
                        onSelectConfig = { config ->
                            weatherCardViewModel.setActiveConfig(config)
                            navController.popBackStack()
                        }
                    )
                }
                composable(
                    route = "config_edit/{configId}",
                    arguments = listOf(
                        navArgument("configId") {
                            type = NavType.IntType
                            defaultValue = -1 // -1 indicates a new config.
                        }
                    )
                ) { backStackEntry ->
                    val configId = backStackEntry.arguments?.getInt("configId") ?: -1
                    // Load the config if editing (configId != -1), else pass null
                    val config by configEditViewModel.getConfigProfile(configId).collectAsState(initial = null)

                    ConfigEditScreen(
                        config = config,
                        onNavigateBack = { navController.popBackStack() }
                    )
                }

            }
        }
    }
}

