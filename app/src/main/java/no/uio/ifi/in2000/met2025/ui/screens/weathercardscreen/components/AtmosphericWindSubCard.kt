package no.uio.ifi.in2000.met2025.ui.screens.weathercardscreen.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import no.uio.ifi.in2000.met2025.data.local.Database.LaunchSite
import no.uio.ifi.in2000.met2025.data.models.IsobaricData
import no.uio.ifi.in2000.met2025.domain.helpers.floorModDouble
import no.uio.ifi.in2000.met2025.domain.helpers.formatZuluTimeToLocal
import no.uio.ifi.in2000.met2025.domain.helpers.roundToDecimals
import no.uio.ifi.in2000.met2025.domain.helpers.windShearDirection
import no.uio.ifi.in2000.met2025.domain.helpers.windShearSpeed
import no.uio.ifi.in2000.met2025.ui.screens.atmosphericwind.AtmosphericWindViewModel
import java.time.Duration
import java.time.Instant

@Composable
fun AtmosphericWindSubCard(
    isobaricDataUiState: AtmosphericWindViewModel.AtmosphericWindUiState,
    onClickGetIsobaricData: () -> Unit = {},
) {
    when (isobaricDataUiState) {
        is AtmosphericWindViewModel.AtmosphericWindUiState.Idle -> {
            Button(
                onClick = { onClickGetIsobaricData() },
                modifier = Modifier
                    .padding(top = 8.dp)
                    .fillMaxWidth()
                    .background(shape = RoundedCornerShape(6.dp), color = MaterialTheme.colorScheme.primary)
            ) {
                Text(text = "Get Isobaric Data")
            }
        }
        is AtmosphericWindViewModel.AtmosphericWindUiState.Loading -> {
            Text(
                text = "Loading isobaric data...",
                style = MaterialTheme.typography.bodyMedium
            )
        }
        is AtmosphericWindViewModel.AtmosphericWindUiState.Error -> {
            Text(
                text = "Error loading isobaric data: ${isobaricDataUiState.message}",
                style = MaterialTheme.typography.bodyMedium
            )
            Button(
                onClick = { onClickGetIsobaricData() },
                modifier = Modifier
                    .padding(top = 8.dp)
                    .fillMaxWidth()
                    .background(shape = RoundedCornerShape(bottomStart = 4.dp, bottomEnd = 4.dp), color = MaterialTheme.colorScheme.primary)
            ) {
                Text(text = "Retry loading isobaric data")
            }
        }
        is AtmosphericWindViewModel.AtmosphericWindUiState.Success -> {
            AtmosphericWindSubCardContent(isobaricDataUiState.isobaricData)
        }
    }
}

