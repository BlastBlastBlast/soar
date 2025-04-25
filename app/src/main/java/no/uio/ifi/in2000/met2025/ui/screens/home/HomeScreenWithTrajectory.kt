import no.uio.ifi.in2000.met2025.ui.screens.home.HomeScreen
import no.uio.ifi.in2000.met2025.ui.screens.home.HomeScreenViewModel

/*package no.uio.ifi.in2000.met2025.ui.screens.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Flight
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.mapbox.maps.extension.compose.animation.viewport.rememberMapViewportState
import kotlinx.coroutines.launch

// HomeScreenWithTrajectory.kt

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize

import androidx.compose.material3.*
import androidx.compose.runtime.*

import com.mapbox.geojson.Point
import no.uio.ifi.in2000.met2025.ui.screens.home.components.MapContainer


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreenWithTrajectory(
    viewModel: HomeScreenViewModel = hiltViewModel(),
    onNavigateToWeather: (Double, Double) -> Unit
) {
    // 1) UI state
    val uiState         by viewModel.uiState.collectAsState()
    val coords          by viewModel.coordinates.collectAsState()
    val launchSites     by viewModel.launchSites.collectAsState()
    val newMarker       by viewModel.newMarker.collectAsState()
    val newMarkerStatus by viewModel.newMarkerStatus.collectAsState()

    // 2) Trajectory state from VM
    // collect your StateFlow directly into a Compose State
    val trajectoryPoints = viewModel.trajectoryPoints
    val isAnimating      = viewModel.isAnimating

    // 3) Local sheet state & flag
    var showSheet by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState()
    val scope      = rememberCoroutineScope()

    // Whenever our flag flips, actually show or hide the sheet
    LaunchedEffect(showSheet) {
        if (showSheet) sheetState.show() else sheetState.hide()
    }

    Scaffold(
        floatingActionButton = {
            ExtendedFloatingActionButton(
                icon        = { Icon(Icons.Default.Flight, "") },
                text        = { Text("Trajectory") },
                onClick     = { showSheet = true }
            )
        }
    ) { padding ->
        Box(Modifier
            .fillMaxSize()
            .padding(padding)
        ) {
            if (!showSheet) {
                HomeScreen(viewModel, onNavigateToWeather)
            } else {
                MapContainer(
                    coordinates            = coords,
                    newMarker              = newMarker,
                    newMarkerStatus        = newMarkerStatus,
                    launchSites            = launchSites,
                    mapViewportState       = rememberMapViewportState {
                        setCameraOptions {
                            center(Point.fromLngLat(coords.second, coords.first))
                            zoom(12.0)
                        }
                    },
                    showAnnotations        = false,
                    onMapLongClick         = { _, _ -> },
                    onMarkerAnnotationClick= { _, _ -> },
                    onMarkerAnnotationLongPress = { _, _ -> },
                    onLaunchSiteMarkerClick    = { },
                    onSavedMarkerAnnotationLongPress = { },
                    onSiteElevation            = { _, _ -> },
                    trajectoryPoints      = trajectoryPoints,
                    isAnimating           = isAnimating,
                    onAnimationEnd        = { viewModel.isAnimating = false },
                    modifier              = Modifier.fillMaxSize()
                )
            }
        }

        // 5) The ModalBottomSheet itself
        if (showSheet) {
            ModalBottomSheet(
                onDismissRequest = { showSheet = false },
                sheetState       = sheetState,
            ) {
                TrajectoryControlSheet(
                    onStartTrajectory   = { viewModel.startTrajectory() },
                    onPickRocketConfig  = { viewModel.showRocketConfigDialog() },
                    onShowCurrentLatLon = { viewModel.showCurrentLatLon() },
                    onLaunchAtMapCenter = { viewModel.launchHere() },
                    onMinimize = {
                        scope.launch {
                            sheetState.hide()
                        }.invokeOnCompletion {
                            // once it's fully hidden, remove it from composition
                            if (!sheetState.isVisible) {
                                showSheet = false
                            }
                        }
                    }
                )
            }
        }
    }
}


@Composable
fun TrajectoryControlSheet(
    onStartTrajectory: () -> Unit,
    onPickRocketConfig: () -> Unit,
    onShowCurrentLatLon: () -> Unit,
    onLaunchAtMapCenter: () -> Unit,
    onMinimize: () -> Unit
) {
    Column(
        Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {
            IconButton(onClick = onMinimize) {
                Icon(Icons.Default.ExpandMore, contentDescription = "Minimize")
            }
        }
        Button(onClick = onStartTrajectory, Modifier.fillMaxWidth()) {
            Text("▶️ Start Trajectory")
        }
        Button(onClick = onPickRocketConfig, Modifier.fillMaxWidth()) {
            Text("⚙️ Rocket Configs")
        }
        Button(onClick = onShowCurrentLatLon, Modifier.fillMaxWidth()) {
            Text("📍 Show Current Lat/Lon")
        }
        Button(onClick = onLaunchAtMapCenter, Modifier.fillMaxWidth()) {
            Text("🚀 Launch From Center")
        }
    }
}
*/