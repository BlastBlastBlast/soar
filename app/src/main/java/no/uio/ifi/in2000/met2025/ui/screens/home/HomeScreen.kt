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
import androidx.compose.runtime.saveable.rememberSaveable
import com.mapbox.maps.plugin.animation.MapAnimationOptions

@Composable
fun HomeScreen(
    viewModel: HomeScreenViewModel = hiltViewModel(),
    onNavigateToWeather: (Double, Double) -> Unit
) {
    val uiState           by viewModel.uiState.collectAsState()
    val coordinates       by viewModel.coordinates.collectAsState()
    val launchSites       by viewModel.launchSites.collectAsState()
    val updateStatus      by viewModel.updateStatus.collectAsState()
    val newMarker         by viewModel.newMarker.collectAsState()
    val newMarkerStatus   by viewModel.newMarkerStatus.collectAsState()
    val context           = LocalContext.current

    // UI state
    var isLaunchSiteMenuExpanded by rememberSaveable { mutableStateOf(false) }
    var showSaveDialog           by rememberSaveable { mutableStateOf(false) }
    var savedMarkerCoordinates   by rememberSaveable { mutableStateOf<Pair<Double, Double>?>(null) }
    var launchSiteName           by rememberSaveable { mutableStateOf("") }
    var isEditingMarker          by rememberSaveable { mutableStateOf(false) }
    var editingMarkerId          by rememberSaveable { mutableStateOf(0) }
    var showAnnotations          by rememberSaveable { mutableStateOf(true) }

    val coroutineScope = rememberCoroutineScope()

    // Shared viewport
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
            Box(
                Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }
        is HomeScreenViewModel.HomeScreenUiState.Error -> {
            val msg = (uiState as HomeScreenViewModel.HomeScreenUiState.Error).message
            Box(
                Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(text = "Error: $msg", color = MaterialTheme.colorScheme.error)
            }
        }
        is HomeScreenViewModel.HomeScreenUiState.Success -> {
            val state = uiState as HomeScreenViewModel.HomeScreenUiState.Success
            Box(Modifier.fillMaxSize()) {
                MapContainer(
                    coordinates                = coordinates,
                    newMarker                  = newMarker,
                    newMarkerStatus            = newMarkerStatus,
                    launchSites                = launchSites,
                    mapViewportState           = mapViewportState,
                    showAnnotations            = showAnnotations,
                    // user long-press → place new pin + save lat/lon/elev
                    onMapLongClick             = { pt, elev ->
                        viewModel.onMarkerPlaced(pt.latitude(), pt.longitude(), elev?: 0.0)
                    },
                    // tap on marker → move camera + update Last Visited with elevation
                    onMarkerAnnotationClick    = { pt, elev ->
                        viewModel.updateCoordinates(pt.latitude(), pt.longitude())
                        viewModel.updateLastVisited(
                            pt.latitude(), pt.longitude(), elev?: 0.0
                        )
                    },
                    // long-press pin → open save dialog (new marker)
                    onMarkerAnnotationLongPress= { pt, elev ->
                        viewModel.updateCoordinates(pt.latitude(), pt.longitude())
                        viewModel.updateLastVisited(
                            pt.latitude(), pt.longitude(), elev?: 0.0
                        )
                        isEditingMarker        = false
                        savedMarkerCoordinates = pt.latitude() to pt.longitude()
                        launchSiteName         = "New Marker"
                        showSaveDialog         = true
                    },
                    // double-tap existing site → move + update last visited from stored elevation
                    onLaunchSiteMarkerClick   = { site ->
                        viewModel.updateCoordinates(site.latitude, site.longitude)
                        viewModel.updateLastVisited(
                            site.latitude, site.longitude, site.elevation
                        )
                    },
                    // long-press existing site → edit name
                    onSavedMarkerAnnotationLongPress = { site ->
                        viewModel.updateCoordinates(site.latitude, site.longitude)
                        viewModel.updateLastVisited(
                            site.latitude, site.longitude, site.elevation
                        )
                        isEditingMarker        = true
                        editingMarkerId        = site.uid
                        savedMarkerCoordinates = site.latitude to site.longitude
                        launchSiteName         = site.name
                        showSaveDialog         = true
                    },
                    // background elevation back-fill
                    onSiteElevation            = { uid, elev ->
                        viewModel.updateSiteElevation(uid, elev)
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
                    enter   = expandVertically(tween(300)) + fadeIn(tween(300)),
                    exit    = shrinkVertically(tween(300)) + fadeOut(tween(300)),
                    modifier= Modifier
                        .align(Alignment.BottomStart)
                        .padding(start = 16.dp, bottom = 100.dp)
                ) {
                    LaunchSitesMenu(
                        launchSites  = state.launchSites.filter { it.name != "Last Visited" },
                        onSiteSelected = { site ->
                            coroutineScope.launch {
                                mapViewportState.easeTo(
                                    cameraOptions {
                                        center(Point.fromLngLat(site.longitude, site.latitude))
                                        zoom(14.0); pitch(0.0); bearing(0.0)
                                    },
                                    MapAnimationOptions.mapAnimationOptions { duration(1000L) }
                                )
                                viewModel.updateLastVisited(
                                    site.latitude, site.longitude, site.elevation
                                )
                            }
                            isLaunchSiteMenuExpanded = false
                        }
                    )
                }

                IconButton(
                    onClick = { showAnnotations = !showAnnotations },
                    modifier= Modifier
                        .align(Alignment.BottomCenter)
                        .padding(16.dp)
                        .size(36.dp)
                ) {
                    Icon(
                        imageVector     = if (showAnnotations) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                        contentDescription = "Toggle annotations"
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
                        onNameChange   = {
                            launchSiteName = it
                            viewModel.setUpdateStatusIdle()
                        },
                        onDismiss = {
                            showSaveDialog         = false
                            savedMarkerCoordinates = null
                            launchSiteName         = ""
                            isEditingMarker        = false
                            viewModel.setUpdateStatusIdle()
                        },
                        onConfirm = {
                            val (lat, lon) = savedMarkerCoordinates!!
                            if (isEditingMarker) {
                                viewModel.editLaunchSite(
                                    editingMarkerId,
                                    lat, lon,
                                    /* pass stored elevation through */ viewModel.launchSites.value
                                        .first { it.uid == editingMarkerId }.elevation,
                                    launchSiteName
                                )
                            } else {
                                // saving a brand‑new site: pull last‐visited elevation
                                val elev = viewModel.lastVisited.value?.elevation ?: 0.0
                                viewModel.addLaunchSite(lat, lon, elev, launchSiteName)
                                viewModel.updateNewMarker(lat, lon, elev)
                                if (updateStatus is HomeScreenViewModel.UpdateStatus.Success) {
                                    viewModel.setNewMarkerStatusFalse()
                                }
                            }
                            showSaveDialog = false
                        },
                        updateStatus = updateStatus
                    )
                }
            }
        }
    }
}
