package no.uio.ifi.in2000.met2025.ui.screens.home

import android.Manifest
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.mapbox.geojson.Point
import no.uio.ifi.in2000.met2025.R
import no.uio.ifi.in2000.met2025.ui.maps.LocationViewModel
import no.uio.ifi.in2000.met2025.ui.maps.MapView
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

    // Retrieve the temporary "Last Visited" launch site.
    val tempLastVisited by launchSiteViewModel.tempLaunchSite.collectAsState(initial = null)
    // Use the "Last Visited" site's coordinates for initial marker if available.
    val initialMarkerCoordinate: Point? = tempLastVisited?.let {
        Point.fromLngLat(it.longitude, it.latitude)
    }

    // Ensure shared location state is in sync with "Last Visited".
    LaunchedEffect(tempLastVisited) {
        tempLastVisited?.let {
            if (coordinates.first != it.latitude || coordinates.second != it.longitude) {
                locationViewModel.updateCoordinates(it.latitude, it.longitude)
            }
        }
    }

    // Local state for coordinate input fields (if needed).
    var latInput by rememberSaveable { mutableStateOf(coordinates.first.toString()) }
    var lonInput by rememberSaveable { mutableStateOf(coordinates.second.toString()) }
    var markerPosition by rememberSaveable { mutableStateOf<Pair<Double, Double>?>(null) }
    var showSaveDialog by rememberSaveable { mutableStateOf(false) }
    var launchSiteName by rememberSaveable { mutableStateOf("") }
    var savedMarkerCoordinates by rememberSaveable { mutableStateOf<Pair<Double, Double>?>(null) }

    // New state: toggle for showing the launch sites menu.
    var isLaunchSiteMenuExpanded by remember { mutableStateOf(false) }
    // Retrieve all saved launch sites.
    val launchSites by launchSiteViewModel.launchSites.collectAsState(initial = emptyList())
    // In the menu, filter out "Last Visited" so that the "New Marker" is shown (along with any other saved sites).
    val menuLaunchSites = launchSites.filter { it.name != "Last Visited" }

    if (locationPermissionState.status.isGranted) {
        Box(modifier = Modifier.fillMaxSize()) {
            // MapView receives the "Last Visited" coordinates as initialMarkerCoordinate.
            // When a marker is placed, we update the "New Marker" temporary instead.
            MapView(
                latitude = coordinates.first,
                longitude = coordinates.second,
                initialMarkerCoordinate = initialMarkerCoordinate,
                modifier = Modifier.fillMaxSize(),
                onMarkerPlaced = { lat, lon ->
                    latInput = lat.toString()
                    lonInput = lon.toString()
                    markerPosition = Pair(lat, lon)
                    locationViewModel.updateCoordinates(lat, lon)
                    // Update the "New Marker" temporary launch site.
                    launchSiteViewModel.updateNewMarkerSite(lat, lon)
                },
                onMarkerAnnotationClick = { lat, lon ->
                    savedMarkerCoordinates = Pair(lat, lon)
                    showSaveDialog = true
                }
            )

            // Top coordinate display.
            Row(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 40.dp)
                    .zIndex(1f)
            ) {
                Box(
                    modifier = Modifier
                        .background(Color.Black, shape = RoundedCornerShape(8.dp))
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = "Lat: ${"%.4f".format(coordinates.first)}",
                        color = Color.White,
                        fontSize = 18.sp
                    )
                }
                Spacer(modifier = Modifier.width(16.dp))
                Box(
                    modifier = Modifier
                        .background(Color.Black, shape = RoundedCornerShape(8.dp))
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = "Lon: ${"%.4f".format(coordinates.second)}",
                        color = Color.White,
                        fontSize = 18.sp
                    )
                }
            }

            // Button to toggle the launch sites menu.
            Button(
                onClick = { isLaunchSiteMenuExpanded = !isLaunchSiteMenuExpanded },
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(16.dp),
                colors = androidx.compose.material3.ButtonDefaults.buttonColors(containerColor = Color.Black)
            ) {
                Text(text = "Launch Sites", color = Color.White)
            }

            // Expanded menu of launch sites (excluding "Last Visited").
            if (isLaunchSiteMenuExpanded) {
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(start = 16.dp, bottom = 80.dp)
                        .background(Color.White.copy(alpha = 0.9f), shape = RoundedCornerShape(8.dp))
                        .padding(8.dp)
                ) {
                    menuLaunchSites.forEach { site ->
                        Text(
                            text = site.name,
                            modifier = Modifier
                                .clickable {
                                    locationViewModel.updateCoordinates(site.latitude, site.longitude)
                                    // Update "Last Visited" temporary so the marker moves accordingly.
                                    launchSiteViewModel.updateTemporaryLaunchSite(site.latitude, site.longitude)
                                    latInput = site.latitude.toString()
                                    lonInput = site.longitude.toString()
                                    isLaunchSiteMenuExpanded = false
                                }
                                .padding(8.dp)
                        )
                    }
                }
            }

            // Floating action button to navigate to WeatherCardScreen.
            FloatingActionButton(
                onClick = {
                    val lat = latInput.toDoubleOrNull()
                    val lon = lonInput.toDoubleOrNull()
                    if (lat != null && lon != null) {
                        locationViewModel.updateCoordinates(lat, lon)
                        onNavigateToWeather(lat, lon)
                    } else {
                        Toast.makeText(context, "Please enter valid coordinates", Toast.LENGTH_SHORT).show()
                    }
                },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(16.dp)
                    .size(90.dp),
                containerColor = Color.Black,
                shape = CircleShape,
                content = {
                    Icon(
                        painter = painterResource(id = R.drawable.weather_icon),
                        contentDescription = "Navigate to Weather",
                        modifier = Modifier.size(50.dp),
                        tint = Color.White
                    )
                }
            )

            // Save Launch Site Dialog.
            if (showSaveDialog && savedMarkerCoordinates != null) {
                androidx.compose.material3.AlertDialog(
                    onDismissRequest = { showSaveDialog = false },
                    title = { Text("Save Launch Site") },
                    text = {
                        Column {
                            Text("Enter a name for this launch site:")
                            OutlinedTextField(
                                value = launchSiteName,
                                onValueChange = { launchSiteName = it },
                                label = { Text("Site Name") }
                            )
                        }
                    },
                    confirmButton = {
                        Button(
                            onClick = {
                                val (lat, lon) = savedMarkerCoordinates!!
                                // Save the launch site permanently via the LaunchSiteViewModel.
                                launchSiteViewModel.addLaunchSite(lat, lon, launchSiteName)
                                showSaveDialog = false
                                launchSiteName = ""
                            }
                        ) {
                            Text("Save")
                        }
                    },
                    dismissButton = {
                        Button(onClick = { showSaveDialog = false }) {
                            Text("Cancel")
                        }
                    }
                )
            }
        }
    } else {
        // UI when location permission is not granted.
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Location permission is required to display your current location on the map.")
                Spacer(modifier = Modifier.height(8.dp))
                Button(onClick = { locationPermissionState.launchPermissionRequest() }) {
                    Text("Grant Permission")
                }
            }
        }
    }
}
