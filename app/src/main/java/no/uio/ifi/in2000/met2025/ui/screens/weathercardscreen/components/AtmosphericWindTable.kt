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
import androidx.hilt.navigation.compose.hiltViewModel
import no.uio.ifi.in2000.met2025.ui.screens.atmosphericwind.AtmosphericWindViewModel
import no.uio.ifi.in2000.met2025.ui.screens.atmosphericwind.components.AWTableContents
import java.time.Instant

@Composable
fun AtmosphericWindTable(
    atmosphericWindViewModel: AtmosphericWindViewModel = hiltViewModel(),
    coordinates: Pair<Double, Double>,
    time: Instant
) {
    val onClickGetIsobaricData = { atmosphericWindViewModel.loadIsobaricData(coordinates.first, coordinates.second, time) }

    val isobaricTimeData by atmosphericWindViewModel.isobaricData.collectAsState()
    val isobaricDataUiState = isobaricTimeData[time] ?: AtmosphericWindViewModel.AtmosphericWindUiState.Idle

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
            AWTableContents(isobaricDataUiState.isobaricData)
        }
    }
}
