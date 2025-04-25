package no.uio.ifi.in2000.met2025.ui.navigation

import android.os.Bundle
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
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
import no.uio.ifi.in2000.met2025.ui.screens.rocketconfig.RocketConfigEditScreen
import no.uio.ifi.in2000.met2025.ui.screens.home.HomeScreen
import no.uio.ifi.in2000.met2025.ui.screens.home.HomeScreenViewModel
import no.uio.ifi.in2000.met2025.ui.screens.launchsite.LaunchSiteScreen
import no.uio.ifi.in2000.met2025.ui.screens.rocketconfig.RocketConfigEditViewModel
import no.uio.ifi.in2000.met2025.ui.screens.rocketconfig.RocketConfigListScreen
import no.uio.ifi.in2000.met2025.ui.screens.weathercardscreen.WeatherCardScreen
import no.uio.ifi.in2000.met2025.ui.screens.weathercardscreen.WeatherCardViewmodel

//TODO: FIKSE NAVIGATION KALL TIL Å IKKE LAUNCHE MANGE GANGER PÅ SAMME SKJERM!

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
    data object ConfigList : Screen("config_list")
    data object ConfigEdit : Screen("config_edit")
    // New rocket configuration screens:
    data object RocketConfigList : Screen("rocket_config_list")
    data object RocketConfigEdit : Screen("rocket_config_edit/{rocketName}/{rocketId}") {
        fun createRoute(rocketName: String, rocketId: Int) = "rocket_config_edit/$rocketName/$rocketId"
    }
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
        Screen.RocketConfigList.route -> "Rocket Configurations"
        else -> if (currentBackStackEntry?.destination?.route?.startsWith("weather") == true) "Weather" else "Unknown"
    }
    // Disable drawer gestures on Home so the drawer is only opened by tapping the logo.
    val gesturesEnabled = currentScreenTitle != "Home"

    // ViewModels used in navigation.
    val configEditViewModel: ConfigEditViewModel = hiltViewModel()
    val weatherCardViewModel: WeatherCardViewmodel = hiltViewModel()
    val homeScreenViewModel: HomeScreenViewModel = hiltViewModel()

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
                // New Drawer Item for Rocket Configurations.
                NavigationDrawerItem(
                    label = { Text("Rocket Configurations") },
                    selected = currentScreenTitle == "Rocket Configurations",
                    onClick = {
                        navController.navigate(Screen.RocketConfigList.route) {
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
                        viewModel            = homeScreenViewModel,
                        onNavigateToWeather  = { lat, lon ->
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
                            navController.navigate("config_edit/${config.id}")
                        },
                        onAddConfig = { navController.navigate("config_edit/-1") },
                        onSelectConfig = { config ->
                            // For example, set active config then pop back.
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
                            defaultValue = -1
                        }
                    )
                ) { backStackEntry ->
                    val configId = backStackEntry.arguments?.getInt("configId") ?: -1
                    val config by configEditViewModel.getConfigProfile(configId)
                        .collectAsState(initial = null)
                    ConfigEditScreen(
                        config = config,
                        onNavigateBack = { navController.popBackStack() }
                    )
                }
                // New Rocket Config List screen.
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
                // New Rocket Config Edit screen.
                composable(
                    route = Screen.RocketConfigEdit.route,
                    arguments = listOf(
                        navArgument("rocketName") { type = NavType.StringType },
                        navArgument("rocketId") {
                            type = NavType.IntType
                            defaultValue = -1
                        }
                    )
                ) { backStackEntry ->
                    val rocketId = backStackEntry.arguments?.getInt("rocketId") ?: -1
                    val rocketName = backStackEntry.arguments?.getString("rocketName") ?: ""
                    val rocketConfig by hiltViewModel<RocketConfigEditViewModel>()
                        .getRocketConfig(rocketId)
                        .collectAsState(initial = null)
                    RocketConfigEditScreen(
                        rocketParameters = rocketConfig,
                        onNavigateBack = { navController.popBackStack() }
                    )
                }
            }
        }
    }
}