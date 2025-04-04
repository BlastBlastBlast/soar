package no.uio.ifi.in2000.met2025.ui.screens.atmosphericwind

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import no.uio.ifi.in2000.met2025.data.models.IsobaricData
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.graphics.Color
import androidx.compose.material3.Icon
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.runtime.MutableState
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.geometry.Size
import androidx.hilt.navigation.compose.hiltViewModel
import no.uio.ifi.in2000.met2025.data.local.Database.LaunchSite
import no.uio.ifi.in2000.met2025.domain.helpers.roundToDecimals
import no.uio.ifi.in2000.met2025.domain.helpers.formatZuluTimeToLocal
import no.uio.ifi.in2000.met2025.domain.helpers.floorModDouble
import no.uio.ifi.in2000.met2025.domain.helpers.windShearDirection
import no.uio.ifi.in2000.met2025.domain.helpers.windShearSpeed
import java.time.Duration
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

@Composable
fun AtmosphericWindScreen(
    atmosphericWindViewModel: AtmosphericWindViewModel = hiltViewModel(),
    //launchSiteViewModel: LaunchSiteViewModel = hiltViewModel()
) {
    val dataMap by atmosphericWindViewModel.isobaricData.collectAsState()
    val coordinates by atmosphericWindViewModel.launchSite.collectAsState()
    val latitude = coordinates?.latitude ?: 0.0
    val longitude = coordinates?.longitude ?: 0.0

    val currentTime = Instant.now()
    val currentHour = LocalDateTime.ofInstant(currentTime, ZoneId.systemDefault()).truncatedTo(
        ChronoUnit.HOURS)
    val nextDivisibleHour = generateSequence(currentHour) { it.plusHours(1) }
        .first { it.hour % 3 == 0 }
    val validTime = nextDivisibleHour.atZone(ZoneId.systemDefault()).toInstant()

    when (coordinates) {
        is LaunchSite -> {
            ScreenContent(
                validTime = validTime,
                dataMap = dataMap,
                coordinates = Pair(latitude, longitude),
                onLoadData = { lat, lon ->
                    atmosphericWindViewModel.loadAllAvailableIsobaricDataInOrder(lat, lon)
                }
            )
        }
        else -> {
            Text("Failed to load coordinates")
        }
    }
}

@Composable
fun ScreenContent(
    validTime: Instant,
    dataMap: Map<Instant, AtmosphericWindViewModel.AtmosphericWindUiState>,
    coordinates: Pair<Double, Double>,
    onLoadData: (Double, Double) -> Unit = { _, _ -> }
) {

    if (dataMap.isEmpty()) {
        onLoadData(coordinates.first, coordinates.second)
    }
    LazyColumn(Modifier.padding(16.dp)) {
        items(8) { index ->
            val itemTime = validTime.plus(Duration.ofHours(index.toLong() * 3))

            val memeTime = itemTime.minus(Duration.ofHours(1)) //TODO: find a better way to display the correct time
            val formattedItemTime = memeTime.atZone(ZoneId.systemDefault()).format(DateTimeFormatter.ofPattern("HH:mm"))

            when (val dataItem = dataMap[itemTime]) {
                is AtmosphericWindViewModel.AtmosphericWindUiState.Error -> {
                    Text(
                        text = "Error: ${dataItem.message}",
                        style = MaterialTheme.typography.headlineSmall
                    )
                }
                is AtmosphericWindViewModel.AtmosphericWindUiState.Loading -> {
                    Text(
                        text = "Loading data for $formattedItemTime...",
                        style = MaterialTheme.typography.headlineSmall
                    )
                }
                is AtmosphericWindViewModel.AtmosphericWindUiState.Idle -> {
                    Text(
                        text = "Idle state for $formattedItemTime",
                        style = MaterialTheme.typography.headlineSmall
                    )
                }
                is AtmosphericWindViewModel.AtmosphericWindUiState.Success -> {
                    IsobaricDataItemCard(item = dataItem.isobaricData)
                }
                else -> {
                }
            }
        }
    }
}

@Composable
fun IsobaricDataItemCard(
    item: IsobaricData
) {
    val cardBackgroundColor = Color(0xFFE3F2FD)
    val windShearColor = Color(0xFFe2e0ff)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
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
                        .graphicsLayer(rotationZ = if (expanded.value) 0f else 180f)
                )

                // Iterate through layer pressure values and display data
                val pressureValues = item.valuesAtLayer.keys.sorted()
                val displayedValues = if (expanded.value) pressureValues else pressureValues.takeLast(6)
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
fun CornerBorderColumn(content: @Composable (ColumnScope.(MutableState<Boolean>) -> Unit)) {
    val expanded = remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, Color.Gray, shape = CustomRoundedCornerShape(8.dp))
            .padding(top = 8.dp/*, bottom = 8.dp*/)
            .clickable { expanded.value = !expanded.value }
    ) {
        Column {
            content(expanded)
        }
    }
}

