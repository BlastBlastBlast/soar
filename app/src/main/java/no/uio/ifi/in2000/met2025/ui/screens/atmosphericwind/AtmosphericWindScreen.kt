package no.uio.ifi.in2000.met2025.ui.screens.atmosphericwind

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.mutableStateOf
import androidx.compose.material3.Button
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.setValue
import androidx.hilt.navigation.compose.hiltViewModel
import no.uio.ifi.in2000.met2025.data.local.database.LaunchSite
import no.uio.ifi.in2000.met2025.domain.helpers.firstAvailableIsobaricDataWindowBefore
import no.uio.ifi.in2000.met2025.ui.screens.atmosphericwind.components.AWTableContents
import java.time.Duration
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@Composable
fun AtmosphericWindScreen(
    atmosphericWindViewModel: AtmosphericWindViewModel = hiltViewModel(),
) {
    val dataMap by atmosphericWindViewModel.isobaricData.collectAsState()
    val coordinates by atmosphericWindViewModel.launchSite.collectAsState()
    val latitude = coordinates?.latitude ?: 0.0
    val longitude = coordinates?.longitude ?: 0.0

    val currentTime = Instant.now()
    val validTime = currentTime.firstAvailableIsobaricDataWindowBefore()

    when (coordinates) {
        is LaunchSite -> {
            ScreenContent(
                validTime = validTime,
                dataMap = dataMap,
                coordinates = Pair(latitude, longitude),
                onLoadData = { lat, lon, time ->
                    atmosphericWindViewModel.loadIsobaricData(lat, lon, time)
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
    onLoadData: (Double, Double, Instant) -> Unit = { _, _, _ -> }
) {
    var dataCount by remember { mutableIntStateOf(0) } //TODO: maybe use size of map in viewModel
    var currentTime by remember { mutableStateOf(validTime) }

    Box{
        Column(modifier = Modifier.padding(16.dp)) {
            LazyColumn {
                items(8) { index ->
                    val itemTime = validTime.plus(Duration.ofHours(index.toLong() * 3))
                    val formattedItemTime = itemTime.atZone(ZoneId.systemDefault()).format(DateTimeFormatter.ofPattern("HH:mm"))

                    when (val dataItem = dataMap[itemTime]) {
                        is AtmosphericWindViewModel.AtmosphericWindUiState.Idle -> {
                            Text(
                                text = "Idle state for $formattedItemTime",
                                style = MaterialTheme.typography.headlineSmall
                            )
                        }
                        is AtmosphericWindViewModel.AtmosphericWindUiState.Loading -> {
                            Text(
                                text = "Loading data for $formattedItemTime...",
                                style = MaterialTheme.typography.headlineSmall
                            )
                        }
                        is AtmosphericWindViewModel.AtmosphericWindUiState.Error -> {
                            Text(
                                text = "Error: ${dataItem.message}",
                                style = MaterialTheme.typography.headlineSmall
                            )
                        }
                        is AtmosphericWindViewModel.AtmosphericWindUiState.Success -> {
                            AWTableContents(item = dataItem.isobaricData)
                        }
                        else -> {
                        }
                    }
                }
            }
        }
        AnimatedVisibility(visible = dataCount < 8) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
                ) {
                Text(
                    text = "",
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier.weight(1f)
                )
                Button(
                    onClick = {
                        onLoadData(coordinates.first, coordinates.second, currentTime)
                        currentTime = currentTime.plus(Duration.ofHours(3))
                        dataCount++
                    },
                    shape = RoundedCornerShape(50),
                    modifier = Modifier
                        .padding(16.dp)
                        .padding(top = 16.dp, end = 16.dp)
                ) {
                    Text("+")
                }
            }
        }
    }
}
