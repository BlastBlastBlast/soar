package no.uio.ifi.in2000.met2025.ui.screens.amtosphericwind

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import no.uio.ifi.in2000.met2025.data.models.IsobaricDataItem
import androidx.compose.runtime.mutableStateOf
import no.uio.ifi.in2000.met2025.data.models.Constants

//TODO: hook up screen to navigation graph

//TODO: make preview with mock data

@Composable
fun AtmosphericWindScreen(viewModel: AtmosphericWindViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState.collectAsState()
    ScreenContent(uiState = uiState, onLoadData = { lat, lon ->
        viewModel.loadIsobaricData(lat, lon)
    })
}

@Composable
fun ScreenContent(
    uiState: AtmosphericWindViewModel.AtmosphericWindUiState,
    onLoadData: (Double, Double) -> Unit = { _, _ -> }
) {
    // Pre-fill with coordinates for Ole Johan Dahl's hus
    //TODO: Change to use coordinates from HomeScreen
    var latInput by remember { mutableStateOf("59.942") }
    var lonInput by remember { mutableStateOf("10.726") }

    Column(modifier = Modifier.padding(16.dp)) {
        when (uiState) {
            is AtmosphericWindViewModel.AtmosphericWindUiState.Idle -> {
                Text(
                    text = "Enter coordinates for forecast:",
                    style = MaterialTheme.typography.headlineSmall
                )
                OutlinedTextField(
                    value = latInput,
                    onValueChange = { latInput = it },
                    label = { Text("Latitude") },
                    modifier = Modifier.padding(top = 8.dp)
                )
                OutlinedTextField(
                    value = lonInput,
                    onValueChange = { lonInput = it },
                    label = { Text("Longitude") },
                    modifier = Modifier.padding(top = 8.dp)
                )
                Button(
                    onClick = {
                        val lat = latInput.toDoubleOrNull()
                        val lon = lonInput.toDoubleOrNull()
                        if (lat != null && lon != null) {
                            onLoadData(lat, lon)
                        }
                    },
                    modifier = Modifier.padding(top = 8.dp)
                ) {
                    Text(text = "Load Isobaric Data")
                }
            }
            is AtmosphericWindViewModel.AtmosphericWindUiState.Loading -> {
                Text(text = "Loading...", style = MaterialTheme.typography.headlineSmall)
            }
            is AtmosphericWindViewModel.AtmosphericWindUiState.Error -> {
                Text(text = "Error: ${uiState.message}", style = MaterialTheme.typography.headlineSmall)
            }
            is AtmosphericWindViewModel.AtmosphericWindUiState.Success -> {
                uiState.isobaricData.timeSeries.forEach { item ->
                    IsobaricDataItemCard(item = item)
                }
            }
        }
    }
}

@Composable
fun IsobaricDataItemCard(
    item: IsobaricDataItem
) {
    Column(modifier = Modifier.padding(vertical = 8.dp)) {
        Text(text = "Time: ${item.time}", style = MaterialTheme.typography.headlineSmall)
            Constants.layerPressureValues.forEach { layer ->
            Text(
                text = "Layer: $layer hPa",
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                text = "Wind Speed: ${item.valuesAtLayer[layer]?.windSpeed} m/s",
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                text = "Wind Direction: ${item.valuesAtLayer[layer]?.windFromDirection}°",
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}