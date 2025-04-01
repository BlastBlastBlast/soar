package no.uio.ifi.in2000.met2025.ui.screens.amtosphericwind

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
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import no.uio.ifi.in2000.met2025.data.models.IsobaricDataItem
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.graphics.Color
import no.uio.ifi.in2000.met2025.data.models.IsobaricDataValues
import java.math.RoundingMode
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import kotlin.math.pow
import kotlin.math.sqrt
import androidx.compose.material3.Icon
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.geometry.Size

//TODO: hook up screen to navigation graph

//TODO: move this function to another file, it is also defined and used in HourlyExpandableCard
fun formatZuluTimeToLocal(zuluTime: String): String {
    // Parse the ISO date‑time string (Zulu/UTC format)
    val zonedDateTime = ZonedDateTime.parse(zuluTime)
    // Convert the time to the system default timezone (or specify ZoneId.of("Europe/Oslo"))
    val localTime = zonedDateTime.withZoneSameInstant(ZoneId.systemDefault())
    // Format as 24‑h time
    return localTime.format(DateTimeFormatter.ofPattern("HH:mm"))
}

fun windShear(v1: IsobaricDataValues, v2: IsobaricDataValues): Double {
    val s1 = v1.windSpeed
    val s2 = v2.windSpeed
    return sqrt(s1.pow(2) + s2.pow(2) - 2 * s1 * s2 * kotlin.math.cos((v2.windFromDirection - v1.windFromDirection) * Math.PI / 180))
}

//TODO: move function to another file, it could also be used in LocationForecastDataSource
fun Double.roundToDecimals(n: Int) : Double {
    return this.toBigDecimal().setScale(n, RoundingMode.HALF_UP).toDouble()
}

@Composable
fun AtmosphericWindScreen(viewModel: AtmosphericWindViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState.collectAsState()
    ScreenContent(uiState = uiState, onLoadData = { lat, lon ->
        viewModel.loadIsobaricData(lat, lon)
    })
}

@Composable
fun ScreenContent(
    uiState: AtmosphericWindViewModel.AtmosphericWindUiState,
    onLoadData: (Double, Double) -> Unit = { _, _ -> }
) {
    // Pre-fill with coordinates for Ole Johan Dahl's hus
    //TODO: Change to use coordinates from HomeScreen
    var latInput by remember { mutableStateOf("59.942") }
    var lonInput by remember { mutableStateOf("10.726") }

    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .padding(16.dp)
            .verticalScroll(scrollState)
    ) {
        when (uiState) {
            is AtmosphericWindViewModel.AtmosphericWindUiState.Idle -> {
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
                            onLoadData(lat, lon)
                        }
                    },
                    modifier = Modifier.padding(top = 8.dp)
                ) {
                    Text(text = "Load Isobaric Data")
                }
            }
            is AtmosphericWindViewModel.AtmosphericWindUiState.Loading -> {
                Text(text = "Loading...", style = MaterialTheme.typography.headlineSmall)
            }
            is AtmosphericWindViewModel.AtmosphericWindUiState.Error -> {
                Text(text = "Error: ${uiState.message}", style = MaterialTheme.typography.headlineSmall)
            }
            is AtmosphericWindViewModel.AtmosphericWindUiState.Success -> {
                uiState.isobaricData.timeSeries.forEach { item ->
                    IsobaricDataItemCard(item = item)
                }
            }
        }
    }
}

@Composable
fun IsobaricDataItemCard(
    item: IsobaricDataItem
) {
    val cardBackgroundColor = Color(0xFFE3F2FD)
    val windshearColor = Color(0xFFe2e0ff)
    val expanded = remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .clickable { expanded.value = !expanded.value },
        colors = CardDefaults.cardColors(containerColor = cardBackgroundColor),
        shape = RoundedCornerShape(corner = CornerSize(8.dp))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = formatZuluTimeToLocal(item.time),
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.padding(bottom = 8.dp)
            )
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
                backgroundColor = windshearColor,
                text = "Wind Shear",
                style = MaterialTheme.typography.titleSmall
            )

            //HorizontalDivider(thickness = 1.dp, color = Color.Gray, modifier = Modifier.padding(bottom = 8.dp))
            Spacer(modifier = Modifier.height(8.dp))

            CornerBorderColumn {
                Icon(
                    imageVector = Icons.Default.ExpandMore,
                    contentDescription = "Expand",
                    tint = Color.Gray,
                    modifier = Modifier
                        .fillMaxWidth()
                        //.padding(8.dp)
                        .graphicsLayer(rotationZ = if (expanded.value) 0f else 180f)
                )

                // Iterate through layer pressure values and display data
                val pressureValues = item.valuesAtLayer.keys.sorted()
                val displayedValues = if (expanded.value) pressureValues else pressureValues.takeLast(6)
                displayedValues.forEachIndexed { index, layer ->
                    val altitude = item.valuesAtLayer[layer]?.altitude?.toInt() ?: "--"
                    val windSpeed = item.valuesAtLayer[layer]?.windSpeed?.roundToDecimals(1) ?: "--"
                    val windDirection = item.valuesAtLayer[layer]?.windFromDirection?.roundToDecimals(1) ?: "--"

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
                            val windShearValue = windShear(
                                item.valuesAtLayer[layer]!!,
                                item.valuesAtLayer[nextLayer]!!
                            )

                            WindShearRow(
                                backgroundColor = windshearColor,
                                text = "${windShearValue.roundToDecimals(1)} m/s",
                                style = MaterialTheme.typography.bodyMedium
                            )
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
        Text(
            text = windDirectionText,
            style = style,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
fun WindShearRow(
    backgroundColor: Color,
    text: String,
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
            text = text,
            style = style,
            modifier = Modifier.weight(1f)
        )

        Box(modifier = Modifier.weight(1f))
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
fun CornerBorderColumn(content: @Composable() (ColumnScope.() -> Unit)) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, Color.Gray, shape = CustomRoundedCornerShape(8.dp))
            .padding(top = 8.dp/*, bottom = 8.dp*/)
    ) {
        Column {
            content()
        }
    }
}

