package no.uio.ifi.in2000.met2025.ui.screens.weathercardscreen.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import no.uio.ifi.in2000.met2025.ui.screens.weathercardscreen.components.windcomponents.AWTableContents
import no.uio.ifi.in2000.met2025.ui.screens.weathercardscreen.WeatherCardViewmodel
import java.time.Instant

// AtmosphericWindTable.kt
@Composable
fun AtmosphericWindTable(
    viewModel: WeatherCardViewmodel,
    coordinates: Pair<Double, Double>,
    time: Instant
) {
    val isobaricTimeData by viewModel.isobaricData.collectAsState()
    val lastLoadedCoordinates by viewModel.lastIsobaricCoordinates.collectAsState()
    val config by viewModel.activeConfig.collectAsState()

    val effectiveState = if (lastLoadedCoordinates != coordinates) {
        WeatherCardViewmodel.AtmosphericWindUiState.Idle
    } else {
        isobaricTimeData[time] ?: WeatherCardViewmodel.AtmosphericWindUiState.Idle
    }

    when (effectiveState) {
        is WeatherCardViewmodel.AtmosphericWindUiState.Idle -> {
            Button(
                onClick = { viewModel.loadIsobaricData(coordinates.first, coordinates.second, time) },
                modifier = Modifier
                    .padding(top = 8.dp)
                    .fillMaxWidth()
                    .background(
                        shape = RoundedCornerShape(6.dp),
                        color = MaterialTheme.colorScheme.primary
                    )
            ) {
                Text(text = "Get Isobaric Data")
            }
        }
        is WeatherCardViewmodel.AtmosphericWindUiState.Loading -> {
            Box(
                modifier = Modifier
                    .padding(top = 8.dp)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(modifier = Modifier.size(24.dp))
            }
        }
        is WeatherCardViewmodel.AtmosphericWindUiState.Error -> {
            Column {
                Text(
                    text = "Error loading isobaric data: ${effectiveState.message}",
                    style = MaterialTheme.typography.bodyMedium
                )
                Button(
                    onClick = { viewModel.loadIsobaricData(coordinates.first, coordinates.second, time) },
                    modifier = Modifier
                        .padding(top = 8.dp)
                        .fillMaxWidth()
                        .background(
                            shape = RoundedCornerShape(bottomStart = 4.dp, bottomEnd = 4.dp),
                            color = MaterialTheme.colorScheme.primary
                        )
                ) {
                    Text(text = "Retry loading isobaric data")
                }
            }
        }
        is WeatherCardViewmodel.AtmosphericWindUiState.Success -> {
            if (config == null) {
                Text("Loading configuration...", style = MaterialTheme.typography.bodyMedium)
            } else {
                AWTableContents(effectiveState.isobaricData, config!!)
            }
        }
    }
}
