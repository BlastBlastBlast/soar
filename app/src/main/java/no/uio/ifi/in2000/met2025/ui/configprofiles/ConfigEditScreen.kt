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
import no.uio.ifi.in2000.met2025.ui.AppOutlinedTextField

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
    var isEnabledCloudCover by remember(config) { mutableStateOf(config?.isEnabledCloudCover != false) }
    var cloudCoverHighThreshold by remember(config) { mutableStateOf(config?.cloudCoverHighThreshold?.toString() ?: "15.0") }
    var isEnabledCloudCoverHigh by remember(config) { mutableStateOf(config?.isEnabledCloudCoverHigh != false) }
    var cloudCoverMediumThreshold by remember(config) { mutableStateOf(config?.cloudCoverMediumThreshold?.toString() ?: "15.0") }
    var isEnabledCloudCoverMedium by remember(config) { mutableStateOf(config?.isEnabledCloudCoverMedium != false) }
    var cloudCoverLowThreshold by remember(config) { mutableStateOf(config?.cloudCoverLowThreshold?.toString() ?: "15.0") }
    var isEnabledCloudCoverLow by remember(config) { mutableStateOf(config?.isEnabledCloudCoverLow != false) }
    var humidityThreshold by remember(config) { mutableStateOf(config?.humidityThreshold?.toString() ?: "75.0") }
    var dewPointThreshold by remember(config) { mutableStateOf(config?.dewPointThreshold?.toString() ?: "15.0") }
    var isEnabledGroundWind by remember(config) { mutableStateOf(config?.isEnabledGroundWind != false) }
    var isEnabledAirWind by remember(config) { mutableStateOf(config?.isEnabledAirWind != false) }
    var isEnabledHumidity by remember(config) { mutableStateOf(config?.isEnabledHumidity != false) }
    var isEnabledDewPoint by remember(config) { mutableStateOf(config?.isEnabledDewPoint != false) }
    var isEnabledWindDirection by remember(config) { mutableStateOf(config?.isEnabledWindDirection != false) }
    var isEnabledFog by remember(config) { mutableStateOf(config?.isEnabledFog != false) }
    var isEnabledPrecipitation by remember(config) { mutableStateOf(config?.isEnabledPrecipitation != false) }
    var isEnabledProbabilityOfThunder by remember(config) { mutableStateOf(config?.isEnabledProbabilityOfThunder != false) }
    var fogThreshold by remember(config) { mutableStateOf(config?.fogThreshold?.toString() ?: "0.0") }
    var precipitationThreshold by remember(config) { mutableStateOf(config?.precipitationThreshold?.toString() ?: "0.0") }
    var probabilityOfThunderThreshold by remember(config) { mutableStateOf(config?.probabilityOfThunderThreshold?.toString() ?: "0.0") }
    var altitudeUpperBound by remember(config) { mutableStateOf(config?.altitudeUpperBound?.toString() ?: "5000.0") }
    var isEnabledAltitudeUpperBound by remember(config) { mutableStateOf(config?.isEnabledAltitudeUpperBound != false) }
    var shearWindSpeedThreshold by remember(config) { mutableStateOf(config?.windShearSpeedThreshold?.toString() ?: "24.5") }
    var isEnabledWindShear by remember(config) { mutableStateOf(config?.isEnabledWindShear != false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Card(modifier = Modifier.fillMaxWidth(), elevation = CardDefaults.cardElevation(8.dp)) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Configuration Name", style = MaterialTheme.typography.titleMedium)
                AppOutlinedTextField(
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
                    AppOutlinedTextField(
                        value = groundWindThreshold,
                        onValueChange = { groundWindThreshold = it },
                        label = { Text("Ground Wind Threshold") },
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Switch(checked = isEnabledGroundWind, onCheckedChange = { isEnabledGroundWind = it })
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    AppOutlinedTextField(
                        value = airWindThreshold,
                        onValueChange = { airWindThreshold = it },
                        label = { Text("Air Wind Threshold") },
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Switch(checked = isEnabledAirWind, onCheckedChange = { isEnabledAirWind = it })
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    AppOutlinedTextField(
                        value = shearWindSpeedThreshold,
                        onValueChange = { shearWindSpeedThreshold = it },
                        label = { Text("Wind Shear Threshold") },
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Switch(checked = isEnabledWindShear, onCheckedChange = { isEnabledWindShear = it })
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
                        AppOutlinedTextField(
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
                        AppOutlinedTextField(
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

        Card(modifier = Modifier.fillMaxWidth(), elevation = CardDefaults.cardElevation(8.dp)) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Altitude Settings", style = MaterialTheme.typography.titleMedium)

                Row(verticalAlignment = Alignment.CenterVertically) {
                    AppOutlinedTextField(
                        value = altitudeUpperBound,
                        onValueChange = { altitudeUpperBound = it },
                        label = { Text("Altitude Upper Bound") },
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Switch(checked = isEnabledAltitudeUpperBound, onCheckedChange = { isEnabledAltitudeUpperBound = it })
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
                isEnabledAltitudeUpperBound = isEnabledAltitudeUpperBound,
                altitudeUpperBound = altitudeUpperBound.toDoubleOrNull() ?: 5000.0,
                isEnabledWindShear = isEnabledWindShear,
                windShearSpeedThreshold = shearWindSpeedThreshold.toDoubleOrNull() ?: 24.5,
                isDefault = config?.isDefault == true
            )
            if (config == null) viewModel.saveConfig(updatedConfig)
            else viewModel.updateConfig(updatedConfig)
            onNavigateBack()
        }, modifier = Modifier.fillMaxWidth()) {
            Text("Save Configuration")
        }
    }
}
