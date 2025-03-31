package no.uio.ifi.in2000.met2025.ui.navigation

import android.os.Bundle
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
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
import no.uio.ifi.in2000.met2025.ui.screens.home.HomeScreen
import no.uio.ifi.in2000.met2025.ui.screens.home.HomeScreenViewModel
import no.uio.ifi.in2000.met2025.ui.screens.weathercardscreen.WeatherCardScreen
import no.uio.ifi.in2000.met2025.ui.screens.weathercardscreen.WeatherCardViewmodel

object DoubleNavType : NavType<Double>(isNullableAllowed = false) {
    override fun get(bundle: Bundle, key: String): Double? = bundle.getDouble(key)
    override fun parseValue(value: String): Double = value.toDouble()
    override fun put(bundle: Bundle, key: String, value: Double) = bundle.putDouble(key, value)
}

sealed class Screen(val route: String) {
    object Home : Screen("home")
    object Weather : Screen("weather?lat={lat}&lon={lon}") {
        fun createRoute(lat: Double, lon: Double) = "weather?lat=$lat&lon=$lon"
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppNavLauncher() {
    val navController = rememberNavController()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    // Determine current screen title based on the nav back stack.
    val currentBackStackEntry by navController.currentBackStackEntryAsState()
    val currentScreenTitle = when (currentBackStackEntry?.destination?.route) {
        Screen.Home.route -> "Home"
        else -> if (currentBackStackEntry?.destination?.route?.startsWith("weather") == true) "Weather" else "Unknown"
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
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
                        // Back button
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(
                                imageVector = Icons.Default.KeyboardArrowLeft,
                                contentDescription = "Back",
                                tint = Color.White
                            )
                        }
                        // Forward button (if needed)
                        IconButton(onClick = { /* forward navigation logic */ }) {
                            Icon(
                                imageVector = Icons.Default.KeyboardArrowRight,
                                contentDescription = "Forward",
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
                    val homeViewModel: HomeScreenViewModel = hiltViewModel()
                    HomeScreen(
                        viewModel = homeViewModel,
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
                    val weatherViewModel: WeatherCardViewmodel = hiltViewModel()
                    LaunchedEffect(lat, lon) {
                        weatherViewModel.loadForecast(lat, lon)
                    }
                    WeatherCardScreen(viewModel = weatherViewModel)
                }
            }
        }
    }
}
