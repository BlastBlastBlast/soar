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
import androidx.compose.material.icons.Icons
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import no.uio.ifi.in2000.met2025.data.models.ForecastDataItem
import no.uio.ifi.in2000.met2025.data.models.safetyevaluation.LaunchStatusIcon
import no.uio.ifi.in2000.met2025.data.models.safetyevaluation.LaunchStatusIndicator
import no.uio.ifi.in2000.met2025.data.models.safetyevaluation.evaluateParameterConditions
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import no.uio.ifi.in2000.met2025.R
import no.uio.ifi.in2000.met2025.data.local.database.ConfigProfile
import no.uio.ifi.in2000.met2025.data.models.safetyevaluation.EvaluationIcon
import no.uio.ifi.in2000.met2025.data.models.safetyevaluation.EvaluationIcon.DrawableIcon
import no.uio.ifi.in2000.met2025.domain.helpers.formatZuluTimeToLocalTime
import no.uio.ifi.in2000.met2025.domain.helpers.formatZuluTimeToLocalDate
import no.uio.ifi.in2000.met2025.domain.helpers.closestIsobaricDataWindowBefore
import no.uio.ifi.in2000.met2025.ui.screens.weathercardscreen.WeatherCardViewmodel
import no.uio.ifi.in2000.met2025.ui.theme.IconGreen
import no.uio.ifi.in2000.met2025.ui.theme.IconRed
import no.uio.ifi.in2000.met2025.ui.theme.IconYellow
import no.uio.ifi.in2000.met2025.ui.theme.WarmOrange
import java.time.Instant

@Composable
fun WindDirectionIcon(windDirection: Double?) {
    if (windDirection == null) {
        return
    }
    val arrowPainter = painterResource(id = no.uio.ifi.in2000.met2025.R.drawable.up_arrow)
    val rotation = (windDirection + 180) % 360

    // If needed, you can also set a tint on the image if it's a vector asset:
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
    viewModel: WeatherCardViewmodel
) {
    var expanded by remember { mutableStateOf(false) }
    val isobaricDataMap by viewModel.isobaricData.collectAsState()

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { expanded = !expanded },
        colors = CardDefaults.cardColors(
            containerColor = WarmOrange,   // Card background is warm orange.
            contentColor = Color.Black  // Default content color inside the card.
        ),
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
                    style = MaterialTheme.typography.headlineLarge,
                    color = Color.Black
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    when (val isobaricData = isobaricDataMap[
                        Instant.parse(forecastItem.time)
                            .closestIsobaricDataWindowBefore()
                    ]) {
                        is WeatherCardViewmodel.AtmosphericWindUiState.Success -> {
                            Icon(
                                painter = painterResource(id = R.drawable.yes_grib_real),
                                contentDescription = "Altitude",
                                modifier = Modifier.size(48.dp),
                                tint = Color.Black
                            )
                            LaunchStatusIndicator(
                                config = config,
                                forecast = forecastItem,
                                isobaricData.isobaricData,
                                modifier = Modifier.size(38.dp)
                            )
                        }
                        else -> {
                            Icon(
                                painter = painterResource(id = R.drawable.no_grib_real),
                                contentDescription = "No Altitude",
                                modifier = Modifier.size(48.dp),
                                tint = Color.Black
                            )
                            LaunchStatusIndicator(
                                config = config,
                                forecast = forecastItem,
                                modifier = Modifier.size(38.dp)
                            )
                        }
                    }
                }
            }
            Text(
                text = "Temperature: ${forecastItem.values.airTemperature}°C",
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
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
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                modifier = Modifier.weight(0.5f),
                            )
                            // 2. Parameter Icon.
                            if (evaluation.label == "Wind Direction") {
                                WindDirectionIcon(forecastItem.values.windFromDirection)
                            } else {
                                evaluation.icon?.let { iconData ->
                                    when (iconData) {
                                        is DrawableIcon -> {
                                            Icon(
                                                painter = painterResource(id = iconData.resId),
                                                contentDescription = evaluation.label,
                                                modifier = Modifier.size(24.dp),
                                            )
                                        }
                                        is EvaluationIcon.VectorIcon -> {
                                            Icon(
                                                imageVector = iconData.icon,
                                                contentDescription = evaluation.label,
                                                modifier = Modifier.size(24.dp),
                                            )
                                        }
                                    }
                                } ?: Box(modifier = Modifier.size(24.dp))
                            }
                            // 3. Parameter Value + Unit.
                            Text(
                                text = evaluation.value,
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                modifier = Modifier.weight(0.3f),
                            )
                            // 4. Launch Status Icon (if not Wind Direction; for wind direction, skip this column).
                            if (evaluation.label == "Wind Direction") {
                                Box(modifier = Modifier.size(24.dp)) // empty placeholder for alignment
                            } else {
                                LaunchStatusIcon(
                                    state = evaluation.state,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }
                    }
                    AtmosphericWindTable(
                        viewModel,
                        coordinates = coordinates,
                        time = Instant.parse(forecastItem.time)
                            .closestIsobaricDataWindowBefore()
                    )
                }
            }
        }
    }
}