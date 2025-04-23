// HomeScreen.kt
package no.uio.ifi.in2000.met2025.ui.screens.home

import android.provider.ContactsContract.CommonDataKinds.Note.NOTE
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
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
import no.uio.ifi.in2000.met2025.domain.helpers.parseLatLon
import no.uio.ifi.in2000.met2025.ui.common.LatLonDisplay

@Composable
fun HomeScreen(
    viewModel: HomeScreenViewModel = hiltViewModel(),
    onNavigateToWeather: (Double, Double) -> Unit
) {
    val uiState         by viewModel.uiState.collectAsState()
    val coordinates     by viewModel.coordinates.collectAsState()
    val launchSites     by viewModel.launchSites.collectAsState()
    val updateStatus    by viewModel.updateStatus.collectAsState()
    val newMarker       by viewModel.newMarker.collectAsState()
    val newMarkerStatus by viewModel.newMarkerStatus.collectAsState()
    val context         = LocalContext.current
    val coroutineScope  = rememberCoroutineScope()

    // Single coordinate input box state & error
    var coordsString by rememberSaveable { mutableStateOf("") }
    var parseError  by rememberSaveable { mutableStateOf<String?>(null) }

    // Sync when map center changes
    LaunchedEffect(coordinates) {
        coordsString = "%.4f, %.4f".format(
            coordinates.first,
            coordinates.second
        )
        parseError = null
    }

    // UI flags
    var isMenuExpanded         by rememberSaveable { mutableStateOf(false) }
    var showSaveDialog         by rememberSaveable { mutableStateOf(false) }
    var savedMarkerCoordinates by rememberSaveable { mutableStateOf<Pair<Double, Double>?>(null) }
    var launchSiteName         by rememberSaveable { mutableStateOf("") }
    var isEditingMarker        by rememberSaveable { mutableStateOf(false) }
    var editingMarkerId        by rememberSaveable { mutableStateOf(0) }
    var showAnnotations        by rememberSaveable { mutableStateOf(true) }
    val fakeLongClick: (Point, Double?) -> Unit = { pt, elev ->
        // this is the same lambda you pass into MapContainer
        viewModel.onMarkerPlaced(pt.latitude(), pt.longitude(), elev ?: 0.0)
    }
    // Map viewport
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
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }
        is HomeScreenViewModel.HomeScreenUiState.Error -> {
            val msg = (uiState as HomeScreenViewModel.HomeScreenUiState.Error).message
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Error: $msg", color = MaterialTheme.colorScheme.error)
            }
        }
        is HomeScreenViewModel.HomeScreenUiState.Success -> {
            val state = uiState as HomeScreenViewModel.HomeScreenUiState.Success

            Box(Modifier.fillMaxSize()) {
                // 1) Fullscreen map
                MapContainer(
                    coordinates                    = coordinates,
                    newMarker                      = newMarker,
                    newMarkerStatus                = newMarkerStatus,
                    launchSites                    = launchSites,
                    mapViewportState               = mapViewportState,
                    showAnnotations                = showAnnotations,
                    onMapLongClick                 = fakeLongClick,

                    onMarkerAnnotationClick        = { pt, elev ->
                        viewModel.updateCoordinates(pt.latitude(), pt.longitude())
                        viewModel.updateLastVisited(pt.latitude(), pt.longitude(), elev ?: 0.0)
                    },
                    onMarkerAnnotationLongPress    = { pt, elev ->
                        viewModel.updateCoordinates(pt.latitude(), pt.longitude())
                        viewModel.updateLastVisited(pt.latitude(), pt.longitude(), elev ?: 0.0)
                        isEditingMarker        = false
                        savedMarkerCoordinates = pt.latitude() to pt.longitude()
                        launchSiteName         = "New Marker"
                        showSaveDialog         = true
                    },
                    onLaunchSiteMarkerClick       = { site ->
                        viewModel.updateCoordinates(site.latitude, site.longitude)
                        viewModel.updateLastVisited(site.latitude, site.longitude, site.elevation)
                    },
                    onSavedMarkerAnnotationLongPress = { site ->
                        viewModel.updateCoordinates(site.latitude, site.longitude)
                        viewModel.updateLastVisited(site.latitude, site.longitude, site.elevation)
                        isEditingMarker        = true
                        editingMarkerId        = site.uid
                        savedMarkerCoordinates = site.latitude to site.longitude
                        launchSiteName         = site.name
                        showSaveDialog         = true
                    },
                    onSiteElevation                = { uid, elev ->
                        viewModel.updateSiteElevation(uid, elev)
                    },
                    modifier                       = Modifier.matchParentSize()
                )

                // 2) Floating LatLonDisplay with Done icon
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    LatLonDisplay(
                        coordinates         = coordsString,
                        onCoordinatesChange = { coordsString = it },
                        onDone              = {
                            parseLatLon(coordsString)?.let { (lat, lon) ->
                                // exactly same as long‑press handler:
                                viewModel.onMarkerPlaced(lat, lon, 0.0)
                                coroutineScope.launch {
                                    mapViewportState.easeTo(
                                        cameraOptions {
                                            center(Point.fromLngLat(lon, lat))
                                            zoom(mapViewportState.cameraState?.zoom ?: 12.0)
                                        },
                                        MapAnimationOptions.mapAnimationOptions { duration(500L) }
                                    )
                                }
                            }
                        },
                        modifier = Modifier.align(Alignment.CenterHorizontally
                        )
                    )
                    parseError?.let { err ->
                        Text(
                            text = err,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(start = 8.dp, top = 4.dp)
                        )
                    }
                }

                // 3) Menu toggle
                LaunchSitesButton(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(16.dp)
                        .size(90.dp),
                    onClick = { isMenuExpanded = !isMenuExpanded }
                )

                // 4) Animated menu
                AnimatedVisibility(
                    visible  = isMenuExpanded,
                    enter    = expandVertically(tween(300)) + fadeIn(tween(300)),
                    exit     = shrinkVertically(tween(300)) + fadeOut(tween(300)),
                    modifier = Modifier.align(Alignment.BottomStart).padding(start = 16.dp, bottom = 100.dp)
                ) {
                    LaunchSitesMenu(
                        launchSites    = state.launchSites.filter { it.name != "Last Visited" },
                        onSiteSelected = { site ->
                            coroutineScope.launch {
                                mapViewportState.easeTo(
                                    cameraOptions {
                                        center(Point.fromLngLat(site.longitude, site.latitude))
                                        zoom(14.0); pitch(0.0); bearing(0.0)
                                    },
                                    MapAnimationOptions.mapAnimationOptions { duration(500L) }
                                )
                                viewModel.updateLastVisited(site.latitude, site.longitude, site.elevation)
                            }
                            isMenuExpanded = false
                        }
                    )
                }

                // 5) Toggle annotations
                IconButton(
                    onClick  = { showAnnotations = !showAnnotations },
                    modifier = Modifier.align(Alignment.BottomCenter).padding(16.dp).size(36.dp)
                ) {
                    Icon(
                        imageVector     = if (showAnnotations) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                        contentDescription = "Toggle annotations"
                    )
                }

                // 6) Weather button
                WeatherNavigationButton(
                    modifier = Modifier.align(Alignment.BottomEnd).padding(16.dp).size(90.dp),
                    latInput   = coordinates.first.toString(),
                    lonInput   = coordinates.second.toString(),
                    onNavigate = { lat, lon ->
                        viewModel.updateCoordinates(lat, lon)
                        onNavigateToWeather(lat, lon)
                    },
                    context = context
                )

                // 7) Save/Edit dialog
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
                                    viewModel.launchSites.value.first { it.uid == editingMarkerId }.elevation,
                                    launchSiteName
                                )
                            } else {
                                val elev = viewModel.lastVisited.value?.elevation ?: 0.0
                                viewModel.addLaunchSite(lat, lon, elev, launchSiteName)
                                viewModel.updateNewMarker(lat, lon, elev)
                                if (updateStatus is HomeScreenViewModel.UpdateStatus.Success) {
                                    viewModel.setNewMarkerStatusFalse()
                                }
                            }
                            // NOTE: DO NOT CLOSE DIALOG HERE!
                        },
                        updateStatus = updateStatus
                    )
                }
                // React to successful save (not on confirm click)
                LaunchedEffect(updateStatus) {
                    if (updateStatus is HomeScreenViewModel.UpdateStatus.Success) {
                        // Close and reset all state
                        showSaveDialog = false
                        savedMarkerCoordinates = null
                        launchSiteName = ""
                        isEditingMarker = false
                        viewModel.setUpdateStatusIdle()
                        viewModel.setNewMarkerStatusFalse()
                    }
                }
            }
        }
    }
}