@Composable
fun AtmosphericWindSubCardContent(
    item: IsobaricData
) {
    val cardBackgroundColor = Color(0xFFE3F2FD)
    val windShearColor = Color(0xFFe2e0ff)

    var expanded by remember { mutableStateOf(true) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        colors = CardDefaults.cardColors(containerColor = cardBackgroundColor),
        shape = RoundedCornerShape(corner = CornerSize(8.dp))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row {
                Text(
                    text = formatZuluTimeToLocal(item.time),
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                Spacer(modifier = Modifier.weight(1f))
                Button(
                    onClick = {expanded = !expanded},
                    content = {
                        Text(
                            text = if (expanded) "-" else "+",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    },
                )
            }
            if (expanded) {
                HorizontalDivider(thickness = 1.dp, color = Color.Gray)

                // Static header row to label columns
                AtmosphericLayerRow(
                    altitudeText = "Altitude",
                    windSpeedText = "Wind Speed",
                    windDirectionText = "Wind Direction",
                    style = MaterialTheme.typography.titleSmall
                )

                // Static header row to label columns
                WindShearRow(
                    backgroundColor = windShearColor,
                    speedText = "Wind Shear Speed",
                    directionText = "Wind Shear Direction",
                    style = MaterialTheme.typography.titleSmall
                )

                //HorizontalDivider(thickness = 1.dp, color = Color.Gray, modifier = Modifier.padding(bottom = 8.dp))
                Spacer(modifier = Modifier.height(8.dp))

                CornerBorderColumn { expanded ->
                    Icon(
                        imageVector = Icons.Default.ExpandMore,
                        contentDescription = "Expand",
                        tint = Color.Gray,
                        modifier = Modifier
                            .fillMaxWidth()
                            .graphicsLayer(rotationZ = if (expanded) 0f else 180f)
                    )

                    // Iterate through layer pressure values and display data
                    val pressureValues = item.valuesAtLayer.keys.sorted()
                    val displayedValues = if (expanded) pressureValues else pressureValues.takeLast(6)
                    displayedValues.forEachIndexed { index, layer ->
                        val altitude = item.valuesAtLayer[layer]?.altitude?.toInt() ?: "--"
                        val windSpeed = item.valuesAtLayer[layer]?.windSpeed
                            ?.roundToDecimals(1) ?: "--"
                        val windDirection = item.valuesAtLayer[layer]?.windFromDirection
                            ?.floorModDouble(360)
                            ?.roundToDecimals(1) ?: "--"

                        AtmosphericLayerRow(
                            altitudeText = "$altitude m",
                            windSpeedText = "$windSpeed m/s",
                            windDirectionText = "$windDirection°",
                            style = MaterialTheme.typography.bodyMedium
                        )

                        // Calculate wind shear if not the last item in list and both layers exist
                        if (index < displayedValues.size - 1) {
                            val nextLayer = displayedValues[index + 1]

                            if (item.valuesAtLayer[layer] != null && item.valuesAtLayer[nextLayer] != null) {
                                val windShearValue = windShearSpeed(
                                    item.valuesAtLayer[layer]!!,
                                    item.valuesAtLayer[nextLayer]!!
                                )
                                    .roundToDecimals(1)

                                val windShearDirection = windShearDirection(
                                    item.valuesAtLayer[layer]!!,
                                    item.valuesAtLayer[nextLayer]!!
                                )
                                    .floorModDouble(360).roundToDecimals(1)

                                WindShearRow(
                                    backgroundColor = windShearColor,
                                    speedText = "$windShearValue m/s",
                                    directionText = "$windShearDirection°",
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AtmosphericLayerRow(
    altitudeText: String,
    windSpeedText: String,
    windDirectionText: String,
    style: androidx.compose.ui.text.TextStyle
) {
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
        Text(
            text = altitudeText,
            style = style,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = windSpeedText,
            style = style,
            modifier = Modifier.weight(1f)
        )
        Text( //TODO: Add wind direction icon. Pointing downwards at 0° rotating clockwise
            text = windDirectionText,
            style = style,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
fun WindShearRow(
    backgroundColor: Color,
    speedText: String,
    directionText: String,
    style: androidx.compose.ui.text.TextStyle
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .background(
                color = backgroundColor,
                shape = RoundedCornerShape(4.dp)
            )
            .padding(4.dp) // Add border and padding for visual offset
    ) {
        Box(modifier = Modifier.weight(1f))

        Text(
            text = speedText,
            style = style,
            modifier = Modifier.weight(1f)
        )

        Text(
            text = directionText,
            style = style,
            modifier = Modifier.weight(1f)
        )
    }
}

class CustomRoundedCornerShape(private val cornerSize: Dp) : Shape {
    override fun createOutline(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density
    ): Outline {
        val cornerRadius = with(density) { cornerSize.toPx() }
        return Outline.Generic(Path().apply {
            // Top-left corner
            moveTo(0f, cornerRadius)
            arcTo(
                rect = androidx.compose.ui.geometry.Rect(0f, 0f, cornerRadius * 2, cornerRadius * 2),
                startAngleDegrees = 180f,
                sweepAngleDegrees = 90f,
                forceMoveTo = false
            )
            // Top horizontal line
            lineTo(size.width - cornerRadius, 0f)
            // Top-right corner
            arcTo(
                rect = androidx.compose.ui.geometry.Rect(size.width - cornerRadius * 2, 0f, size.width, cornerRadius * 2),
                startAngleDegrees = 270f,
                sweepAngleDegrees = 90f,
                forceMoveTo = false
            )
//            // Bottom-right corner
//            moveTo(size.width, size.height - cornerRadius)
//            arcTo(
//                rect = androidx.compose.ui.geometry.Rect(size.width - cornerRadius * 2, size.height - cornerRadius * 2, size.width, size.height),
//                startAngleDegrees = 0f,
//                sweepAngleDegrees = 90f,
//                forceMoveTo = false
//            )
//            // Bottom horizontal line
//            lineTo(cornerRadius, size.height)
//            // Bottom-left corner
//            arcTo(
//                rect = androidx.compose.ui.geometry.Rect(0f, size.height - cornerRadius * 2, cornerRadius * 2, size.height),
//                startAngleDegrees = 90f,
//                sweepAngleDegrees = 90f,
//                forceMoveTo = false
//            )
        })
    }
}

@Composable
fun CornerBorderColumn(content: @Composable (ColumnScope.(Boolean) -> Unit)) {
    var expanded by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, Color.Gray, shape = CustomRoundedCornerShape(8.dp))
            .padding(top = 8.dp/*, bottom = 8.dp*/)
            .clickable { expanded = !expanded }
    ) {
        Column {
            content(expanded)
        }
    }
}