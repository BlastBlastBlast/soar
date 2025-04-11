// HomeScreen.kt
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
import com.mapbox.maps.dsl.cameraOptions
import com.mapbox.maps.extension.compose.animation.viewport.rememberMapViewportState
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
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
import com.mapbox.maps.plugin.animation.MapAnimationOptions

@Composable
fun HomeScreen(
    viewModel: HomeScreenViewModel = hiltViewModel(),
    onNavigateToWeather: (Double, Double) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val coordinates by viewModel.coordinates.collectAsState()
    val launchSites by viewModel.launchSites.collectAsState()
    val context = LocalContext.current
    var isLaunchSiteMenuExpanded by remember { mutableStateOf(false) }
    var showSaveDialog by remember { mutableStateOf(false) }
    // We'll use these states for both adding new markers and editing saved markers.
    var savedMarkerCoordinates by remember { mutableStateOf<Pair<Double, Double>?>(null) }
    var launchSiteName by remember { mutableStateOf("") }
    var isEditingMarker by remember { mutableStateOf(false) } // false: new marker; true: editing existing marker
    var editingMarkerId by remember { mutableStateOf(0) }
    var showAnnotations by remember { mutableStateOf(true) }
    val coroutineScope = rememberCoroutineScope()

    // Shared MapViewportState.
    val mapViewportState = rememberMapViewportState {
        setCameraOptions {
            center(Point.fromLngLat(coordinates.second, coordinates.first))
            zoom(12.0)
            pitch(0.0)
            bearing(0.0)
        }
    }

    when (uiState) {
        is HomeScreenViewModel.HomeScreenUiState.Loading -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }
        is HomeScreenViewModel.HomeScreenUiState.Error -> {
            val errorMessage = (uiState as HomeScreenViewModel.HomeScreenUiState.Error).message
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(text = "Error: $errorMessage", color = MaterialTheme.colorScheme.error)
            }
        }
        is HomeScreenViewModel.HomeScreenUiState.Success -> {
            val state = uiState as HomeScreenViewModel.HomeScreenUiState.Success
            Box(modifier = Modifier.fillMaxSize()) {
                MapContainer(
                    coordinates = coordinates,
                    temporaryMarker = null,  // For demonstration, replace this with your temporary marker if needed.
                    launchSites = launchSites,
                    mapViewportState = mapViewportState,
                    showAnnotations = showAnnotations,
                    onMapLongClick = { point ->
                        // When the user long presses the map, place a temporary marker.
                        viewModel.onMarkerPlaced(point.latitude(), point.longitude())
                    },
                    onMarkerAnnotationClick = { point ->
                        // Optionally handle single-tap on a temporary marker.
                    },
                    onMarkerAnnotationLongPress = { point ->
                        // Temporary marker: open the save dialog (for new marker).
                        isEditingMarker = false
                        savedMarkerCoordinates = Pair(point.latitude(), point.longitude())
                        launchSiteName = "New Marker"
                        showSaveDialog = true
                    },
                    onLaunchSiteMarkerClick = { site ->
                        // Optional extra behavior on double-tap.
                    },
                    onSavedMarkerAnnotationLongPress = { site ->
                        // Saved marker: open the edit dialog.
                        isEditingMarker = true
                        editingMarkerId = site.uid
                        savedMarkerCoordinates = Pair(site.latitude, site.longitude)
                        launchSiteName = site.name  // Pre-fill with the current name.
                        showSaveDialog = true
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
                    enter = expandVertically(animationSpec = tween(300)) + fadeIn(animationSpec = tween(300)),
                    exit = shrinkVertically(animationSpec = tween(300)) + fadeOut(animationSpec = tween(300)),
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(start = 16.dp, bottom = 100.dp)
                ) {
                    LaunchSitesMenu(
                        launchSites = state.launchSites.filter { it.name != "Last Visited" },
                        onSiteSelected = { site ->
                            coroutineScope.launch {
                                mapViewportState.easeTo(
                                    cameraOptions {
                                        center(Point.fromLngLat(site.longitude, site.latitude))
                                        zoom(14.0)
                                        pitch(0.0)
                                        bearing(0.0)
                                    },
                                    MapAnimationOptions.mapAnimationOptions { duration(1000L) }
                                )
                                viewModel.updateLastVisited(site.latitude, site.longitude)
                            }
                            isLaunchSiteMenuExpanded = false
                        }
                    )
                }

               /* // Show the save/edit dialog.
                if (showSaveDialog && savedMarkerCoordinates != null) {
                    SaveLaunchSiteDialog(
                        launchSiteName = launchSiteName,
                        onNameChange = { launchSiteName = it },
                        onDismiss = {
                            showSaveDialog = false
                            savedMarkerCoordinates = null
                            launchSiteName = ""
                        },
                        onConfirm = {
                            val (lat, lon) = savedMarkerCoordinates!!
                            if (isEditingMarker) {
                                // For editing, add a new marker with the edited values.
                                viewModel.addLaunchSite(lat, lon, launchSiteName)
                            } else {
                                // For a new marker.
                                viewModel.addLaunchSite(lat, lon, launchSiteName)
                                // Update the new marker placeholder so that next time it is fresh.
                                viewModel.updateNewMarker(lat, lon)
                            }
                            showSaveDialog = false
                            savedMarkerCoordinates = null
                            launchSiteName = ""
                        }
                    )
                } */
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
}
