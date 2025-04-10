package no.uio.ifi.in2000.met2025.ui.screens.weathercardscreen.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import no.uio.ifi.in2000.met2025.data.models.ForecastDataItem
import no.uio.ifi.in2000.met2025.data.models.LaunchStatusIcon
import no.uio.ifi.in2000.met2025.data.models.LaunchStatusIndicator
import no.uio.ifi.in2000.met2025.data.models.evaluateParameterConditions
import androidx.compose.ui.res.painterResource
import no.uio.ifi.in2000.met2025.R
import no.uio.ifi.in2000.met2025.data.local.database.ConfigProfile
import no.uio.ifi.in2000.met2025.domain.helpers.formatZuluTimeToLocalTime
import no.uio.ifi.in2000.met2025.domain.helpers.formatZuluTimeToLocalDate
import no.uio.ifi.in2000.met2025.domain.helpers.closestIsobaricDataWindowBefore
import java.time.Instant

@Composable
fun WindDirectionIcon(windDirection: Double) {
    // Load the custom arrow drawable from resources (ensure the name matches your resource)
    val arrowPainter = painterResource(id = R.drawable.up_arrow)
    // Calculate rotation: add 180° so the arrow points in the wind's source direction
    val rotation = (windDirection + 180) % 360

    Image(
        painter = arrowPainter,
        contentDescription = "Wind Direction",
        modifier = Modifier
            .size(24.dp)
            .graphicsLayer(rotationZ = rotation.toFloat())
    )
}

@Composable
fun HourlyExpandableCard(
    forecastItem: ForecastDataItem,
    coordinates: Pair<Double, Double>,
    config: ConfigProfile,
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
            // Header: Display date, time and overall launch status
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
            ) {
                Text(
                    text = "${formatZuluTimeToLocalDate(forecastItem.time)}: ${formatZuluTimeToLocalTime(forecastItem.time)}",
                    style = MaterialTheme.typography.headlineSmall
                )
                LaunchStatusIndicator(forecast = forecastItem, config = config)
            }
            // Display a quick temperature readout
            Text(
                text = "Temperature: ${forecastItem.values.airTemperature}°C",
                style = MaterialTheme.typography.bodyMedium
            )
            // Expanded details: show all parameter evaluations in a neat three-column layout.
            AnimatedVisibility(visible = expanded) {
                Column(modifier = Modifier.padding(top = 8.dp)) {
                    // Get evaluations based on forecast and configuration.
                    val evaluations = evaluateParameterConditions(forecastItem, config)
                    evaluations.forEach { evaluation ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                        ) {
                            // Label column with weight 1 (can be given a fixed width if preferred)
                            Text(
                                text = evaluation.label,
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.weight(0.6f)
                            )
                            // Value column with weight 1, right aligned
                            Text(
                                text = evaluation.value,
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier
                                    .weight(0.4f)
                            )
                            // Icon column with a small weight
                            if (evaluation.label == "Wind Direction") {
                                WindDirectionIcon(forecastItem.values.windFromDirection)
                            } else {
                                LaunchStatusIcon(status = evaluation.status)
                            }
                        }
                    }

                    // Additional content
                    AtmosphericWindTable(
                        coordinates = coordinates,
                        time = Instant.parse(forecastItem.time).closestIsobaricDataWindowBefore()
                    )
                }
            }
        }
    }
}
