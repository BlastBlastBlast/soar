// HomeScreen.kt
package no.uio.ifi.in2000.met2025.ui.screens.home

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
import no.uio.ifi.in2000.met2025.ui.screens.home.components.LaunchSitesButton
import no.uio.ifi.in2000.met2025.ui.screens.home.components.LaunchSitesMenu
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
import no.uio.ifi.in2000.met2025.ui.screens.home.components.MapContainer
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.zIndex
import no.uio.ifi.in2000.met2025.R
import no.uio.ifi.in2000.met2025.ui.screens.home.components.TrajectoryPopup
import no.uio.ifi.in2000.met2025.ui.screens.home.maps.MapView

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: HomeScreenViewModel = hiltViewModel(),
    onNavigateToWeather: (Double, Double) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

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
            val coords by viewModel.coordinates.collectAsState()
            val launchSites by viewModel.launchSites.collectAsState()
            val updateStatus by viewModel.updateStatus.collectAsState()
            val newMarker by viewModel.newMarker.collectAsState()
            val newMarkerStatus by viewModel.newMarkerStatus.collectAsState()
            var savedMarkerCoordinates by rememberSaveable {
                mutableStateOf<Pair<Double, Double>?>(
                    null
                )
            }

            val trajectoryPoints by viewModel.trajectoryPoints.collectAsState()
            val isAnimating = viewModel.isAnimating
            var showTrajectorySheet by remember { mutableStateOf(false) }
            val sheetState = rememberModalBottomSheetState()
            val scope = rememberCoroutineScope()
            var isMenuExpanded by rememberSaveable { mutableStateOf(false) }
            var showSaveDialog by rememberSaveable { mutableStateOf(false) }

            var launchSiteName by rememberSaveable { mutableStateOf("") }
            var isEditingMarker by rememberSaveable { mutableStateOf(false) }
            var editingMarkerId by rememberSaveable { mutableStateOf(0) }
            var showAnnotations by rememberSaveable { mutableStateOf(true) }
            var coordsString by rememberSaveable { mutableStateOf("") }
            var parseError by rememberSaveable { mutableStateOf<String?>(null) }
            val lastVisitedSite by viewModel.lastVisited.collectAsState()
            val trajectory3D by viewModel.trajectoryPoints.collectAsState()
            val configs by viewModel.rocketConfigList.collectAsState()
            val selectedCfg by viewModel.selectedConfig.collectAsState()
            var showTrajectoryPopup by remember { mutableStateOf(false) }
            val makeTraj by remember { derivedStateOf { viewModel.isTrajectoryMode } }
            val fakeLongClick: (Point, Double?) -> Unit = { pt, elev ->
                viewModel.onMarkerPlaced(
                    lat = pt.latitude(),
                    lon = pt.longitude(),
                    elevation = elev
                )
                launchSiteName = "New Marker"
            }

            // Map viewport state
            val mapViewportState = rememberMapViewportState {
                setCameraOptions {
                    center(Point.fromLngLat(coords.second, coords.first))
                    zoom(12.0); pitch(0.0); bearing(0.0)
                }
            }

            LaunchedEffect(showTrajectorySheet) {
                if (showTrajectorySheet) sheetState.show() else sheetState.hide()
            }

            LaunchedEffect(coords) {
                coordsString = "%.4f, %.4f".format(coords.first, coords.second)
                parseError = null
            }


            Box(Modifier.fillMaxSize()) {
                // A) Map + annotations (inlined MapView instead of MapContainer)
                MapView(
                    center                   = coords,
                    newMarker                = newMarker,
                    newMarkerStatus          = newMarkerStatus,
                    launchSites              = launchSites,
                    mapViewportState         = mapViewportState,
                    modifier                 = Modifier.matchParentSize(),
                    showAnnotations          = showAnnotations,
                    onMapLongClick           = fakeLongClick,
                    onMarkerAnnotationClick  = { pt, elev ->
                        viewModel.updateCoordinates(pt.latitude(), pt.longitude())
                        viewModel.updateLastVisited(pt.latitude(), pt.longitude(), elev)
                    },
                    onMarkerAnnotationLongPress = { pt, elev ->
                        viewModel.updateCoordinates(pt.latitude(), pt.longitude())
                        viewModel.updateLastVisited(pt.latitude(), pt.longitude(), elev)
                        isEditingMarker = false
                        launchSiteName = "New Marker"
                        showSaveDialog = true
                    },
                    onLaunchSiteMarkerClick      = { site ->
                        viewModel.updateCoordinates(site.latitude, site.longitude)
                        viewModel.updateLastVisited(site.latitude, site.longitude, site.elevation)
                    },
                    onSavedMarkerAnnotationLongPress = { site ->
                        viewModel.updateCoordinates(site.latitude, site.longitude)
                        viewModel.updateLastVisited(site.latitude, site.longitude, site.elevation)
                        isEditingMarker = true
                        editingMarkerId = site.uid
                        savedMarkerCoordinates = site.latitude to site.longitude
                        launchSiteName = site.name
                        showSaveDialog = true
                    },
                    onSiteElevation = { uid, elev ->
                        viewModel.updateSiteElevation(uid, elev)
                    },
                    trajectoryPoints = trajectoryPoints,
                    isAnimating       = isAnimating,
                    onAnimationEnd    = { viewModel.isAnimating = false }
                )

                ExtendedFloatingActionButton(
                    icon = {
                        Icon(
                            painter = painterResource(id = R.drawable.missile),
                            contentDescription = null,
                            modifier = Modifier.size(30.dp).padding(4.dp),
                            tint = Color.Black // Set the desired color here
                        )
                    },
                    text = { Text("Trajectory") },
                    onClick = { showTrajectoryPopup = true },
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(16.dp)
                )

                // B) Now the popup with carousel inside
                if (showTrajectoryPopup) {
                    TrajectoryPopup(
                        show = true,
                        lastVisited = viewModel.lastVisited.collectAsState().value,
                        configs = configs,                    // ← pass list
                        selectedConfig = selectedCfg,                // ← pass default
                        onSelectConfig = { viewModel.selectConfig(it) },
                        onClose = { showTrajectoryPopup = false },
                        onStartTrajectory = { viewModel.startTrajectory() },
                        onEditConfigs = { },
                        onClearTrajectory = {
                            viewModel.clearTrajectory()
                            showTrajectoryPopup = false
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .align(Alignment.BottomCenter)
                            .zIndex(1f)
                    )
                }

                // B) Lat/Lon input & Done
                Column(
                    Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    if (!showTrajectoryPopup) {
                        LatLonDisplay(
                            coordinates = coordsString,
                            onCoordinatesChange = { coordsString = it },
                            onDone = {
                                parseLatLon(coordsString)?.let { (lat, lon) ->
                                    viewModel.onMarkerPlaced(lat, lon, null)
                                    scope.launch {
                                        mapViewportState.easeTo(
                                            cameraOptions {
                                                center(Point.fromLngLat(lon, lat))
                                                zoom(mapViewportState.cameraState?.zoom ?: 12.0)
                                            },
                                            MapAnimationOptions.mapAnimationOptions {
                                                duration(
                                                    500L
                                                )
                                            }
                                        )
                                    }
                                } ?: run { parseError = "Invalid format" }
                            },
                            modifier = Modifier.align(Alignment.CenterHorizontally)
                        )
                        parseError?.let { Text(it, color = MaterialTheme.colorScheme.error) }
                    }
                }

                // C) Launch sites menu
                LaunchSitesButton(
                    Modifier.align(Alignment.BottomStart).padding(16.dp).size(90.dp),
                    onClick = { isMenuExpanded = !isMenuExpanded }
                )
                AnimatedVisibility(
                    visible = isMenuExpanded,
                    enter = expandVertically(tween(300)) + fadeIn(tween(300)),
                    exit = shrinkVertically(tween(300)) + fadeOut(tween(300)),
                    modifier = Modifier.align(Alignment.BottomStart)
                        .padding(start = 16.dp, bottom = 100.dp)
                ) {
                    LaunchSitesMenu(
                        launchSites = launchSites.filter { it.name != "Last Visited" },
                        onSiteSelected = { site ->
                            scope.launch {
                                mapViewportState.easeTo(
                                    cameraOptions {
                                        center(Point.fromLngLat(site.longitude, site.latitude))
                                        zoom(14.0); pitch(0.0); bearing(0.0)
                                    },
                                    MapAnimationOptions.mapAnimationOptions { duration(500L) }
                                )
                                viewModel.updateLastVisited(
                                    site.latitude,
                                    site.longitude,
                                    site.elevation
                                )
                            }
                            isMenuExpanded = false
                        }
                    )
                }

                // D) Toggle annotations
                IconButton(
                    onClick = { showAnnotations = !showAnnotations },
                    modifier = Modifier.align(Alignment.BottomCenter).padding(16.dp).size(36.dp)
                ) {
                    Icon(
                        imageVector = if (showAnnotations) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                        contentDescription = "Toggle annotations"
                    )
                }

                // E) Weather navigation
                WeatherNavigationButton(
                    modifier = Modifier.align(Alignment.BottomEnd).padding(16.dp).size(90.dp),
                    latInput = coords.first.toString(),
                    lonInput = coords.second.toString(),
                    onNavigate = { lat, lon ->
                        viewModel.updateCoordinates(lat, lon)
                        onNavigateToWeather(lat, lon)
                    },
                    context = LocalContext.current
                )

                // F) Save/Edit dialog
                if (showSaveDialog) {
                    SaveLaunchSiteDialog(
                        launchSiteName = launchSiteName,
                        onNameChange = {
                            launchSiteName = it
                            viewModel.setUpdateStatusIdle()
                        },
                        onDismiss = {
                            showSaveDialog = false
                            savedMarkerCoordinates = null
                            launchSiteName = ""
                            isEditingMarker = false
                            viewModel.setUpdateStatusIdle()
                        },
                        onConfirm = {
                            val elev: Double? = if (isEditingMarker) {
                                viewModel.launchSites.value.first { it.uid == editingMarkerId }.elevation
                            } else {
                                viewModel.lastVisited.value?.elevation
                            }
                            //Null safety for savedMarkerCoordinates is that the only operation that sets isEditingMarker
                            //to true also updates savedMarkerCoordinates
                            if (isEditingMarker) {
                                viewModel.editLaunchSite(
                                    editingMarkerId,
                                    savedMarkerCoordinates!!.first, savedMarkerCoordinates!!.second,
                                    elev,
                                    launchSiteName
                                )
                            } else {
                                viewModel.addLaunchSite(newMarker!!.latitude, newMarker!!.longitude, elev, launchSiteName)
                            }
                        },
                        updateStatus = updateStatus
                    )
                }
                LaunchedEffect(updateStatus) {
                    if (updateStatus is HomeScreenViewModel.UpdateStatus.Success) {
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
