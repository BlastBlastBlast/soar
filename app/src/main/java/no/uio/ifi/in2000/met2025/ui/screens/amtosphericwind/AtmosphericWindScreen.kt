package no.uio.ifi.in2000.met2025.ui.screens.amtosphericwind

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.foundation.shape.RoundedCornerShape
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
import no.uio.ifi.in2000.met2025.data.models.Constants
import no.uio.ifi.in2000.met2025.data.models.IsobaricDataValues
import java.math.RoundingMode
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import kotlin.math.pow
import kotlin.math.sqrt

//TODO: hook up screen to navigation graph

//TODO: make card scrollable and expandable, showing data closer to ground first

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
    return sqrt(s1.pow(2) + s2.pow(2) - 2 * s1 * s2 * kotlin.math.cos(v2.windFromDirection - v1.windFromDirection))
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

    Column(modifier = Modifier.padding(16.dp)) {
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
    val windshearColor = Color(0xFFd7ebfa)

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
            Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                Text(
                    text = "Altitude",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = "Wind Speed",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = "Wind Direction",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.weight(1f)
                )
            }

            // Static header row to label columns
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
//                    .border(
//                        BorderStroke(1.dp, Color.LightGray),
//                        RoundedCornerShape(4.dp)
//                    )
                    .background(
                        color = windshearColor,
                        shape = RoundedCornerShape(4.dp)
                    )
                    .padding(4.dp) // Add border and padding for visual offset
            ) {
                Text(
                    text = "",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = "Wind Shear",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = "",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.weight(1f)
                )
            }
            HorizontalDivider(thickness = 1.dp, color = Color.Gray)

            // Iterate through layer pressure values and display data
            val pressureValues = item.valuesAtLayer.keys.sorted()
            pressureValues.forEachIndexed { index, layer ->
                val altitude = item.valuesAtLayer[layer]?.altitude ?: "--"
                val windSpeed = item.valuesAtLayer[layer]?.windSpeed ?: "--"
                val windDirection = item.valuesAtLayer[layer]?.windFromDirection ?: "--"

                Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                    Text(
                        text = "$altitude m",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        text = "$windSpeed m/s",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        text = "$windDirection°",
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.weight(1f)
                    )
                }

                // Calculate wind shear if not the last item in list and both layers exist
                if (index < pressureValues.size - 1) {
                    val nextLayer = pressureValues[index + 1]

                    if (item.valuesAtLayer[layer] != null && item.valuesAtLayer[nextLayer] != null) {
                        val windShearValue = windShear(
                            item.valuesAtLayer[layer]!!,
                            item.valuesAtLayer[nextLayer]!!
                        )

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
//                                .border(
//                                    BorderStroke(1.dp, Color.LightGray),
//                                    RoundedCornerShape(4.dp)
//                                )
                                .background(
                                    color = windshearColor,
                                    shape = RoundedCornerShape(4.dp)
                                )
                                .padding(4.dp) // Add border and padding for visual offset
                        ) {
                            Text(
                                text = "",
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.weight(1f)
                            )
                            Text(
                                text = "${windShearValue.roundToDecimals(1)} m/s",
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.weight(1f)
                            )
                            Text(
                                text = "",
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }
        }
    }
}

//@Composable
//fun IsobaricDataItemCard(
//    item: IsobaricDataItem
//) {
//    val cardBackgroundColor = Color(0xFFE3F2FD)
//
//    Card(
//        modifier = Modifier
//            .fillMaxWidth()
//            .padding(vertical = 8.dp),
//        colors = CardDefaults.cardColors(containerColor = cardBackgroundColor),
//        shape = RoundedCornerShape(corner = CornerSize(8.dp))
//    ) {
//        Column(modifier = Modifier.padding(16.dp)) {
//            Text(
//                text = formatZuluTimeToLocal(item.time),
//                style = MaterialTheme.typography.headlineSmall,
//                modifier = Modifier.padding(bottom = 8.dp)
//            )
//            HorizontalDivider(thickness = 1.dp, color = Color.Gray)
//
//            // Static header row to label columns
//            Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
//                Text(
//                    text = "Altitude",
//                    style = MaterialTheme.typography.bodyMedium,
//                    modifier = Modifier.weight(1f)
//                )
//                Text(
//                    text = "Wind Speed",
//                    style = MaterialTheme.typography.bodyMedium,
//                    modifier = Modifier.weight(1f)
//                )
//                Text(
//                    text = "Wind Direction",
//                    style = MaterialTheme.typography.bodyMedium,
//                    modifier = Modifier.weight(1f)
//                )
//            }
//            HorizontalDivider(thickness = 1.dp, color = Color.Gray)
//
//            // Data rows: dynamic content for each pressure layer
//            Constants.layerPressureValues.forEach { layer ->
//                val altitude = item.valuesAtLayer[layer]?.altitude ?: "--"
//                val windSpeed = item.valuesAtLayer[layer]?.windSpeed ?: "--"
//                val windDirection = item.valuesAtLayer[layer]?.windFromDirection ?: "--"
//
//                Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
//                    Text(
//                        text = "$altitude m",
//                        style = MaterialTheme.typography.bodyMedium,
//                        modifier = Modifier.weight(1f)
//                    )
//                    Text(
//                        text = "$windSpeed m/s",
//                        style = MaterialTheme.typography.bodyMedium,
//                        modifier = Modifier.weight(1f)
//                    )
//                    Text(
//                        text = "$windDirection°",
//                        style = MaterialTheme.typography.bodyMedium,
//                        modifier = Modifier.weight(1f)
//                    )
//                }
//                HorizontalDivider(thickness = 0.5.dp, color = Color.Gray)
//            }
//        }
//    }
//}

