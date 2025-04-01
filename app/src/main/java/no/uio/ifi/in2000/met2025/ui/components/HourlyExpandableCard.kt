package no.uio.ifi.in2000.met2025.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowUp
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import no.uio.ifi.in2000.met2025.data.models.ForecastDataItem
import no.uio.ifi.in2000.met2025.data.models.ForecastDataValues
import no.uio.ifi.in2000.met2025.data.models.LaunchStatusIcon
import no.uio.ifi.in2000.met2025.data.models.LaunchStatusIndicator
import no.uio.ifi.in2000.met2025.data.models.evaluateParameterConditions
import androidx.compose.ui.unit.dp
import androidx.compose.material3.Icon
import androidx.compose.ui.res.painterResource
import com.mapbox.maps.extension.compose.annotation.rememberIconImage
import no.uio.ifi.in2000.met2025.R
import no.uio.ifi.in2000.met2025.domain.helpers.formatZuluTimeToLocal

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
            // Header: show time and overall launch status icon
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Time: ${formatZuluTimeToLocal(forecastItem.time)}",
                    style = MaterialTheme.typography.headlineSmall
                )
                LaunchStatusIndicator(forecast = forecastItem)
            }
            Text(
                text = "Temperature: ${forecastItem.values.airTemperature}°C",
                style = MaterialTheme.typography.bodyMedium
            )
            // Expanded details: show detailed per-parameter evaluation
            AnimatedVisibility(visible = expanded) {
                Column(modifier = Modifier.padding(top = 8.dp)) {
                    // Get individual evaluations
                    val evaluations = evaluateParameterConditions(forecastItem)
                    evaluations.forEach { eval ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                        ) {
                            Text(
                                text = "${eval.label}: ${eval.value}",
                                style = MaterialTheme.typography.bodyMedium
                            )
                            // If this is wind direction, show the custom icon
                            if (eval.label == "Wind Direction") {
                                WindDirectionIcon(forecastItem.values.windFromDirection)
                            } else {
                                LaunchStatusIcon(status = eval.status)
                            }
                        }
                    }
                }
            }
        }
    }
}
