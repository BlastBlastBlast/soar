package no.uio.ifi.in2000.met2025.ui.screens.amtosphericwind

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
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
import java.text.SimpleDateFormat
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.Date
import java.util.Locale

//TODO: hook up screen to navigation graph

//TODO: make preview with mock data

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

//@Composable
//fun IsobaricDataItemCard(
//    item: IsobaricDataItem
//) {
//    val cardBackgroundColor = Color(0xFFE3F2FD)
//
//    val formattedTime = try {
//        val inputDateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault())
//        val outputTimeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
//        val date = inputDateFormat.parse(item.time)
//        outputTimeFormat.format(date ?: Date())
//    } catch (e: Exception) {
//        "--:--"
//    }
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
//                text = "Time: $formattedTime",
//                style = MaterialTheme.typography.headlineSmall,
//                modifier = Modifier.padding(bottom = 8.dp)
//            )
//            HorizontalDivider(thickness = 1.dp, color = Color.Gray)
//
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
//                        text = "WS: $windSpeed m/s",
//                        style = MaterialTheme.typography.bodyMedium,
//                        modifier = Modifier.weight(1f)
//                    )
//                    Text(
//                        text = "WD: $windDirection°",
//                        style = MaterialTheme.typography.bodyMedium,
//                        modifier = Modifier.weight(1f)
//                    )
//                    // Optionally, add icons here to represent data visually
//                }
//                HorizontalDivider(thickness = 0.5.dp, color = Color.Gray)
//            }
//        }
//    }
//}

@Composable
fun IsobaricDataItemCard(
    item: IsobaricDataItem
) {
    val cardBackgroundColor = Color(0xFFE3F2FD)

    val formattedTime = try {
        val inputDateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault())
        val outputTimeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
        val date = inputDateFormat.parse(item.time)
        outputTimeFormat.format(date ?: Date())
    } catch (e: Exception) {
        "--:--"
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        colors = CardDefaults.cardColors(containerColor = cardBackgroundColor),
        shape = RoundedCornerShape(corner = CornerSize(8.dp))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Time: $formattedTime",
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
            HorizontalDivider(thickness = 1.dp, color = Color.Gray)

            // Data rows: dynamic content for each pressure layer
            Constants.layerPressureValues.forEach { layer ->
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
                HorizontalDivider(thickness = 0.5.dp, color = Color.Gray)
            }
        }
    }
}

