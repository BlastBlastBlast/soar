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
import no.uio.ifi.in2000.met2025.data.models.ForecastDataItem

@Composable
fun HourlyExpandableCard(
    forecastItem: ForecastDataItem,
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
                text = "Time: ${forecastItem.time}",
                style = MaterialTheme.typography.headlineSmall
            )
            Text(
                text = "Temperature: ${forecastItem.values.airTemperature}°C",
                style = MaterialTheme.typography.bodyMedium
            )
            // Extra details shown when card is tapped
            AnimatedVisibility(visible = expanded) {
                Column(modifier = Modifier.padding(top = 8.dp)) {
                    Text(
                        text = "Humidity: ${forecastItem.values.relativeHumidity}%",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = "Wind Speed: ${forecastItem.values.windSpeed} m/s",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = "Wind Gust: ${forecastItem.values.windSpeedOfGust} m/s",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = "Wind Direction: ${forecastItem.values.windFromDirection}°",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = "Precipitation: ${forecastItem.values.precipitationAmount} mm",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    // Here, we're reusing fogAreaFraction as a stand-in for "visibility"
                    Text(
                        text = "Visibility: ${forecastItem.values.fogAreaFraction}%",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = "Dew Point: ${forecastItem.values.dewPointTemperature}°C",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = "Cloud Cover: ${forecastItem.values.cloudAreaFraction}%",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = "Thunder Prob: ${forecastItem.values.probabilityOfThunder}%",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
    }
}
