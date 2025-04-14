package no.uio.ifi.in2000.met2025.ui.screens.weathercardscreen.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
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
import no.uio.ifi.in2000.met2025.data.models.EvaluationIcon
import no.uio.ifi.in2000.met2025.domain.helpers.formatZuluTimeToLocalTime
import no.uio.ifi.in2000.met2025.domain.helpers.formatZuluTimeToLocalDate
import no.uio.ifi.in2000.met2025.domain.helpers.closestIsobaricDataWindowBefore
import no.uio.ifi.in2000.met2025.ui.screens.weathercardscreen.WeatherCardViewmodel
import java.time.Instant

//HourlyExpandableCard.kt
@Composable
fun WindDirectionIcon(windDirection: Double) {
    val arrowPainter = painterResource(id = R.drawable.up_arrow)
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
    modifier: Modifier = Modifier,
    viewModel : WeatherCardViewmodel
) {
    var expanded by remember { mutableStateOf(false) }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { expanded = !expanded },
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${formatZuluTimeToLocalDate(forecastItem.time)}: ${formatZuluTimeToLocalTime(forecastItem.time)}",
                    style = MaterialTheme.typography.headlineSmall
                )
                LaunchStatusIndicator(forecast = forecastItem, config = config)
            }
            Text(
                text = "Temperature: ${forecastItem.values.airTemperature}°C",
                style = MaterialTheme.typography.bodyMedium
            )
            AnimatedVisibility(visible = expanded) {
                Column(modifier = Modifier.padding(top = 8.dp)) {
                    val evaluations = evaluateParameterConditions(forecastItem, config)
                    evaluations.forEach { evaluation ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // 1. Parameter Name.
                            Text(
                                text = evaluation.label,
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.weight(0.5f)
                            )
                            // 2. Parameter Icon.
                            if (evaluation.label == "Wind Direction") {
                                WindDirectionIcon(forecastItem.values.windFromDirection)
                            } else {
                                evaluation.icon?.let { iconData ->
                                    when (iconData) {
                                        is EvaluationIcon.DrawableIcon -> {
                                            Icon(
                                                painter = painterResource(id = iconData.resId),
                                                contentDescription = evaluation.label,
                                                modifier = Modifier.size(24.dp)
                                            )
                                        }
                                        is EvaluationIcon.VectorIcon -> {
                                            Icon(
                                                imageVector = iconData.icon,
                                                contentDescription = evaluation.label,
                                                modifier = Modifier.size(24.dp)
                                            )
                                        }
                                    }
                                } ?: Box(modifier = Modifier.size(24.dp))
                            }
                            // 3. Parameter Value + Unit.
                            Text(
                                text = evaluation.value,
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.weight(0.3f)
                            )
                            // 4. Launch Status Icon (if not Wind Direction; for wind direction, skip this column).
                            if (evaluation.label == "Wind Direction") {
                                Box(modifier = Modifier.size(24.dp)) // empty placeholder for alignment
                            } else {
                                LaunchStatusIcon(status = evaluation.status)
                            }
                        }
                    }
                    AtmosphericWindTable(
                        viewModel,
                        coordinates = coordinates,
                        time = Instant.parse(forecastItem.time).closestIsobaricDataWindowBefore(),
                    )
                }
            }
        }
    }
}