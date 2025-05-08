package no.uio.ifi.in2000.met2025.ui.screens.weathercardscreen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.navigation.NavHostController
import no.uio.ifi.in2000.met2025.ui.navigation.Screen
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.style.TextAlign
import no.uio.ifi.in2000.met2025.data.local.database.ConfigProfile
import no.uio.ifi.in2000.met2025.data.local.database.LaunchSite
import no.uio.ifi.in2000.met2025.data.models.locationforecast.ForecastDataItem
import no.uio.ifi.in2000.met2025.data.models.safetyevaluation.LaunchStatus
import no.uio.ifi.in2000.met2025.data.models.safetyevaluation.ParameterState
import no.uio.ifi.in2000.met2025.data.models.safetyevaluation.evaluateLaunchConditions
import no.uio.ifi.in2000.met2025.data.models.safetyevaluation.launchStatus
import no.uio.ifi.in2000.met2025.domain.helpers.formatZuluTimeToLocal
import no.uio.ifi.in2000.met2025.ui.screens.weathercardscreen.components.DailyForecastCard
import no.uio.ifi.in2000.met2025.ui.screens.weathercardscreen.components.WeatherLoadingSpinner
import no.uio.ifi.in2000.met2025.ui.screens.weathercardscreen.components.config.ConfigMenuOverlay
import no.uio.ifi.in2000.met2025.ui.screens.weathercardscreen.components.SegmentedBottomBar
import no.uio.ifi.in2000.met2025.ui.screens.weathercardscreen.components.filter.FilterMenuOverlay
import no.uio.ifi.in2000.met2025.ui.screens.weathercardscreen.components.site.LaunchSitesMenuOverlay
import java.time.Instant
import java.time.ZonedDateTime


