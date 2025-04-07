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
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import no.uio.ifi.in2000.met2025.data.local.Database.ConfigProfile

@Composable
fun ConfigEditScreen(
    config: ConfigProfile? = null, // null means new; otherwise editing
    viewModel: ConfigEditViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit
) {
    var configName by remember { mutableStateOf(config?.name ?: "") }
    var groundWindThreshold by remember { mutableStateOf(config?.groundWindThreshold?.toString() ?: "8.6") }
    var airWindThreshold by remember { mutableStateOf(config?.airWindThreshold?.toString() ?: "17.2") }

    // Overall cloud cover
    var cloudCoverThreshold by remember { mutableStateOf(config?.cloudCoverThreshold?.toString() ?: "15.0") }
    var isEnabledCloudCover by remember { mutableStateOf(config?.isEnabledCloudCover ?: true) }

    // Individual cloud cover thresholds and switches
    var cloudCoverHighThreshold by remember { mutableStateOf(config?.cloudCoverHighThreshold?.toString() ?: "15.0") }
    var isEnabledCloudCoverHigh by remember { mutableStateOf(config?.isEnabledCloudCoverHigh ?: true) }

    var cloudCoverMediumThreshold by remember { mutableStateOf(config?.cloudCoverMediumThreshold?.toString() ?: "15.0") }
    var isEnabledCloudCoverMedium by remember { mutableStateOf(config?.isEnabledCloudCoverMedium ?: true) }

    var cloudCoverLowThreshold by remember { mutableStateOf(config?.cloudCoverLowThreshold?.toString() ?: "15.0") }
    var isEnabledCloudCoverLow by remember { mutableStateOf(config?.isEnabledCloudCoverLow ?: true) }

    var humidityThreshold by remember { mutableStateOf(config?.humidityThreshold?.toString() ?: "75.0") }
    var dewPointThreshold by remember { mutableStateOf(config?.dewPointThreshold?.toString() ?: "15.0") }

    var isEnabledGroundWind by remember { mutableStateOf(config?.isEnabledGroundWind ?: true) }
    var isEnabledAirWind by remember { mutableStateOf(config?.isEnabledAirWind ?: true) }
    var isEnabledHumidity by remember { mutableStateOf(config?.isEnabledHumidity ?: true) }
    var isEnabledDewPoint by remember { mutableStateOf(config?.isEnabledDewPoint ?: true) }

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
        // Overall Cloud Cover
        OutlinedTextField(
            value = cloudCoverThreshold,
            onValueChange = { cloudCoverThreshold = it },
            label = { Text("Overall Cloud Cover Threshold") }
        )
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Enable Overall Cloud Cover")
            Switch(checked = isEnabledCloudCover, onCheckedChange = { isEnabledCloudCover = it })
        }
        // Individual Cloud Cover High
        OutlinedTextField(
            value = cloudCoverHighThreshold,
            onValueChange = { cloudCoverHighThreshold = it },
            label = { Text("Cloud Cover High Threshold") }
        )
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Enable Cloud Cover High")
            Switch(checked = isEnabledCloudCoverHigh, onCheckedChange = { isEnabledCloudCoverHigh = it })
        }
        // Individual Cloud Cover Medium
        OutlinedTextField(
            value = cloudCoverMediumThreshold,
            onValueChange = { cloudCoverMediumThreshold = it },
            label = { Text("Cloud Cover Medium Threshold") }
        )
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Enable Cloud Cover Medium")
            Switch(checked = isEnabledCloudCoverMedium, onCheckedChange = { isEnabledCloudCoverMedium = it })
        }
        // Individual Cloud Cover Low
        OutlinedTextField(
            value = cloudCoverLowThreshold,
            onValueChange = { cloudCoverLowThreshold = it },
            label = { Text("Cloud Cover Low Threshold") }
        )
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Enable Cloud Cover Low")
            Switch(checked = isEnabledCloudCoverLow, onCheckedChange = { isEnabledCloudCoverLow = it })
        }
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
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Enable Ground Wind")
            Switch(checked = isEnabledGroundWind, onCheckedChange = { isEnabledGroundWind = it })
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Enable Air Wind")
            Switch(checked = isEnabledAirWind, onCheckedChange = { isEnabledAirWind = it })
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Enable Humidity")
            Switch(checked = isEnabledHumidity, onCheckedChange = { isEnabledHumidity = it })
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Enable Dew Point")
            Switch(checked = isEnabledDewPoint, onCheckedChange = { isEnabledDewPoint = it })
        }
        Spacer(modifier = Modifier.padding(8.dp))
        Button(
            onClick = {
                val updatedConfig = ConfigProfile(
                    id = config?.id ?: 0,
                    name = configName,
                    groundWindThreshold = groundWindThreshold.toDoubleOrNull() ?: 8.6,
                    airWindThreshold = airWindThreshold.toDoubleOrNull() ?: 17.2,
                    cloudCoverThreshold = cloudCoverThreshold.toDoubleOrNull() ?: 15.0,
                    cloudCoverHighThreshold = cloudCoverHighThreshold.toDoubleOrNull() ?: 15.0,
                    cloudCoverMediumThreshold = cloudCoverMediumThreshold.toDoubleOrNull() ?: 15.0,
                    cloudCoverLowThreshold = cloudCoverLowThreshold.toDoubleOrNull() ?: 15.0,
                    humidityThreshold = humidityThreshold.toDoubleOrNull() ?: 75.0,
                    dewPointThreshold = dewPointThreshold.toDoubleOrNull() ?: 15.0,
                    isEnabledGroundWind = isEnabledGroundWind,
                    isEnabledAirWind = isEnabledAirWind,
                    isEnabledCloudCover = isEnabledCloudCover,
                    isEnabledCloudCoverHigh = isEnabledCloudCoverHigh,
                    isEnabledCloudCoverMedium = isEnabledCloudCoverMedium,
                    isEnabledCloudCoverLow = isEnabledCloudCoverLow,
                    isEnabledHumidity = isEnabledHumidity,
                    isEnabledDewPoint = isEnabledDewPoint,
                    isDefault = config?.isDefault ?: false
                )
                if (config == null) {
                    viewModel.saveConfig(updatedConfig)
                } else {
                    viewModel.updateConfig(updatedConfig)
                }
                onNavigateBack()
            },
            modifier = Modifier.padding(top = 16.dp)
        ) {
            Text("Save Configuration")
        }
    }
}
