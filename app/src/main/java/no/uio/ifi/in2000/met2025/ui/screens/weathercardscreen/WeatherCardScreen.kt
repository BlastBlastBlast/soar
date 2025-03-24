package no.uio.ifi.in2000.met2025.ui.screens.weathercardscreen

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import no.uio.ifi.in2000.met2025.ui.components.HourlyExpandableCard

@Composable
fun WeatherCardScreen(viewModel: WeatherCardViewmodel = hiltViewModel()) {
    val uiState by viewModel.uiState.collectAsState()
    ScreenContent(uiState = uiState, onLoadForecast = { lat, lon ->
        viewModel.loadForecast(lat, lon)
    })
}

@Composable
fun ScreenContent(
    uiState: WeatherCardViewmodel.WeatherCardUiState,
    onLoadForecast: (Double, Double) -> Unit = { _, _ -> }
) {
    Column(modifier = Modifier.padding(16.dp)) {
        when (uiState) {
            is WeatherCardViewmodel.WeatherCardUiState.Idle -> {
                Text(
                    text = "Idle",
                    style = MaterialTheme.typography.headlineSmall
                )
                Button(
                    onClick = { onLoadForecast(59.91, 10.75) },
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
                val sortedHours = uiState.displayData.temperatures.keys.sorted()
                sortedHours.forEach { hour ->
                    HourlyExpandableCard(
                        displayData = uiState.displayData,
                        hour = hour,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }
            }
        }
    }
}