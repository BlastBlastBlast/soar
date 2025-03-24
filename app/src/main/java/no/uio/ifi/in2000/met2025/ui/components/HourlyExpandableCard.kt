package no.uio.ifi.in2000.met2025.ui.components

import no.uio.ifi.in2000.met2025.ui.screens.weathercardscreen.WeatherCardViewmodel
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

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