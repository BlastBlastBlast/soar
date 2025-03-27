package no.uio.ifi.in2000.met2025.ui.screens.weathercardscreen

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import no.uio.ifi.in2000.met2025.ui.components.HourlyExpandableCard
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll

@Composable
fun WeatherCardScreen(viewModel: WeatherCardViewmodel = hiltViewModel()) {
    val uiState by viewModel.uiState.collectAsState()
    // Pass the lambda to load forecast using the entered coordinates
    ScreenContent(uiState = uiState, onLoadForecast = { lat, lon ->
        viewModel.loadForecast(lat, lon)
    })
}

@Composable
fun ScreenContent(
    uiState: WeatherCardViewmodel.WeatherCardUiState,
    onLoadForecast: (Double, Double) -> Unit = { _, _ -> }
) {
    // Pre-fill with coordinates for Ole Johan Dahl's hus
    var latInput by remember { mutableStateOf("59.942") }
    var lonInput by remember { mutableStateOf("10.726") }

    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier.padding(16.dp)
        .verticalScroll(scrollState)
    ) {
        when (uiState) {
            is WeatherCardViewmodel.WeatherCardUiState.Idle -> {
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
                            onLoadForecast(lat, lon)
                        }
                    },
                    modifier = Modifier.padding(top = 8.dp)
                ) {
                    Text(text = "Load Forecast")
                }
            }
            is WeatherCardViewmodel.WeatherCardUiState.Loading -> {
                Text(
                    text = "Loading...",
                    style = MaterialTheme.typography.headlineSmall
                )
            }
            is WeatherCardViewmodel.WeatherCardUiState.Error -> {
                Text(
                    text = "Error: ${uiState.message}",
                    style = MaterialTheme.typography.headlineSmall
                )
            }
            is WeatherCardViewmodel.WeatherCardUiState.Success -> {
                // Here we iterate through the list of ForecastDataItem directly
                uiState.forecastItems.forEach { forecastItem ->
                    HourlyExpandableCard(
                        forecastItem = forecastItem,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }
            }
        }
    }
}
