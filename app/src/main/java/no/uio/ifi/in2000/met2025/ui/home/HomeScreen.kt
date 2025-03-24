// Kotlin
package no.uio.ifi.in2000.met2025.ui.home

import android.Manifest
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.mapbox.geojson.Point
import com.mapbox.maps.extension.compose.MapboxMap
import com.mapbox.maps.extension.compose.animation.viewport.rememberMapViewportState

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun HomeScreen(viewModel: HomeScreenViewModel = hiltViewModel()) {
    // Use the new API: check status.isGranted instead of hasPermission.
    val locationPermissionState = rememberPermissionState(Manifest.permission.ACCESS_FINE_LOCATION)
    val context = LocalContext.current

    if (locationPermissionState.status.isGranted) {
        // Permission granted; show the main UI.
        val coordinates by viewModel.coordinates.collectAsState()
        var latInput by remember { mutableStateOf("") }
        var lonInput by remember { mutableStateOf("") }
        var addressInput by remember { mutableStateOf("") }

        Column(modifier = Modifier.fillMaxSize()) {
            // Map takes 70% of the screen.
            MapboxMap(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(0.7f),
                mapViewportState = rememberMapViewportState {
                    setCameraOptions {
                        center(Point.fromLngLat(coordinates.second, coordinates.first))
                        zoom(12.0)
                        pitch(0.0)
                        bearing(0.0)
                    }
                }
            )
            // Input area takes 30% of the screen.
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(0.3f)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(text = "Coordinates: Lat = ${coordinates.first}, Lon = ${coordinates.second}")
                OutlinedTextField(
                    value = latInput,
                    onValueChange = { latInput = it },
                    label = { Text("Latitude") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = lonInput,
                    onValueChange = { lonInput = it },
                    label = { Text("Longitude") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = addressInput,
                    onValueChange = { addressInput = it },
                    label = { Text("Address (optional)") },
                    modifier = Modifier.fillMaxWidth()
                )
                Button(
                    onClick = {
                        if (latInput.isNotBlank() && lonInput.isNotBlank()) {
                            val lat = latInput.toDoubleOrNull()
                            val lon = lonInput.toDoubleOrNull()
                            if (lat != null && lon != null) {
                                viewModel.updateCoordinates(lat, lon)
                            } else {
                                Toast.makeText(
                                    context, "Invalid latitude or longitude", Toast.LENGTH_SHORT).show()
                            }
                        } else if (addressInput.isNotBlank()) {
                            val coords = viewModel.geocodeAddress(addressInput)
                            if (coords != null) {
                                viewModel.updateCoordinates(coords.first, coords.second)
                            } else {
                                Toast.makeText(context, "Unable to convert address to coordinates", Toast.LENGTH_SHORT).show()
                            }
                        } else {
                            Toast.makeText(context, "Please enter coordinates or an address", Toast.LENGTH_SHORT).show()
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Update Map")
                }
            }
        }
    } else {
        // If permission is not granted, show a message and button to request it.
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