package no.uio.ifi.in2000.met2025.ui.screens.weathercardscreen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Slider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.navigation.NavHostController
import no.uio.ifi.in2000.met2025.data.local.database.ConfigProfile
import no.uio.ifi.in2000.met2025.ui.navigation.Screen
import no.uio.ifi.in2000.met2025.ui.screens.weathercardscreen.components.DailyForecastRowSection
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.platform.LocalConfiguration
import no.uio.ifi.in2000.met2025.data.models.ForecastDataItem
import no.uio.ifi.in2000.met2025.data.models.LaunchStatus
import no.uio.ifi.in2000.met2025.ui.screens.weathercardscreen.components.WeatherLoadingSpinner
import no.uio.ifi.in2000.met2025.ui.screens.weathercardscreen.components.filter.LaunchStatusFilter
import no.uio.ifi.in2000.met2025.ui.screens.weathercardscreen.components.filter.forecastPassesFilter
import no.uio.ifi.in2000.met2025.ui.screens.weathercardscreen.components.config.ConfigMenuOverlay
import no.uio.ifi.in2000.met2025.ui.screens.weathercardscreen.components.SegmentedBottomBar
import no.uio.ifi.in2000.met2025.ui.screens.weathercardscreen.components.filter.FilterMenuOverlay
import no.uio.ifi.in2000.met2025.ui.screens.weathercardscreen.components.site.LaunchSitesMenuOverlay
import java.time.Instant

@Composable
fun WeatherCardScreen(
    viewModel: WeatherCardViewmodel = hiltViewModel(),
    navController: NavHostController
) {
    val uiState by viewModel.uiState.collectAsState()
    val activeConfig by viewModel.activeConfig.collectAsState()
    val configList by viewModel.configList.collectAsState()
    val coordinates by viewModel.coordinates.collectAsState()
    // Expose launch sites via the view model.
    val launchSites by viewModel.launchSites.collectAsState(initial = emptyList())

    // Shared state for forecast hours (controlled via the filter overlay)
    var hoursToShow by remember { mutableStateOf(24f) }
    var filterActive by remember { mutableStateOf(false) }
    var isConfigMenuExpanded by remember { mutableStateOf(false) }
    var isFilterMenuExpanded by remember { mutableStateOf(false) }
    var isLaunchMenuExpanded by remember { mutableStateOf(false) }

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
                hoursToShow = hoursToShow,
                viewModel = viewModel
            )
            // Segmented Bottom Bar with three buttons.
            SegmentedBottomBar(
                onConfigClick = {
                    // Toggle configuration overlay and close others.
                    if (!isConfigMenuExpanded) {
                        isConfigMenuExpanded = true
                        isFilterMenuExpanded = false
                        isLaunchMenuExpanded = false
                    } else {
                        isConfigMenuExpanded = false
                    }
                },
                onFilterClick = {
                    // Toggle filter overlay and close others.
                    if (!isFilterMenuExpanded) {
                        isFilterMenuExpanded = true
                        isConfigMenuExpanded = false
                        isLaunchMenuExpanded = false
                    } else {
                        isFilterMenuExpanded = false
                    }
                },
                onLaunchClick = {
                    // Toggle launch overlay and close others.
                    if (!isLaunchMenuExpanded) {
                        isLaunchMenuExpanded = true
                        isConfigMenuExpanded = false
                        isFilterMenuExpanded = false
                    } else {
                        isLaunchMenuExpanded = false
                    }
                },
                modifier = Modifier.align(Alignment.BottomCenter)
            )
            // Configuration Overlay.
            if (isConfigMenuExpanded) {
                ConfigMenuOverlay(
                    configList = configList,
                    onConfigSelected = { selectedConfig ->
                        viewModel.setActiveConfig(selectedConfig)
                    },
                    onNavigateToEditConfigs = { navController.navigate(Screen.ConfigList.route) },
                    onDismiss = { isConfigMenuExpanded = false },
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .offset(y = -(56.dp + 16.dp))
                )
            }
            // Filter Overlay.
            if (isFilterMenuExpanded) {
                FilterMenuOverlay(
                    isFilterActive = filterActive,
                    onToggleFilter = { filterActive = !filterActive },
                    hoursToShow = hoursToShow,
                    onHoursChanged = { hoursToShow = it },
                    onDismiss = { isFilterMenuExpanded = false },
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .offset(y = -(56.dp + 16.dp))
                )
            }
            // Launch Sites Overlay.
            if (isLaunchMenuExpanded) {
                LaunchSitesMenuOverlay(
                    launchSites = launchSites.filter { it.name != "Last Visited" }, // filter out "Last Visited"
                    onSiteSelected = { selectedSite ->
                        // Update the coordinates using the helper function.
                        viewModel.updateCoordinates(selectedSite.latitude, selectedSite.longitude)
                        // Reload forecast data for the new coordinates.
                        viewModel.loadForecast(selectedSite.latitude, selectedSite.longitude)
                        // Reload isobaric data – using current time.
                        viewModel.loadIsobaricData(selectedSite.latitude, selectedSite.longitude, Instant.now())
                        // Dismiss the launch overlay.
                        isLaunchMenuExpanded = false
                    },
                    onDismiss = { isLaunchMenuExpanded = false },
                    modifier = Modifier
                        .align(Alignment.BottomEnd) // For bottom-right placement.
                        .offset(x = (-16).dp, y = -(56.dp + 16.dp))
                )
            }
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
    hoursToShow: Float,
    viewModel: WeatherCardViewmodel
) {
    val screenHeight = LocalConfiguration.current.screenHeightDp.dp

    if (uiState is WeatherCardViewmodel.WeatherCardUiState.Success) {
        val forecastItems = uiState.forecastItems
        // Use the passed hoursToShow value for limiting forecast items.
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
        val forecastByDay: Map<String, List<ForecastDataItem>> =
            filteredItems.groupBy { it.time.substring(0, 10) }
        val sortedDays = forecastByDay.keys.sorted()
        val pagerState: PagerState = rememberPagerState(pageCount = { sortedDays.size })

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 16.dp)
        ) {
            // You can remove the header section that previously hosted the slider and toggle.
            // Optional: Place other header or title elements here if required.

            // Daily overview row.
            item {
                DailyForecastRowSection(forecastItems = forecastItems)
            }
            // Horizontal Pager section.
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(screenHeight)
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
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .fillMaxHeight()
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

