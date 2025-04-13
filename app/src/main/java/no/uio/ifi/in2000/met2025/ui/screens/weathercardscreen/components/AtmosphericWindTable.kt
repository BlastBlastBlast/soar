package no.uio.ifi.in2000.met2025.ui.screens.weathercardscreen.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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
    // Collect both the isobaric data state and last loaded coordinates from the viewmodel.
    val isobaricTimeData by viewModel.isobaricData.collectAsState()
    val lastLoadedCoordinates by viewModel.lastIsobaricCoordinates.collectAsState()

    // Determine if the current coordinates match the ones for which data was last loaded.
    // If not, we force the UI state to Idle so that the button is shown.
    val effectiveState = if (lastLoadedCoordinates != coordinates) {
        WeatherCardViewmodel.AtmosphericWindUiState.Idle
    } else {
        isobaricTimeData[time] ?: WeatherCardViewmodel.AtmosphericWindUiState.Idle
    }

    // Render the UI based on the effective state.
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
            Text(
                text = "Loading isobaric data...",
                style = MaterialTheme.typography.bodyMedium
            )
        }
        is WeatherCardViewmodel.AtmosphericWindUiState.Error -> {
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
        is WeatherCardViewmodel.AtmosphericWindUiState.Success -> {
            AWTableContents(effectiveState.isobaricData)
        }
    }
}