@Composable
fun WeatherCardScreen(
    viewModel: WeatherCardViewmodel = hiltViewModel(),
    navController: NavHostController
) {
    val uiState by viewModel.uiState.collectAsState()
    val activeConfig by viewModel.activeConfig.collectAsState()
    val configList by viewModel.configList.collectAsState()
    val coordinates by viewModel.coordinates.collectAsState()
    val launchSites by viewModel.launchSites.collectAsState(initial = emptyList())
    val currentSite by viewModel.currentSite.collectAsState()
    var hoursToShow by rememberSaveable { mutableStateOf(24f) }
    var filterActive by rememberSaveable { mutableStateOf(false) }
    var isConfigMenuExpanded by rememberSaveable { mutableStateOf(false) }
    var isFilterMenuExpanded by rememberSaveable { mutableStateOf(false) }
    var isLaunchMenuExpanded by rememberSaveable { mutableStateOf(false) }
    var isSunFilterActive by rememberSaveable { mutableStateOf(false) }
    var selectedStatuses by remember { mutableStateOf(setOf(LaunchStatus.SAFE, LaunchStatus.CAUTION, LaunchStatus.UNSAFE)) }
    val sitesForOverlay = remember(launchSites) {
        val allButLastVisited = launchSites.filter { it.name != "Last Visited" }
        val (newMarkerList, realSites) = allButLastVisited.partition { it.name == "New Marker" }
        val shouldShowNewMarker = newMarkerList.firstOrNull()?.let { nm ->
            realSites.none { it.latitude == nm.latitude && it.longitude == nm.longitude }
        } ?: false

        buildList { addAll(realSites)
            if (shouldShowNewMarker) add(newMarkerList.first())
        }
    }

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
                currentSite = currentSite,
                selectedStatuses = selectedStatuses,
                viewModel = viewModel,
                isSunFilterActive = isSunFilterActive
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
                        .offset(y = (10.dp))
                )
            }
            // Filter Overlay.
            if (isFilterMenuExpanded) {
                FilterMenuOverlay(
                    isFilterActive = filterActive,
                    onToggleFilter = { filterActive = !filterActive },
                    hoursToShow = hoursToShow,
                    onHoursChanged = { hoursToShow = it },
                    selectedStatuses = selectedStatuses,
                    onStatusToggled = { status ->
                        selectedStatuses = if (selectedStatuses.contains(status))
                            selectedStatuses - status
                        else
                            selectedStatuses + status
                    },
                    isSunFilterActive = isSunFilterActive,
                    onToggleSunFilter = { isSunFilterActive = !isSunFilterActive },
                    onDismiss = { isFilterMenuExpanded = false },
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .offset(y = (10.dp))
                )
            }
            // Launch Sites Overlay.
            if (isLaunchMenuExpanded) {
                LaunchSitesMenuOverlay(
                    launchSites = sitesForOverlay,
                    onSiteSelected = { selectedSite ->
                        viewModel.updateCoordinates(selectedSite.latitude, selectedSite.longitude)
                        viewModel.loadForecast(selectedSite.latitude, selectedSite.longitude)
                        viewModel.loadIsobaricData(selectedSite.latitude, selectedSite.longitude, Instant.now())
                        isLaunchMenuExpanded = false
                    },
                    onDismiss = { isLaunchMenuExpanded = false },
                    modifier = Modifier
                        .align(Alignment.BottomStart) // For bottom-right placement.
                        .offset(y = (10.dp)),

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
    selectedStatuses: Set<LaunchStatus>,
    currentSite: LaunchSite?,
    viewModel: WeatherCardViewmodel,
    isSunFilterActive: Boolean
) {
    val screenHeight = LocalConfiguration.current.screenHeightDp.dp

    if (uiState is WeatherCardViewmodel.WeatherCardUiState.Success) {
        val forecastItems = uiState.forecastItems

        val forecastByDay: Map<String, List<ForecastDataItem>> =
            forecastItems.groupBy { it.time.substring(0, 10) }

        val filteredItems = forecastItems
            .filter { item ->
                if (isSunFilterActive) {
                    val zonedTime = try {
                        ZonedDateTime.parse(item.time).toInstant()
                    } catch (e: Exception) {
                        return@filter false
                    }

                    val sunTimeForDay = uiState.sunTimes[item.time.substring(0, 10)] ?: return@filter false

                    val afterEarliest = zonedTime.isAfter(sunTimeForDay.earliestRocket)
                    val beforeLatest = zonedTime.isBefore(sunTimeForDay.latestRocket)

                    if (!(afterEarliest && beforeLatest)) return@filter false
                }

                val state = evaluateLaunchConditions(item, config)
                if (!filterActive) {
                    if (state !is ParameterState.Available) return@filter true
                    val status = launchStatus(state.relativeUnsafety)
                    return@filter status in selectedStatuses
                }

                if (state !is ParameterState.Available) return@filter false
                val status = launchStatus(state.relativeUnsafety)

                if (status == LaunchStatus.UNSAFE) return@filter false

                return@filter status in selectedStatuses
            }
            .take(hoursToShow.toInt())
        val filteredByDay: Map<String, List<ForecastDataItem>> =
            filteredItems.groupBy { it.time.substring(0, 10) }
        val sortedDays = forecastByDay.keys.sorted()
        val pagerState: PagerState = rememberPagerState(pageCount = { sortedDays.size })



        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 16.dp)
        ) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(screenHeight)
                ) {
                    HorizontalPager(
                        state = pagerState,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(screenHeight),
                        contentPadding = PaddingValues(horizontal = 16.dp),
                        pageSpacing = 16.dp,
                        pageSize = PageSize.Fill
                    ) { page ->
                        val date = sortedDays[page]
                        val dailyForecastItems = forecastByDay[date] ?: emptyList()
                        val hourlyFilteredItems = filteredByDay[date] ?: emptyList()
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .fillMaxHeight()
                                .verticalScroll(rememberScrollState())
                                .padding(vertical = 16.dp),
                            verticalArrangement = Arrangement.Top
                        ) {
                            SiteHeader(
                                site = currentSite,
                                coordinates = coordinates,
                                modifier = Modifier.fillMaxWidth()
                            )

                            DailyForecastCard(
                                forecastItems = dailyForecastItems,
                                modifier = Modifier.fillMaxWidth()
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            if (hourlyFilteredItems.isEmpty()) {
                                Text(
                                    text = "No results for selected filter",
                                    color = MaterialTheme.colorScheme.onBackground,
                                    style = MaterialTheme.typography.bodyLarge,
                                    modifier = Modifier
                                        .padding(16.dp)
                                        .fillMaxWidth(),
                                    textAlign = TextAlign.Center
                                )
                            } else {
                                hourlyFilteredItems.forEach { forecastItem ->
                                    HourlyExpandableCard(
                                        forecastItem = forecastItem,
                                        coordinates = coordinates,
                                        config = config,
                                        modifier = Modifier.padding(vertical = 8.dp),
                                        viewModel = viewModel
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(50.dp))
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

