package no.uio.ifi.in2000.met2025.ui.configprofiles

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.IconButton
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import no.uio.ifi.in2000.met2025.data.local.Database.ConfigProfile
import androidx.compose.ui.res.painterResource
import no.uio.ifi.in2000.met2025.R

@Composable
fun ConfigEditScreen(
    viewModel: ConfigEditViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit
) {
    // Form state for our new config (non-default)
    var configName by remember { mutableStateOf("") }
    var groundWindThreshold by remember { mutableStateOf("8.6") }
    var airWindThreshold by remember { mutableStateOf("17.2") }
    var cloudCoverThreshold by remember { mutableStateOf("15.0") }
    var humidityThreshold by remember { mutableStateOf("75.0") }
    var dewPointThreshold by remember { mutableStateOf("15.0") }
    var isEnabledGroundWind by remember { mutableStateOf(true) }
    var isEnabledAirWind by remember { mutableStateOf(true) }
    var isEnabledCloudCover by remember { mutableStateOf(true) }
    var isEnabledHumidity by remember { mutableStateOf(true) }
    var isEnabledDewPoint by remember { mutableStateOf(true) }


    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        OutlinedTextField(
            value = configName,
            onValueChange = { configName = it },
            label = { Text("Configuration Name") }
        )
        OutlinedTextField(
            value = groundWindThreshold,
            onValueChange = { groundWindThreshold = it },
            label = { Text("Ground Wind Threshold") }
        )
        OutlinedTextField(
            value = airWindThreshold,
            onValueChange = { airWindThreshold = it },
            label = { Text("Air Wind Threshold") }
        )
        OutlinedTextField(
            value = cloudCoverThreshold,
            onValueChange = { cloudCoverThreshold = it },
            label = { Text("Cloud Cover Threshold") }
        )
        OutlinedTextField(
            value = humidityThreshold,
            onValueChange = { humidityThreshold = it },
            label = { Text("Humidity Threshold") }
        )
        OutlinedTextField(
            value = dewPointThreshold,
            onValueChange = { dewPointThreshold = it },
            label = { Text("Dew Point Threshold") }
        )
        // Switches for each parameter.
        Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
            Text("Enable Ground Wind")
            Switch(checked = isEnabledGroundWind, onCheckedChange = { isEnabledGroundWind = it })
        }
        Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
            Text("Enable Air Wind")
            Switch(checked = isEnabledAirWind, onCheckedChange = { isEnabledAirWind = it })
        }
        Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
            Text("Enable Cloud Cover")
            Switch(checked = isEnabledCloudCover, onCheckedChange = { isEnabledCloudCover = it })
        }
        Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
            Text("Enable Humidity")
            Switch(checked = isEnabledHumidity, onCheckedChange = { isEnabledHumidity = it })
        }
        Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
            Text("Enable Dew Point")
            Switch(checked = isEnabledDewPoint, onCheckedChange = { isEnabledDewPoint = it })
        }
        Spacer(modifier = Modifier.padding(8.dp))
        Button(
            onClick = {
                // Create a new configuration from form values.
                viewModel.saveConfig(
                    ConfigProfile(
                        name = configName,
                        groundWindThreshold = groundWindThreshold.toDoubleOrNull() ?: 8.6,
                        airWindThreshold = airWindThreshold.toDoubleOrNull() ?: 17.2,
                        cloudCoverThreshold = cloudCoverThreshold.toDoubleOrNull() ?: 15.0,
                        humidityThreshold = humidityThreshold.toDoubleOrNull() ?: 75.0,
                        dewPointThreshold = dewPointThreshold.toDoubleOrNull() ?: 15.0,
                        isEnabledGroundWind = isEnabledGroundWind,
                        isEnabledAirWind = isEnabledAirWind,
                        isEnabledCloudCover = isEnabledCloudCover,
                        isEnabledHumidity = isEnabledHumidity,
                        isEnabledDewPoint = isEnabledDewPoint,
                        isDefault = false  // New user-defined configs are non-default.
                    )
                )
                onNavigateBack()
            },
            modifier = Modifier.padding(top = 16.dp)
        ) {
            Text("Save Configuration")
        }
    }
}

