package no.uio.ifi.in2000.met2025.ui.screens.weathercardscreen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PageSize
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
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
import androidx.compose.material3.Button
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Slider
import androidx.compose.material3.TextButton
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.navigation.NavHostController
import no.uio.ifi.in2000.met2025.data.local.database.ConfigProfile
import no.uio.ifi.in2000.met2025.ui.navigation.Screen
import no.uio.ifi.in2000.met2025.ui.screens.weathercardscreen.components.ConfigSelectionOverlay
import no.uio.ifi.in2000.met2025.ui.screens.weathercardscreen.components.DailyForecastRowSection
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.platform.LocalConfiguration
import no.uio.ifi.in2000.met2025.data.models.ForecastDataItem
import no.uio.ifi.in2000.met2025.data.models.LaunchStatus
import no.uio.ifi.in2000.met2025.ui.screens.weathercardscreen.components.WeatherLoadingSpinner
import no.uio.ifi.in2000.met2025.ui.screens.weathercardscreen.components.filter.LaunchStatusFilter
import no.uio.ifi.in2000.met2025.ui.screens.weathercardscreen.components.filter.forecastPassesFilter
import androidx.compose.ui.platform.LocalConfiguration


//WeatherCardScreen.kt
@Composable
fun WeatherCardScreen(
    viewModel: WeatherCardViewmodel = hiltViewModel(),
    navController: NavHostController
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
    config: no.uio.ifi.in2000.met2025.data.local.database.ConfigProfile,
    filterActive: Boolean,
    onToggleFilter: () -> Unit,
    viewModel: WeatherCardViewmodel
) {
    // State for the hour slider.
    var hoursToShow by remember { mutableStateOf(24f) }
    // Get screen height from configuration.
    val screenHeight = LocalConfiguration.current.screenHeightDp.dp

    if (uiState is WeatherCardViewmodel.WeatherCardUiState.Success) {
        val forecastItems = uiState.forecastItems

        // Filter and limit forecast items based on the slider.
        val filteredItems = forecastItems.let { allItems ->
            if (filterActive)
                allItems.filter {
                    forecastPassesFilter(
                        it,
                        config,
                        LaunchStatusFilter(setOf(LaunchStatus.SAFE, LaunchStatus.CAUTION))
                    )
                }
            else allItems
        }.take(hoursToShow.toInt())

        // Group forecast items by day (using the first 10 characters of the timestamp).
        val forecastByDay: Map<String, List<ForecastDataItem>> =
            filteredItems.groupBy { it.time.substring(0, 10) }
        val sortedDays = forecastByDay.keys.sorted()

        // Create a PagerState for the number of days.
        val pagerState: PagerState = rememberPagerState(pageCount = { sortedDays.size })

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 16.dp)
        ) {
            // -- Header Section (Filter toggle, slider, labels) --
            item {
                Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                    // Filter toggle at the top right.
                    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = androidx.compose.ui.Alignment.CenterEnd) {
                        FilterToggleButton(isActive = filterActive, onClick = onToggleFilter)
                    }
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
                    Text(
                        text = "Hourly",
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier.padding(vertical = 16.dp)
                    )
                }
            }

            // Optional: Daily overview row.
            item {
                DailyForecastRowSection(forecastItems = forecastItems)
            }

            // -- Horizontal Pager Section --
            // We wrap the HorizontalPager in a Box whose height is the full screen height.
            item {
                Box(modifier = Modifier
                    .fillMaxWidth()
                    .height(screenHeight)   // Force the pager to occupy full screen height.
                    .padding(vertical = 16.dp)
                ) {
                    HorizontalPager(
                        state = pagerState,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(screenHeight)
                            .padding(vertical = 16.dp),
                        contentPadding = PaddingValues(horizontal = 16.dp),
                        pageSpacing = 16.dp,
                        pageSize = PageSize.Fill
                    ) { page ->
                        val date = sortedDays[page]
                        val dailyForecastItems = forecastByDay[date] ?: emptyList()

                        // The Column now fills the parent's height so that its children align at the top.
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .fillMaxHeight()    // Forces the Column to take up the full height.
                                .verticalScroll(rememberScrollState())
                                .padding(vertical = 16.dp),
                            verticalArrangement = Arrangement.Top
                        ) {
                            Text(
                                text = date,
                                style = MaterialTheme.typography.titleLarge,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                            dailyForecastItems.forEach { forecastItem ->
                                HourlyExpandableCard(
                                    forecastItem = forecastItem,
                                    coordinates = coordinates,
                                    config = config,
                                    modifier = Modifier.padding(vertical = 8.dp),
                                    viewModel = viewModel
                                )
                            }
                        }
                    }
                }
            }
        }
    } else {
        // For Loading and Error states, use a simple LazyColumn.
        LazyColumn(modifier = Modifier.fillMaxSize(), contentPadding = PaddingValues(16.dp)) {
            when (uiState) {
                is WeatherCardViewmodel.WeatherCardUiState.Loading -> item { WeatherLoadingSpinner() }
                is WeatherCardViewmodel.WeatherCardUiState.Error -> item {
                    Text(
                        text = "Feil: ${uiState.message}",
                        style = MaterialTheme.typography.headlineSmall
                    )
                }
                else -> Unit
            }
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

