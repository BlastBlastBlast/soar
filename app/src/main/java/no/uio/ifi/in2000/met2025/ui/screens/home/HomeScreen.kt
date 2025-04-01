package no.uio.ifi.in2000.met2025.ui.screens.home

import android.Manifest
import android.widget.Toast
import androidx.compose.foundation.background
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

    // Collect the temporary launch site from the DAO.
    val tempLaunchSite by launchSiteViewModel.tempLaunchSite.collectAsState(initial = null)
    // If a temp site exists, use its coordinates as the default.
    val initialMarkerCoordinate: Point? = tempLaunchSite?.let {
        Point.fromLngLat(it.longitude, it.latitude)
    }

    // If the temporary site exists and differs from current coordinates, update shared state.
    LaunchedEffect(tempLaunchSite) {
        tempLaunchSite?.let {
            if (coordinates.first != it.latitude || coordinates.second != it.longitude) {
                locationViewModel.updateCoordinates(it.latitude, it.longitude)
            }
        }
    }

    var latInput by rememberSaveable { mutableStateOf(coordinates.first.toString()) }
    var lonInput by rememberSaveable { mutableStateOf(coordinates.second.toString()) }
    var addressInput by rememberSaveable { mutableStateOf("") }
    var markerPosition by rememberSaveable { mutableStateOf<Pair<Double, Double>?>(null) }
    var showAddressField by rememberSaveable { mutableStateOf(false) }
    var showSaveDialog by rememberSaveable { mutableStateOf(false) }
    var launchSiteName by rememberSaveable { mutableStateOf("") }
    var savedMarkerCoordinates by rememberSaveable { mutableStateOf<Pair<Double, Double>?>(null) }

    if (locationPermissionState.status.isGranted) {
        Box(modifier = Modifier.fillMaxSize()) {
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
                    // Update the temporary launch site in the database.
                    launchSiteViewModel.updateTemporaryLaunchSite(lat, lon)
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

            // Optional address input field.
            if (showAddressField) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(top = 60.dp)
                        .background(Color.White.copy(alpha = 0.8f), shape = RoundedCornerShape(8.dp))
                        .padding(8.dp)
                ) {
                    OutlinedTextField(
                        value = addressInput,
                        onValueChange = { addressInput = it },
                        label = { Text("Address (optional)") },
                        modifier = Modifier.fillMaxWidth()
                    )
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
                    } else if (addressInput.isNotBlank()) {
                        val coords = viewModel.geocodeAddress(addressInput)
                        if (coords != null) {
                            locationViewModel.updateCoordinates(coords.first, coords.second)
                            onNavigateToWeather(coords.first, coords.second)
                        } else {
                            Toast.makeText(context, "Unable to geocode address", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        Toast.makeText(context, "Please enter coordinates or an address", Toast.LENGTH_SHORT).show()
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

            // Button to toggle the optional address input field.
            Button(
                onClick = { showAddressField = !showAddressField },
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(16.dp),
                colors = androidx.compose.material3.ButtonDefaults.buttonColors(containerColor = Color.Black)
            ) {
                Text(text = "Address Field", color = Color.White)
            }

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
                                // Save the launch site temporarily via the LaunchSiteViewModel.
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
        // UI when permission is not granted.
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
