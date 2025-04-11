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
import no.uio.ifi.in2000.met2025.data.local.database.LaunchSite
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.compose.runtime.rememberCoroutineScope

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
    var launchSiteName by remember { mutableStateOf("") }
    var savedMarkerCoordinates by remember { mutableStateOf<Pair<Double, Double>?>(null) }
    var showAnnotations by remember { mutableStateOf(true) }
    val coroutineScope = rememberCoroutineScope()

    when (uiState) {
        is HomeScreenViewModel.HomeScreenUiState.Loading -> {
            // Loading UI omitted for brevity.
        }
        is HomeScreenViewModel.HomeScreenUiState.Error -> {
            // Error UI omitted for brevity.
        }
        is HomeScreenViewModel.HomeScreenUiState.Success -> {
            val state = uiState as HomeScreenViewModel.HomeScreenUiState.Success
            Box(modifier = Modifier.fillMaxSize()) {
                MapContainer(
                    coordinates = coordinates,
                    temporaryMarker = null,
                    launchSites = launchSites,
                    showAnnotations = showAnnotations,
                    onMapLongClick = { point ->
                        viewModel.onMarkerPlaced(point.latitude(), point.longitude())
                    },
                    onMarkerAnnotationClick = { point ->
                        // Handle temporary marker tap.
                    },
                    onLaunchSiteMarkerClick = { site ->
                        // Additional handling on saved marker double-tap (if needed).
                        // Note: The camera animation is now handled within MapView.
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
                    enter = expandVertically(animationSpec = tween(300)) +
                            fadeIn(animationSpec = tween(300)),
                    exit = shrinkVertically(animationSpec = tween(300)) +
                            fadeOut(animationSpec = tween(300)),
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(start = 16.dp, bottom = 100.dp)
                ) {
                    LaunchSitesMenu(
                        launchSites = state.launchSites.filter { it.name != "Last Visited" },
                        onSiteSelected = { site ->
                            // Launch a coroutine to delay the update so an animation can play.
                            coroutineScope.launch {
                                // For example, delay 300 ms.
                                delay(300)
                                viewModel.updateCoordinates(site.latitude, site.longitude)
                                viewModel.updateLastVisited(site.latitude, site.longitude)
                            }
                            isLaunchSiteMenuExpanded = false
                        }
                    )
                }

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
