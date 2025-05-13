/*
 * This screen displays an interactive Mapbox map with:
 *  - Markers for saved launch sites and a temporary user marker
 *  - Long-press to add a new marker
 *  - Rocket trajectory animation and 3D model rendering
 *
 * Special notes:
 *  - Uses Mapbox DEM to fetch terrain elevations
 *  - Parses coordinate input with parseLatLon helper
 *  - Longer load times due to massive calculatin with interpolation
 */
package no.uio.ifi.in2000.met2025.ui.screens.mapScreen

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
import no.uio.ifi.in2000.met2025.ui.screens.mapScreen.components.LaunchSitesButton
import no.uio.ifi.in2000.met2025.ui.screens.mapScreen.components.LaunchSitesMenu
import no.uio.ifi.in2000.met2025.ui.screens.mapScreen.components.SaveLaunchSiteDialog
import no.uio.ifi.in2000.met2025.ui.screens.mapScreen.components.WeatherNavigationButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.RocketLaunch
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.runtime.saveable.rememberSaveable
import com.mapbox.maps.plugin.animation.MapAnimationOptions
import no.uio.ifi.in2000.met2025.domain.helpers.parseLatLon
import no.uio.ifi.in2000.met2025.ui.screens.mapScreen.components.LatLonDisplay
import androidx.compose.material3.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.zIndex
import no.uio.ifi.in2000.met2025.R
import no.uio.ifi.in2000.met2025.ui.common.ErrorScreen
import no.uio.ifi.in2000.met2025.ui.screens.mapScreen.components.TrajectoryPopup

