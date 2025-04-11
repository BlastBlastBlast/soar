package no.uio.ifi.in2000.met2025.ui.screens.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.mapbox.geojson.Point
import no.uio.ifi.in2000.met2025.ui.screens.home.components.CoordinateDisplay
import no.uio.ifi.in2000.met2025.ui.screens.home.components.LaunchSitesButton
import no.uio.ifi.in2000.met2025.ui.screens.home.components.LaunchSitesMenu
import no.uio.ifi.in2000.met2025.ui.screens.home.components.MapContainer
import no.uio.ifi.in2000.met2025.ui.screens.home.components.SaveLaunchSiteDialog
import no.uio.ifi.in2000.met2025.ui.screens.home.components.WeatherNavigationButton

import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff

@Composable
fun HomeScreen(
    viewModel: HomeScreenViewModel = hiltViewModel(),
    onNavigateToWeather: (Double, Double) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val coordinates by viewModel.coordinates.collectAsState()
    val context = LocalContext.current

    var isLaunchSiteMenuExpanded by remember { mutableStateOf(false) }
    var showSaveDialog by remember { mutableStateOf(false) }
    var launchSiteName by remember { mutableStateOf("") }
    var savedMarkerCoordinates by remember { mutableStateOf<Pair<Double, Double>?>(null) }
    // Optionally, you can add a toggle for annotation visibility if needed:
    var showAnnotations by remember { mutableStateOf(true) }

    when (uiState) {
        is HomeScreenViewModel.HomeScreenUiState.Loading -> {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }
        is HomeScreenViewModel.HomeScreenUiState.Error -> {
            val errorMessage = (uiState as HomeScreenViewModel.HomeScreenUiState.Error).message
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(text = "Error: $errorMessage", color = MaterialTheme.colorScheme.error)
            }
        }
        is HomeScreenViewModel.HomeScreenUiState.Success -> {
            val state = uiState as HomeScreenViewModel.HomeScreenUiState.Success
            Box(modifier = Modifier.fillMaxSize()) {
                MapContainer(
                    coordinates = coordinates,
                    // Use, for example, the first launch site as the initial marker if available.
                    initialMarkerCoordinate = state.launchSites.firstOrNull()?.let {
                        Point.fromLngLat(it.longitude, it.latitude)
                    },
                    launchSites = state.launchSites,
                    showAnnotations = showAnnotations,
                    onMarkerPlaced = { lat, lon ->
                        viewModel.onMarkerPlaced(lat, lon)
                    },
                    onMarkerAnnotationClick = { lat, lon ->
                        savedMarkerCoordinates = Pair(lat, lon)
                        showSaveDialog = true
                    },
                    onLaunchSiteMarkerClick = { site ->
                        // For example, update the map center when a launch site marker is tapped.
                        viewModel.updateCoordinates(site.latitude, site.longitude)
                    }
                )
                CoordinateDisplay(coordinates = coordinates)
                LaunchSitesButton(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(16.dp)
                        .size(90.dp),
                    onClick = { isLaunchSiteMenuExpanded = !isLaunchSiteMenuExpanded }
                )
                AnimatedVisibility(
                    visible = isLaunchSiteMenuExpanded,
                    enter = expandVertically(animationSpec = tween(durationMillis = 300)) +
                            fadeIn(animationSpec = tween(durationMillis = 300)),
                    exit = shrinkVertically(animationSpec = tween(durationMillis = 300)) +
                            fadeOut(animationSpec = tween(durationMillis = 300)),
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(start = 16.dp, bottom = 100.dp)
                ) {
                    LaunchSitesMenu(
                        launchSites = state.launchSites.filter { it.name != "Last Visited" },
                        onSiteSelected = { site ->
                            // Update map coordinates and update the "Last Visited" temporary marker.
                            viewModel.updateCoordinates(site.latitude, site.longitude)
                            viewModel.updateLastVisited(site.latitude, site.longitude)
                            isLaunchSiteMenuExpanded = false
                        }
                    )
                }
                // Toggle button for annotations:
                IconButton(
                    onClick = { showAnnotations = !showAnnotations },
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(16.dp)
                        .size(36.dp)
                ) {
                    Icon(
                        imageVector = if (showAnnotations) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                        contentDescription = "Toggle marker annotations"
                    )
                }
                WeatherNavigationButton(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(16.dp)
                        .size(90.dp),
                    latInput = coordinates.first.toString(),
                    lonInput = coordinates.second.toString(),
                    onNavigate = { lat, lon ->
                        viewModel.updateCoordinates(lat, lon)
                        onNavigateToWeather(lat, lon)
                    },
                    context = context
                )
                if (showSaveDialog && savedMarkerCoordinates != null) {
                    SaveLaunchSiteDialog(
                        launchSiteName = launchSiteName,
                        onNameChange = { launchSiteName = it },
                        onDismiss = { showSaveDialog = false },
                        onConfirm = {
                            val (lat, lon) = savedMarkerCoordinates!!
                            viewModel.addLaunchSite(lat, lon, launchSiteName)
                            showSaveDialog = false
                            launchSiteName = ""
                        }
                    )
                }
            }
        }
    }
}}