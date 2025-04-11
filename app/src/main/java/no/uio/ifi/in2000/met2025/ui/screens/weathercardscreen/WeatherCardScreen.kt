package no.uio.ifi.in2000.met2025.ui.screens.weathercardscreen

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
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
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Slider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.navigation.NavHostController
import no.uio.ifi.in2000.met2025.data.local.database.ConfigProfile
import no.uio.ifi.in2000.met2025.ui.navigation.Screen
import no.uio.ifi.in2000.met2025.ui.screens.weathercardscreen.components.ConfigSelectionOverlay
import no.uio.ifi.in2000.met2025.ui.screens.weathercardscreen.components.DailyForecastRowSection
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import no.uio.ifi.in2000.met2025.data.models.launchstatus.LaunchStatus
import no.uio.ifi.in2000.met2025.ui.screens.weathercardscreen.components.WeatherLoadingSpinner
import no.uio.ifi.in2000.met2025.ui.screens.weathercardscreen.components.filter.LaunchStatusFilter
import no.uio.ifi.in2000.met2025.ui.screens.weathercardscreen.components.filter.forecastPassesFilter


@Composable
fun WeatherCardScreen(
    viewModel: WeatherCardViewmodel = hiltViewModel(),
    navController: NavHostController  // Pass navController from your NavGraph
) {
    val uiState by viewModel.uiState.collectAsState()
    val activeConfig by viewModel.activeConfig.collectAsState()
    val configList by viewModel.configList.collectAsState()
    val coordinates by viewModel.coordinates.collectAsState()

    var filterActive by remember { mutableStateOf(false) }

    LaunchedEffect(coordinates) {
        viewModel.loadForecast(coordinates.first, coordinates.second)
    }

    if (activeConfig != null) {
        Box(modifier = Modifier.fillMaxSize()) {
            ScreenContent(
                uiState = uiState,
                coordinates = coordinates,
                config = activeConfig!!,
                filterActive = filterActive,
                onToggleFilter = { filterActive = !filterActive },
                viewModel
            )
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
        Text("Loading configuration...", style = MaterialTheme.typography.bodyMedium)
    }
}


@Composable
fun ScreenContent(
    uiState: WeatherCardViewmodel.WeatherCardUiState,
    coordinates: Pair<Double, Double>,
    config: ConfigProfile,
    filterActive: Boolean,
    onToggleFilter: () -> Unit,
    viewModel: WeatherCardViewmodel
) {
    val scrollState = rememberScrollState()
    // Mutable state for the slider value (default = 24 hours, range: 4-72 hours)
    var hoursToShow by remember { mutableStateOf(24f) }

    Column(
        modifier = Modifier
            .padding(16.dp)
            .verticalScroll(scrollState)
    ) {
        // Filter toggle button at the top right.
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            contentAlignment = Alignment.CenterEnd
        ) {
            FilterToggleButton(
                isActive = filterActive,
                onClick = onToggleFilter
            )
        }

        when (uiState) {
            is WeatherCardViewmodel.WeatherCardUiState.Loading -> { WeatherLoadingSpinner() }
            is WeatherCardViewmodel.WeatherCardUiState.Error -> {
                Text("Feil: ${uiState.message}", style = MaterialTheme.typography.headlineSmall)
            }
            is WeatherCardViewmodel.WeatherCardUiState.Success -> {
                val forecastItems = uiState.forecastItems

                // Optionally, show a daily forecast row section for additional context.
                DailyForecastRowSection(forecastItems = forecastItems)

                // ===== TIME SLIDER SECTION =====
                Text(
                    text = "Show forecast for ${hoursToShow.toInt()} hours",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
                Slider(
                    value = hoursToShow,
                    onValueChange = { hoursToShow = it },
                    valueRange = 4f..72f,
                    steps = (72 - 4 - 1),
                    modifier = Modifier.fillMaxWidth()
                )
                // ===== END SLIDER SECTION =====

                Text(
                    text = "Hourly",
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(vertical = 16.dp)
                )

                // --- ORIGINAL FILTER BY TODAY (Commented Out) ---
                // val today = forecastItems.firstOrNull()?.time?.substring(0, 10)
                // val dailyForecast = forecastItems.filter { it.time.startsWith(today ?: "") }
                // --- END ORIGINAL TODAY FILTER ---

                // Now, without filtering by "today",
                // optionally filter the forecast based on a launch status filter if active,
                // then take as many items as specified by the slider.
                val filteredItems = forecastItems
                    .let { allItems ->
                        if (filterActive)
                            allItems.filter {
                                forecastPassesFilter(
                                    it,
                                    config,
                                    LaunchStatusFilter(setOf(LaunchStatus.SAFE, LaunchStatus.CAUTION))
                                )
                            }
                        else allItems
                    }
                    .take(hoursToShow.toInt())

                filteredItems.forEach { forecastItem ->
                    HourlyExpandableCard(
                        forecastItem = forecastItem,
                        coordinates = coordinates,
                        config = config,
                        modifier = Modifier.padding(vertical = 8.dp),
                        viewModel
                    )
                }
            }
            else -> Unit
        }
    }
}




@Composable
fun FilterToggleButton(
    isActive: Boolean,
    onClick: () -> Unit
) {
    FilledTonalButton(onClick = onClick) {
        Text(text = if (isActive) "Show all" else "Show valid")
    }
}

