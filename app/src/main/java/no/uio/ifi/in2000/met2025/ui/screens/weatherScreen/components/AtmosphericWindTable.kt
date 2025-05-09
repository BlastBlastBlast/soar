package no.uio.ifi.in2000.met2025.ui.screens.weatherScreen.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import no.uio.ifi.in2000.met2025.ui.screens.weatherScreen.components.windcomponents.AWTableContents
import no.uio.ifi.in2000.met2025.ui.screens.weatherScreen.WeatherViewModel
import java.time.Instant

// AtmosphericWindTable.kt
@Composable
fun AtmosphericWindTable(
    viewModel: WeatherViewModel,
    coordinates: Pair<Double, Double>,
    time: Instant
) {
    val isobaricTimeData by viewModel.isobaricData.collectAsState()
    val lastLoadedCoordinates by viewModel.lastIsobaricCoordinates.collectAsState()
    val config by viewModel.activeConfig.collectAsState()

    val effectiveState = if (lastLoadedCoordinates != coordinates) {
        WeatherViewModel.AtmosphericWindUiState.Idle
    } else {
        isobaricTimeData[time] ?: WeatherViewModel.AtmosphericWindUiState.Idle
    }

    when (effectiveState) {
        is WeatherViewModel.AtmosphericWindUiState.Idle -> {
            Button(
                onClick = { viewModel.loadIsobaricData(coordinates.first, coordinates.second, time) },
                modifier = Modifier
                    .padding(top = 8.dp)
                    .fillMaxWidth()
                    .semantics {
                        role = Role.Button
                        contentDescription = "Get isobaric wind data for $time at ${coordinates.first}, ${coordinates.second}"
                    },
                shape = RoundedCornerShape(6.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.onPrimary)
            ) {
                Text(
                    text = "Get Isobaric Data",
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                )
            }
        }
        is WeatherViewModel.AtmosphericWindUiState.Loading -> {
            Box(
                modifier = Modifier
                    .padding(top = 8.dp)
                    .fillMaxWidth().semantics {
                    liveRegion = LiveRegionMode.Assertive
                    contentDescription = "Loading isobaric wind data"
                },
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.Black)
            }
        }
        is WeatherViewModel.AtmosphericWindUiState.Error -> {
            Column {
                Text(
                    text = "Error loading isobaric data: ${effectiveState.message}",
                    modifier = Modifier.semantics {
                        liveRegion = LiveRegionMode.Assertive
                        contentDescription = "Error loading isobaric data: ${effectiveState.message}"
                    },
                    style = MaterialTheme.typography.bodyMedium
                )
                Button(
                    onClick = { viewModel.loadIsobaricData(coordinates.first, coordinates.second, time) },
                    modifier = Modifier
                        .padding(top = 8.dp)
                        .fillMaxWidth()
                        .semantics {
                            role = Role.Button
                            contentDescription = "Retry loading isobaric data"
                        },
                    shape = RoundedCornerShape(6.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.onPrimary)
                ) {
                    Text(text = "Retry loading isobaric data",
                        color = MaterialTheme.colorScheme.primary,
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                    )
                }
            }
        }
        is WeatherViewModel.AtmosphericWindUiState.Success -> {
            if (config == null) {
                Text("Loading configuration...", color = MaterialTheme.colorScheme.onPrimary,
                    style = MaterialTheme.typography.bodyMedium)
            } else {
                HorizontalDivider(thickness = 1.dp, color = MaterialTheme.colorScheme.onPrimary)
                Text(
                    text  = "Atmospheric Wind Data",
                    color = MaterialTheme.colorScheme.onPrimary,
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    modifier = Modifier
                        .padding(vertical = 4.dp)
                        .semantics { heading() }
                )
                HorizontalDivider(thickness = 1.dp, color = MaterialTheme.colorScheme.primary)
                AWTableContents(effectiveState.isobaricData, config!!)
            }
        }
    }
}
