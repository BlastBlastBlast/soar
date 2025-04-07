package no.uio.ifi.in2000.met2025.ui.screens.weathercardscreen

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import no.uio.ifi.in2000.met2025.ui.screens.weathercardscreen.components.HourlyExpandableCard
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.LaunchedEffect
import no.uio.ifi.in2000.met2025.data.local.Database.ConfigProfile
import no.uio.ifi.in2000.met2025.ui.screens.home.maps.LocationViewModel
import no.uio.ifi.in2000.met2025.ui.screens.weathercardscreen.components.DailyForecastCard
import no.uio.ifi.in2000.met2025.ui.screens.weathercardscreen.components.DailyForecastRowSection

@Composable
fun WeatherCardScreen(
    viewModel: WeatherCardViewmodel = hiltViewModel(),
    locationViewModel: LocationViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val activeConfig by viewModel.activeConfig.collectAsState()
    val coordinates by locationViewModel.coordinates.collectAsState()

    LaunchedEffect(coordinates) {
        viewModel.loadForecast(coordinates.first, coordinates.second)
    }

    // Only show the screen once the config is loaded
    if (activeConfig != null) {
        ScreenContent(uiState = uiState, config = activeConfig!!)
    } else {
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
                        config = config,  // Pass the active configuration here
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }
            }
            else -> Unit
        }
    }
}

