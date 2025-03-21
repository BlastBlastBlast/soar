package no.uio.ifi.in2000.met2025.ui.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

@Composable
fun WeatherCardScreen(viewModel: WeatherCardViewmodel) {
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

@Composable
fun HourlyExpandableCard(
    displayData: WeatherCardViewmodel.ForecastDisplayData,
    hour: String,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { expanded = !expanded },
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Summary section for the hour
            Text(
                text = "Time: $hour",
                style = MaterialTheme.typography.headlineSmall
            )
            Text(
                text = "Temperature: ${displayData.temperatures[hour]}°C",
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                text = "Humidity: ${displayData.humidities[hour]}%",
                style = MaterialTheme.typography.bodyMedium
            )
            // Extra details shown when card is tapped
            AnimatedVisibility(visible = expanded) {
                Column(modifier = Modifier.padding(top = 8.dp)) {
                    Text(
                        text = "Wind Speed: ${displayData.windSpeeds[hour]} m/s",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = "Wind Gust: ${displayData.windGusts[hour]} m/s",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = "Wind Direction: ${displayData.windDirections[hour]}°",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = "Precipitation: ${displayData.precipitations[hour]} mm",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = "Visibility: ${displayData.visibilities[hour]}%",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = "Dew Point: ${displayData.dewPoints[hour]}°C",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = "Cloud Cover: ${displayData.cloudCovers[hour]}%",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = "Thunder Prob: ${displayData.thunderProbabilities[hour]}%",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }
}

@Preview(showSystemUi = true, showBackground = true)
@Composable
fun WeatherCardPreview() {
    val sampleForecastDisplayData = WeatherCardViewmodel.ForecastDisplayData(
        temperatures = mapOf(
            "08:00" to 10.0,
            "09:00" to 11.0,
            "10:00" to 12.0,
            "11:00" to 13.0
        ),
        humidities = mapOf(
            "08:00" to 80.0,
            "09:00" to 78.0,
            "10:00" to 75.0,
            "11:00" to 70.0
        ),
        windSpeeds = mapOf(
            "08:00" to 3.0,
            "09:00" to 3.5,
            "10:00" to 4.0,
            "11:00" to 4.5
        ),
        windGusts = mapOf(
            "08:00" to 5.0,
            "09:00" to 5.5,
            "10:00" to 6.0,
            "11:00" to 6.5
        ),
        windDirections = mapOf(
            "08:00" to 180.0,
            "09:00" to 190.0,
            "10:00" to 200.0,
            "11:00" to 210.0
        ),
        precipitations = mapOf(
            "08:00" to 0.0,
            "09:00" to 0.1,
            "10:00" to 0.0,
            "11:00" to 0.2
        ),
        visibilities = mapOf(
            "08:00" to 100.0,
            "09:00" to 98.0,
            "10:00" to 95.0,
            "11:00" to 92.0
        ),
        dewPoints = mapOf(
            "08:00" to 5.0,
            "09:00" to 6.0,
            "10:00" to 7.0,
            "11:00" to 8.0
        ),
        cloudCovers = mapOf(
            "08:00" to 20.0,
            "09:00" to 30.0,
            "10:00" to 50.0,
            "11:00" to 60.0
        ),
        thunderProbabilities = mapOf(
            "08:00" to 0.0,
            "09:00" to 10.0,
            "10:00" to 20.0,
            "11:00" to 30.0
        )
    )

    Column(modifier = Modifier.padding(16.dp)) {
        sampleForecastDisplayData.temperatures.keys.sorted().forEach { hour ->
            HourlyExpandableCard(
                displayData = sampleForecastDisplayData,
                hour = hour,
                modifier = Modifier.padding(vertical = 8.dp)
            )
        }
    }
}