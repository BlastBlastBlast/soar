// HomeScreen.kt
package no.uio.ifi.in2000.met2025.ui.screens.home

import android.Manifest
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.mapbox.geojson.Point
import no.uio.ifi.in2000.met2025.ui.screens.home.maps.LocationViewModel
import no.uio.ifi.in2000.met2025.ui.screens.home.components.CoordinateDisplay
import no.uio.ifi.in2000.met2025.ui.screens.home.components.LaunchSitesMenu
import no.uio.ifi.in2000.met2025.ui.screens.home.components.MapContainer
import no.uio.ifi.in2000.met2025.ui.screens.home.components.PermissionRequestScreen
import no.uio.ifi.in2000.met2025.ui.screens.home.components.SaveLaunchSiteDialog
import no.uio.ifi.in2000.met2025.ui.screens.home.components.WeatherNavigationButton
import no.uio.ifi.in2000.met2025.ui.screens.launchsite.LaunchSiteViewModel

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun HomeScreen(
    viewModel: HomeScreenViewModel = hiltViewModel(),
    locationViewModel: LocationViewModel = hiltViewModel(),
    onNavigateToWeather: (Double, Double) -> Unit
) {
    val launchSiteViewModel: LaunchSiteViewModel = hiltViewModel()
    val locationPermissionState = rememberPermissionState(Manifest.permission.ACCESS_FINE_LOCATION)
    val context = LocalContext.current
    val coordinates by locationViewModel.coordinates.collectAsState()

    // Get "Last Visited" launch site and initialize marker if available.
    val tempLastVisited by launchSiteViewModel.tempLaunchSite.collectAsState(initial = null)
    val initialMarkerCoordinate: Point? = tempLastVisited?.let {
        Point.fromLngLat(it.longitude, it.latitude)
    }
    LaunchedEffect(tempLastVisited) {
        tempLastVisited?.let {
            if (coordinates.first != it.latitude || coordinates.second != it.longitude) {
                locationViewModel.updateCoordinates(it.latitude, it.longitude)
            }
        }
    }

    // Local states for inputs and dialog.
    var latInput by rememberSaveable { mutableStateOf(coordinates.first.toString()) }
    var lonInput by rememberSaveable { mutableStateOf(coordinates.second.toString()) }
    var markerPosition by rememberSaveable { mutableStateOf<Pair<Double, Double>?>(null) }
    var showSaveDialog by rememberSaveable { mutableStateOf(false) }
    var launchSiteName by rememberSaveable { mutableStateOf("") }
    var savedMarkerCoordinates by rememberSaveable { mutableStateOf<Pair<Double, Double>?>(null) }
    var isLaunchSiteMenuExpanded by remember { mutableStateOf(false) }
    val launchSites by launchSiteViewModel.launchSites.collectAsState(initial = emptyList())
    val menuLaunchSites = launchSites.filter { it.name != "Last Visited" }

    // Only display the main content if the location permission is granted.
    if (locationPermissionState.status.isGranted) {
        Box(modifier = Modifier.fillMaxSize()) {
            // Map Container encapsulates the MapView logic.
            MapContainer(
                coordinates = coordinates,
                initialMarkerCoordinate = initialMarkerCoordinate,
                onMarkerPlaced = { lat, lon ->
                    latInput = lat.toString()
                    lonInput = lon.toString()
                    markerPosition = Pair(lat, lon)
                    locationViewModel.updateCoordinates(lat, lon)
                    launchSiteViewModel.updateNewMarkerSite(lat, lon)
                },
                onMarkerAnnotationClick = { lat, lon ->
                    savedMarkerCoordinates = Pair(lat, lon)
                    showSaveDialog = true
                }
            )

            // Top coordinate display.
            CoordinateDisplay(coordinates = coordinates)

            // Toggle button for the launch sites menu.
            Button(
                onClick = { isLaunchSiteMenuExpanded = !isLaunchSiteMenuExpanded },
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(16.dp),
                colors = androidx.compose.material3.ButtonDefaults.buttonColors(containerColor = Color.Black)
            ) {
                Text(text = "Launch Sites", color = Color.White)
            }

            // Display the launch sites menu if toggled.
            if (isLaunchSiteMenuExpanded) {
                LaunchSitesMenu(
                    launchSites = menuLaunchSites,
                    onSiteSelected = { site ->
                        locationViewModel.updateCoordinates(site.latitude, site.longitude)
                        launchSiteViewModel.updateTemporaryLaunchSite(site.latitude, site.longitude)
                        latInput = site.latitude.toString()
                        lonInput = site.longitude.toString()
                        isLaunchSiteMenuExpanded = false
                    },
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(start = 16.dp, bottom = 80.dp)
                )
            }

            // Floating action button to navigate to Weather Screen.
            WeatherNavigationButton(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(16.dp)
                    .size(90.dp),
                latInput = latInput,
                lonInput = lonInput,
                onNavigate = { lat, lon ->
                    locationViewModel.updateCoordinates(lat, lon)
                    onNavigateToWeather(lat, lon)
                },
                context = context
            )

            // Dialog for saving a launch site.
            if (showSaveDialog && savedMarkerCoordinates != null) {
                SaveLaunchSiteDialog(
                    launchSiteName = launchSiteName,
                    onNameChange = { launchSiteName = it },
                    onDismiss = { showSaveDialog = false },
                    onConfirm = {
                        val (lat, lon) = savedMarkerCoordinates!!
                        launchSiteViewModel.addLaunchSite(lat, lon, launchSiteName)
                        showSaveDialog = false
                        launchSiteName = ""
                    }
                )
            }
        }
    } else {
        // Fallback UI when permission is not granted.
        PermissionRequestScreen(
            onRequestPermission = { locationPermissionState.launchPermissionRequest() }
        )
    }
}
