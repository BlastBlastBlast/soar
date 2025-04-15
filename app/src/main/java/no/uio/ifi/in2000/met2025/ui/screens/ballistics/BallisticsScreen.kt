package no.uio.ifi.in2000.met2025.ui.screens.ballistics

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel

@Composable
fun BallisticsScreen(
    viewModel: BallisticsViewModel = hiltViewModel()
) {
    // Observe the default configuration from the viewmodel.
    val defaultConfig by viewModel.defaultRocketConfig.collectAsState(initial = null)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Ballistics Screen Placeholder",
            style = MaterialTheme.typography.headlineMedium
        )
        if (defaultConfig != null) {
            Text(
                text = "Default Rocket Config: ${defaultConfig!!.name}",
                style = MaterialTheme.typography.bodyLarge
            )
        } else {
            Text(
                text = "No default Rocket Config available.",
                style = MaterialTheme.typography.bodyLarge
            )
        }
    }
}