/**
 * Main composable that renders the map UI.
 *
 * @param viewModel Provides UI state and actions via Hilt
 * @param onNavigateToWeather Callback to open weather screen with given coords
 * @param onNavigateToRocketConfig Callback to open rocket configuration screen
 */

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapScreen(
    viewModel: MapScreenViewModel = hiltViewModel(),
    onNavigateToWeather: (Double, Double) -> Unit,
    onNavigateToRocketConfig: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

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

    val launchSiteName by viewModel.launchSiteName.collectAsState()
    var isEditingMarker by rememberSaveable { mutableStateOf(false) }
    var editingMarkerId by rememberSaveable { mutableStateOf(0) }
    var showAnnotations by rememberSaveable { mutableStateOf(true) }
    var coordsString by rememberSaveable { mutableStateOf("") }
    var parseError by rememberSaveable { mutableStateOf<String?>(null) }
    val currentSite by viewModel.currentSite.collectAsState()
    val rocketConfigs by viewModel.rocketConfigList.collectAsState()
    val selectedCfg by viewModel.selectedConfig.collectAsState()
    var showTrajectoryPopup by remember { mutableStateOf(false) }

    /**
     * Triggered on long-press: places a new marker at the clicked location
     * and updates its elevation if available.
     */
    val mapLongClick: (Point, Double?) -> Unit = { pt, elev ->
        viewModel.onMarkerPlaced(
            lat = pt.latitude(),
            lon = pt.longitude(),
            elevation = elev
        )
        viewModel.updateLaunchSiteName("New Marker")
    }


    val mapViewportState = rememberMapViewportState {
        setCameraOptions {
            center(Point.fromLngLat(coords.second, coords.first))
            zoom(12.0); pitch(0.0); bearing(0.0)
        }
    }

    when (uiState) {
        is MapScreenViewModel.MapScreenUiState.Loading -> {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(
                    modifier = Modifier.semantics {
                        contentDescription = "Loading map and data"
                    }
                )
            }
        }

        is MapScreenViewModel.MapScreenUiState.Error -> {
            val msg = (uiState as MapScreenViewModel.MapScreenUiState.Error).message
            ErrorScreen(
                errorMsg = msg,
                buttonText = "Reload Map",
                onReload = { viewModel.reloadScreen() })
        }

        is MapScreenViewModel.MapScreenUiState.Success -> {


            LaunchedEffect(showTrajectorySheet) {
                if (showTrajectorySheet) sheetState.show() else sheetState.hide()
            }

            /**
             * Updates the displayed lat/lon string whenever coords change,
             * and clears any previous parse errors.
             */
            LaunchedEffect(coords) {
                coordsString = "%.4f, %.4f".format(coords.first, coords.second)
                parseError = null
            }

            // Trigger to reload base style
            var styleReloadTrigger by rememberSaveable { mutableStateOf(0) }

            /**
             * Clears the current trajectory data and forces the map style to reload.
             */
            val handleClearTrajectory = {
                viewModel.clearTrajectory()
                styleReloadTrigger++
                showTrajectoryPopup = false
            }

            Box(Modifier
                .fillMaxSize()
                .semantics { contentDescription = "Map screen with map and controls" }
            ) {
                // Treat the map as an image with interactive markers
                Box(modifier = Modifier
                    .matchParentSize()
                    .semantics {
                        role = Role.Image
                        contentDescription =
                            "Map view showing launch sites and your current position"
                    }
                ) {
                    // Render the MapView composable with markers, trajectory, and elevation callbacks
                    MapView(
                        center = coords,
                        newMarker = newMarker,
                        newMarkerStatus = newMarkerStatus,
                        launchSites = launchSites,
                        mapViewportState = mapViewportState,
                        modifier = Modifier.matchParentSize(),
                        showAnnotations = showAnnotations,
                        onMapLongClick = mapLongClick,
                        onMarkerAnnotationClick = { pt, elev ->
                            viewModel.updateCoordinates(pt.latitude(), pt.longitude())
                            viewModel.updateLastVisited(pt.latitude(), pt.longitude(), elev)
                        },
                        onMarkerAnnotationLongPress = { pt, elev ->
                            viewModel.updateCoordinates(pt.latitude(), pt.longitude())
                            viewModel.updateLastVisited(pt.latitude(), pt.longitude(), elev)
                            isEditingMarker = false
                            viewModel.updateLaunchSiteName("New Marker")
                            showSaveDialog = true
                        },
                        onLaunchSiteMarkerClick = { site ->
                            viewModel.updateCoordinates(site.latitude, site.longitude)
                            viewModel.updateLastVisited(
                                site.latitude,
                                site.longitude,
                                site.elevation
                            )
                        },
                        onSavedMarkerAnnotationLongPress = { site ->
                            viewModel.updateCoordinates(site.latitude, site.longitude)
                            viewModel.updateLastVisited(
                                site.latitude,
                                site.longitude,
                                site.elevation
                            )
                            editingMarkerId = site.uid
                            savedMarkerCoordinates = site.latitude to site.longitude
                            viewModel.updateLaunchSiteName(site.name)
                            showSaveDialog = true
                            isEditingMarker = true
                        },
                        onSiteElevation = { uid, elev ->
                            viewModel.updateSiteElevation(uid, elev)
                        },
                        trajectoryPoints = trajectoryPoints,
                        isAnimating = isAnimating,
                        onAnimationEnd = { viewModel.isAnimating = false },
                        styleReloadTrigger  = styleReloadTrigger

                    )
                    // Floating button to open the trajectory simulation popup
                    if (!showTrajectoryPopup) {
                        ExtendedFloatingActionButton(
                            containerColor = MaterialTheme.colorScheme.surface,
                            icon = {
                                Icon(
                                    Icons.Default.RocketLaunch,
                                    contentDescription = null,
                                    modifier = Modifier
                                        .size(30.dp)
                                        .padding(4.dp),
                                    tint = MaterialTheme.colorScheme.onSurface
                                )
                            },
                            text = { Text("BALLISTIC\nTRAJECTORY") },
                            onClick = { showTrajectoryPopup = true },
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .padding(16.dp)
                                .semantics {
                                    contentDescription = "Start trajectory simulation"
                                }
                        )
                    }
                    // Popup for trajectory simulation
                    if (showTrajectoryPopup) {
                        TrajectoryPopup(
                            show = true,
                            lastVisited = viewModel.lastVisited.collectAsState().value,
                            currentSite = currentSite,
                            rocketConfigs = rocketConfigs,
                            selectedConfig = selectedCfg,
                            onSelectConfig = { viewModel.selectConfig(it) },
                            onClose = { showTrajectoryPopup = false },
                            onStartTrajectory = { viewModel.startTrajectory() },
                            onEditConfigs = onNavigateToRocketConfig,
                            onClearTrajectory = handleClearTrajectory,
                            modifier = Modifier
                                .fillMaxWidth()
                                .align(Alignment.BottomCenter)
                                .zIndex(1f)
                        )
                    }

                    Column(
                        Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        if (!showTrajectoryPopup) {
                            // Display the latitude and longitude input field
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
                    // Floating button to open the launch site menu
                    if (!showTrajectoryPopup) {
                        LaunchSitesButton(
                            Modifier
                                .align(Alignment.BottomStart)
                                .padding(16.dp)
                                .size(90.dp),
                            onClick = { isMenuExpanded = !isMenuExpanded }
                        )
                    }

                    AnimatedVisibility(
                        visible = isMenuExpanded,
                        enter = expandVertically(tween(300)) + fadeIn(tween(300)),
                        exit = shrinkVertically(tween(300)) + fadeOut(tween(300)),
                        modifier = Modifier
                            .align(Alignment.BottomStart)
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
                    // Floating button to toggle the visibility of map annotations
                    IconButton(
                        onClick = { showAnnotations = !showAnnotations },
                        modifier = Modifier
                            .align(Alignment.CenterEnd)
                            .padding(16.dp)
                            .size(36.dp)
                            .semantics {
                                contentDescription = if (showAnnotations)
                                    "Hide map annotations" else "Show map annotations"
                            }
                    ) {
                        Icon(
                            imageVector = if (showAnnotations)
                                Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                            contentDescription = null
                        )
                    }
                    // Floating button to navigate to the weather screen
                    if (!showTrajectoryPopup) {
                        WeatherNavigationButton(
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .padding(16.dp)
                                .size(90.dp),
                            latInput = coords.first.toString(),
                            lonInput = coords.second.toString(),
                            onNavigate = { lat, lon ->
                                viewModel.updateCoordinates(lat, lon)
                                onNavigateToWeather(lat, lon)
                            },
                            context = LocalContext.current
                        )
                    }
                    // Popup dialog for saving a launch site
                    if (showSaveDialog) {
                        SaveLaunchSiteDialog(
                            launchSiteName = launchSiteName,
                            onNameChange = {
                                viewModel.updateLaunchSiteName(it)
                                viewModel.setUpdateStatusIdle()
                            },
                            onDismiss = {
                                showSaveDialog = false
                                savedMarkerCoordinates = null
                                viewModel.updateLaunchSiteName("")
                                isEditingMarker = false
                                viewModel.setUpdateStatusIdle()
                            },
                            onConfirm = {
                                val elev: Double? = if (isEditingMarker) {
                                    viewModel.launchSites.value.first { it.uid == editingMarkerId }.elevation
                                } else {
                                    viewModel.lastVisited.value?.elevation
                                }
                                if (isEditingMarker) {
                                    viewModel.editLaunchSite(
                                        siteId = editingMarkerId,
                                        lat = savedMarkerCoordinates!!.first,
                                        lon = savedMarkerCoordinates!!.second,
                                        elevation = elev,
                                        name = launchSiteName
                                    )
                                } else {
                                    viewModel.addLaunchSite(
                                        newMarker!!.latitude,
                                        newMarker!!.longitude,
                                        elev,
                                        launchSiteName
                                    )
                                }
                            },
                            updateStatus = updateStatus
                        )
                    }
                    // Handle the update status of the launch site
                    LaunchedEffect(updateStatus) {
                        if (updateStatus is MapScreenViewModel.UpdateStatus.Success) {
                            showSaveDialog = false
                            savedMarkerCoordinates = null
                            viewModel.updateLaunchSiteName("")
                            if (isEditingMarker) {
                                isEditingMarker = false
                            } else {
                                viewModel.setNewMarkerStatusFalse()
                            }
                            viewModel.setUpdateStatusIdle()
                        }
                    }
                }
            }
        }
    }
}
