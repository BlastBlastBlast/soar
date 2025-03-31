package no.uio.ifi.in2000.met2025.ui.screens.home

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
import no.uio.ifi.in2000.met2025.ui.maps.LocationViewModel
import no.uio.ifi.in2000.met2025.ui.maps.MapView

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun HomeScreen(
    viewModel: HomeScreenViewModel = hiltViewModel(),
    locationViewModel: LocationViewModel = hiltViewModel(),  // Inject here
    onNavigateToWeather: (Double, Double) -> Unit
) {
    val locationPermissionState = rememberPermissionState(Manifest.permission.ACCESS_FINE_LOCATION)
    val context = LocalContext.current
    val coordinates by locationViewModel.coordinates.collectAsState()

    if (locationPermissionState.status.isGranted) {
        var latInput by remember { mutableStateOf(coordinates.first.toString()) }
        var lonInput by remember { mutableStateOf(coordinates.second.toString()) }
        var addressInput by remember { mutableStateOf("") }

        Column(modifier = Modifier.fillMaxSize()) {
            MapView(
                latitude = coordinates.first,
                longitude = coordinates.second,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(0.65f),
                onMarkerPlaced = { lat, lon ->
                    latInput = lat.toString()
                    lonInput = lon.toString()
                    locationViewModel.updateCoordinates(lat, lon)  // Update centralized ViewModel
                }
            )
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(0.35f)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(text = "Coordinates: Lat = ${coordinates.first}, Lon = ${coordinates.second}")

                Row(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = latInput,
                        onValueChange = { latInput = it },
                        label = { Text("Latitude") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(0.5f)
                    )
                    OutlinedTextField(
                        value = lonInput,
                        onValueChange = { lonInput = it },
                        label = { Text("Longitude") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(0.5f)
                    )
                }

                OutlinedTextField(
                    value = addressInput,
                    onValueChange = { addressInput = it },
                    label = { Text("Address (optional)") },
                    modifier = Modifier.fillMaxWidth()
                )
                Button(
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
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Update Map")
                }
            }
        }
    } else {
        // Request permission if not granted.
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
