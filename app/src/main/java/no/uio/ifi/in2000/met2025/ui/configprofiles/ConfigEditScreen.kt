package no.uio.ifi.in2000.met2025.ui.configprofiles

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
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
import no.uio.ifi.in2000.met2025.data.local.database.ConfigProfile

@Composable
fun ConfigEditScreen(
    config: ConfigProfile? = null,
    viewModel: ConfigEditViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit
) {
    var configName by remember(config) { mutableStateOf(config?.name ?: "") }
    var groundWindThreshold by remember(config) { mutableStateOf(config?.groundWindThreshold?.toString() ?: "8.6") }
    var airWindThreshold by remember(config) { mutableStateOf(config?.airWindThreshold?.toString() ?: "17.2") }
    var cloudCoverThreshold by remember(config) { mutableStateOf(config?.cloudCoverThreshold?.toString() ?: "15.0") }
    var isEnabledCloudCover by remember(config) { mutableStateOf(config?.isEnabledCloudCover ?: true) }
    var cloudCoverHighThreshold by remember(config) { mutableStateOf(config?.cloudCoverHighThreshold?.toString() ?: "15.0") }
    var isEnabledCloudCoverHigh by remember(config) { mutableStateOf(config?.isEnabledCloudCoverHigh ?: true) }
    var cloudCoverMediumThreshold by remember(config) { mutableStateOf(config?.cloudCoverMediumThreshold?.toString() ?: "15.0") }
    var isEnabledCloudCoverMedium by remember(config) { mutableStateOf(config?.isEnabledCloudCoverMedium ?: true) }
    var cloudCoverLowThreshold by remember(config) { mutableStateOf(config?.cloudCoverLowThreshold?.toString() ?: "15.0") }
    var isEnabledCloudCoverLow by remember(config) { mutableStateOf(config?.isEnabledCloudCoverLow ?: true) }
    var humidityThreshold by remember(config) { mutableStateOf(config?.humidityThreshold?.toString() ?: "75.0") }
    var dewPointThreshold by remember(config) { mutableStateOf(config?.dewPointThreshold?.toString() ?: "15.0") }
    var isEnabledGroundWind by remember(config) { mutableStateOf(config?.isEnabledGroundWind ?: true) }
    var isEnabledAirWind by remember(config) { mutableStateOf(config?.isEnabledAirWind ?: true) }
    var isEnabledHumidity by remember(config) { mutableStateOf(config?.isEnabledHumidity ?: true) }
    var isEnabledDewPoint by remember(config) { mutableStateOf(config?.isEnabledDewPoint ?: true) }
    var isEnabledWindDirection by remember(config) { mutableStateOf(config?.isEnabledWindDirection ?: true) }
    var isEnabledFog by remember(config) { mutableStateOf(config?.isEnabledFog ?: true) }
    var isEnabledPrecipitation by remember(config) { mutableStateOf(config?.isEnabledPrecipitation ?: true) }
    var isEnabledProbabilityOfThunder by remember(config) { mutableStateOf(config?.isEnabledProbabilityOfThunder ?: true) }
    var fogThreshold by remember(config) { mutableStateOf(config?.fogThreshold?.toString() ?: "0.0") }
    var precipitationThreshold by remember(config) { mutableStateOf(config?.precipitationThreshold?.toString() ?: "0.0") }
    var probabilityOfThunderThreshold by remember(config) { mutableStateOf(config?.probabilityOfThunderThreshold?.toString() ?: "0.0") }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Card(modifier = Modifier.fillMaxWidth(), elevation = CardDefaults.cardElevation(8.dp)) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Configuration Name", style = MaterialTheme.typography.titleMedium)
                OutlinedTextField(
                    value = configName,
                    onValueChange = { configName = it },
                    label = { Text("Name") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
        Spacer(modifier = Modifier.height(16.dp))

        Card(modifier = Modifier.fillMaxWidth(), elevation = CardDefaults.cardElevation(8.dp)) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Wind Settings", style = MaterialTheme.typography.titleMedium)
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                    Text("Wind Direction", modifier = Modifier.weight(1f))
                    Switch(checked = isEnabledWindDirection, onCheckedChange = { isEnabledWindDirection = it })
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    OutlinedTextField(
                        value = groundWindThreshold,
                        onValueChange = { groundWindThreshold = it },
                        label = { Text("Ground Wind Threshold") },
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Switch(checked = isEnabledGroundWind, onCheckedChange = { isEnabledGroundWind = it })
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    OutlinedTextField(
                        value = airWindThreshold,
                        onValueChange = { airWindThreshold = it },
                        label = { Text("Air Wind Threshold") },
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Switch(checked = isEnabledAirWind, onCheckedChange = { isEnabledAirWind = it })
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Card(modifier = Modifier.fillMaxWidth(), elevation = CardDefaults.cardElevation(8.dp)) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Cloud Cover Settings", style = MaterialTheme.typography.titleMedium)
                listOf(
                    Triple("Overall Cloud Cover", cloudCoverThreshold, isEnabledCloudCover),
                    Triple("Cloud Cover High", cloudCoverHighThreshold, isEnabledCloudCoverHigh),
                    Triple("Cloud Cover Medium", cloudCoverMediumThreshold, isEnabledCloudCoverMedium),
                    Triple("Cloud Cover Low", cloudCoverLowThreshold, isEnabledCloudCoverLow)
                ).forEachIndexed { index, (label, threshold, enabled) ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        OutlinedTextField(
                            value = threshold,
                            onValueChange = {
                                when (index) {
                                    0 -> cloudCoverThreshold = it
                                    1 -> cloudCoverHighThreshold = it
                                    2 -> cloudCoverMediumThreshold = it
                                    3 -> cloudCoverLowThreshold = it
                                }
                            },
                            label = { Text(label) },
                            modifier = Modifier.weight(1f)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Switch(checked = enabled, onCheckedChange = {
                            when (index) {
                                0 -> isEnabledCloudCover = it
                                1 -> isEnabledCloudCoverHigh = it
                                2 -> isEnabledCloudCoverMedium = it
                                3 -> isEnabledCloudCoverLow = it
                            }
                        })
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Card(modifier = Modifier.fillMaxWidth(), elevation = CardDefaults.cardElevation(8.dp)) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Water & Weather Settings", style = MaterialTheme.typography.titleMedium)
                listOf(
                    Triple("Fog Threshold", fogThreshold, isEnabledFog),
                    Triple("Precipitation Threshold", precipitationThreshold, isEnabledPrecipitation),
                    Triple("Humidity Threshold", humidityThreshold, isEnabledHumidity),
                    Triple("Dew Point Threshold", dewPointThreshold, isEnabledDewPoint),
                    Triple("% Thunder Threshold", probabilityOfThunderThreshold, isEnabledProbabilityOfThunder)
                ).forEachIndexed { index, (label, threshold, enabled) ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        OutlinedTextField(
                            value = threshold,
                            onValueChange = {
                                when (index) {
                                    0 -> fogThreshold = it
                                    1 -> precipitationThreshold = it
                                    2 -> humidityThreshold = it
                                    3 -> dewPointThreshold = it
                                    4 -> probabilityOfThunderThreshold = it
                                }
                            },
                            label = { Text(label) },
                            modifier = Modifier.weight(1f)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Switch(checked = enabled, onCheckedChange = {
                            when (index) {
                                0 -> isEnabledFog = it
                                1 -> isEnabledPrecipitation = it
                                2 -> isEnabledHumidity = it
                                3 -> isEnabledDewPoint = it
                                4 -> isEnabledProbabilityOfThunder = it
                            }
                        })
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(onClick = {
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
                isEnabledWindDirection = isEnabledWindDirection,
                isEnabledFog = isEnabledFog,
                fogThreshold = fogThreshold.toDoubleOrNull() ?: 0.0,
                isEnabledPrecipitation = isEnabledPrecipitation,
                precipitationThreshold = precipitationThreshold.toDoubleOrNull() ?: 0.0,
                isEnabledProbabilityOfThunder = isEnabledProbabilityOfThunder,
                probabilityOfThunderThreshold = probabilityOfThunderThreshold.toDoubleOrNull() ?: 0.0,
                isDefault = config?.isDefault ?: false
            )
            if (config == null) viewModel.saveConfig(updatedConfig)
            else viewModel.updateConfig(updatedConfig)
            onNavigateBack()
        }, modifier = Modifier.fillMaxWidth()) {
            Text("Save Configuration")
        }
    }
}
