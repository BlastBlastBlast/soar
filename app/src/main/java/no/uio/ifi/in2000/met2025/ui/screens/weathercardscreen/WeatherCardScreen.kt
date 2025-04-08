package no.uio.ifi.in2000.met2025.ui.screens.weathercardscreen

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import no.uio.ifi.in2000.met2025.ui.screens.weathercardscreen.components.HourlyExpandableCard
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.LaunchedEffect
import androidx.navigation.NavHostController
import no.uio.ifi.in2000.met2025.data.local.database.ConfigProfile
import no.uio.ifi.in2000.met2025.ui.navigation.Screen
import no.uio.ifi.in2000.met2025.ui.screens.home.maps.LocationViewModel
import no.uio.ifi.in2000.met2025.ui.screens.weathercardscreen.components.ConfigSelectionOverlay
import no.uio.ifi.in2000.met2025.ui.screens.weathercardscreen.components.DailyForecastRowSection

@Composable
fun WeatherCardScreen(
    viewModel: WeatherCardViewmodel = hiltViewModel(),
    locationViewModel: LocationViewModel = hiltViewModel(),
    navController: NavHostController  // Pass navController from your NavGraph
) {
    val uiState by viewModel.uiState.collectAsState()
    val activeConfig by viewModel.activeConfig.collectAsState()
    val configList by viewModel.configList.collectAsState()
    val coordinates by locationViewModel.coordinates.collectAsState()

    LaunchedEffect(coordinates) {
        viewModel.loadForecast(coordinates.first, coordinates.second)
    }

    if (activeConfig != null) {
        Box(modifier = Modifier.fillMaxSize()) {
            ScreenContent(uiState = uiState, config = activeConfig!!)
            ConfigSelectionOverlay(
                configList = configList,
                activeConfig = activeConfig!!,
                onConfigSelected = { selectedConfig ->
                    viewModel.setActiveConfig(selectedConfig)
                },
                onNavigateToEditConfigs = { navController.navigate(Screen.ConfigList.route) }
            )
        }
    } else {
        //TODO: Add better error handling for default config
        Text("Loading configuration...", style = MaterialTheme.typography.bodyMedium)
    }
}


@Composable
fun ScreenContent(
    uiState: WeatherCardViewmodel.WeatherCardUiState,
    config: ConfigProfile
) {
    val scrollState = rememberScrollState()
    Column(
        modifier = Modifier
            .padding(16.dp)
            .verticalScroll(scrollState)
    ) {
        when (uiState) {
            is WeatherCardViewmodel.WeatherCardUiState.Loading -> {
                Text("Loading...", style = MaterialTheme.typography.headlineSmall)
            }
            is WeatherCardViewmodel.WeatherCardUiState.Error -> {
                Text("Error: ${uiState.message}", style = MaterialTheme.typography.headlineSmall)
            }
            is WeatherCardViewmodel.WeatherCardUiState.Success -> {
                val forecastItems = uiState.forecastItems
                DailyForecastRowSection(forecastItems = forecastItems)
                Text(
                    text = "Hourly Forecast",
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(vertical = 16.dp)
                )
                val today = forecastItems.firstOrNull()?.time?.substring(0, 10)
                val dailyItems = forecastItems.filter { it.time.startsWith(today ?: "") }
                dailyItems.forEach { forecastItem ->
                    HourlyExpandableCard(
                        forecastItem = forecastItem,
                        config = config,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }
            }
            else -> Unit
        }
    }
}

