package no.uio.ifi.in2000.met2025.ui.configprofiles

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import no.uio.ifi.in2000.met2025.data.local.database.ConfigProfile
import no.uio.ifi.in2000.met2025.ui.common.AppOutlinedTextField
import no.uio.ifi.in2000.met2025.ui.common.ColoredSwitch
import no.uio.ifi.in2000.met2025.ui.configprofiles.common.ScreenContainer
import no.uio.ifi.in2000.met2025.ui.configprofiles.common.SectionCard
import no.uio.ifi.in2000.met2025.ui.configprofiles.common.SettingItem
import no.uio.ifi.in2000.met2025.ui.configprofiles.common.SettingRow
import no.uio.ifi.in2000.met2025.ui.screens.settings.SettingsViewModel
import no.uio.ifi.in2000.met2025.ui.theme.WarmOrange

@Composable
fun ConfigEditScreen(
    config: ConfigProfile? = null,
    viewModel: SettingsViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit
) {
    val updateStatus by viewModel.updateStatus.collectAsState()

    var configName               by remember(config) { mutableStateOf(config?.name ?: "") }
    var groundWind               by remember(config) { mutableStateOf(config?.groundWindThreshold?.toString() ?: "8.6") }
    var isEnabledGroundWind      by remember(config) { mutableStateOf(config?.isEnabledGroundWind == true) }
    var airWind                  by remember(config) { mutableStateOf(config?.airWindThreshold?.toString() ?: "17.2") }
    var isEnabledAirWind         by remember(config) { mutableStateOf(config?.isEnabledAirWind == true) }
    var windShear                by remember(config) { mutableStateOf(config?.windShearSpeedThreshold?.toString() ?: "24.5") }
    var isEnabledWindShear       by remember(config) { mutableStateOf(config?.isEnabledWindShear == true) }
    var isEnabledWindDirection   by remember(config) { mutableStateOf(config?.isEnabledWindDirection == true) }
    var overallCloud             by remember(config) { mutableStateOf(config?.cloudCoverThreshold?.toString() ?: "15.0") }
    var isEnabledOverallCloud    by remember(config) { mutableStateOf(config?.isEnabledCloudCover == true) }
    var highCloud                by remember(config) { mutableStateOf(config?.cloudCoverHighThreshold?.toString() ?: "15.0") }
    var isEnabledHighCloud       by remember(config) { mutableStateOf(config?.isEnabledCloudCoverHigh == true) }
    var medCloud                 by remember(config) { mutableStateOf(config?.cloudCoverMediumThreshold?.toString() ?: "15.0") }
    var isEnabledMedCloud        by remember(config) { mutableStateOf(config?.isEnabledCloudCoverMedium == true) }
    var lowCloud                 by remember(config) { mutableStateOf(config?.cloudCoverLowThreshold?.toString() ?: "15.0") }
    var isEnabledLowCloud        by remember(config) { mutableStateOf(config?.isEnabledCloudCoverLow == true) }
    var fog                      by remember(config) { mutableStateOf(config?.fogThreshold?.toString() ?: "0.0") }
    var isEnabledFog             by remember(config) { mutableStateOf(config?.isEnabledFog == true) }
    var precip                   by remember(config) { mutableStateOf(config?.precipitationThreshold?.toString() ?: "0.0") }
    var isEnabledPrecip          by remember(config) { mutableStateOf(config?.isEnabledPrecipitation == true) }
    var humidity                 by remember(config) { mutableStateOf(config?.humidityThreshold?.toString() ?: "75.0") }
    var isEnabledHumidity        by remember(config) { mutableStateOf(config?.isEnabledHumidity == true) }
    var dewPoint                 by remember(config) { mutableStateOf(config?.dewPointThreshold?.toString() ?: "15.0") }
    var isEnabledDewPoint        by remember(config) { mutableStateOf(config?.isEnabledDewPoint == true) }
    var thunder                  by remember(config) { mutableStateOf(config?.probabilityOfThunderThreshold?.toString() ?: "0.0") }
    var isEnabledThunder         by remember(config) { mutableStateOf(config?.isEnabledProbabilityOfThunder == true) }
    var altitude                 by remember(config) { mutableStateOf(config?.altitudeUpperBound?.toString() ?: "5000.0") }
    var isEnabledAltitude        by remember(config) { mutableStateOf(config?.isEnabledAltitudeUpperBound == true) }

    val windSettings = listOf(
        SettingItem("Ground Wind Threshold", groundWind, { groundWind = it }, isEnabledGroundWind) { isEnabledGroundWind = it },
        SettingItem("Air Wind Threshold",    airWind,    { airWind    = it }, isEnabledAirWind)    { isEnabledAirWind    = it },
        SettingItem("Wind Shear Threshold",  windShear,  { windShear  = it }, isEnabledWindShear)  { isEnabledWindShear  = it },
    )

    val cloudSettings = listOf(
        SettingItem("Overall Cloud Cover",    overallCloud, { overallCloud    = it }, isEnabledOverallCloud)    { isEnabledOverallCloud    = it },
        SettingItem("High Cloud Cover",       highCloud,    { highCloud       = it }, isEnabledHighCloud)       { isEnabledHighCloud       = it },
        SettingItem("Medium Cloud Cover",     medCloud,     { medCloud        = it }, isEnabledMedCloud)        { isEnabledMedCloud        = it },
        SettingItem("Low Cloud Cover",        lowCloud,     { lowCloud        = it }, isEnabledLowCloud)        { isEnabledLowCloud        = it },
    )

    val weatherSettings = listOf(
        SettingItem("Fog Threshold",              fog,     { fog     = it }, isEnabledFog)     { isEnabledFog     = it },
        SettingItem("Precipitation Threshold",    precip,  { precip  = it }, isEnabledPrecip)  { isEnabledPrecip  = it },
        SettingItem("Humidity Threshold",         humidity,{ humidity = it }, isEnabledHumidity){ isEnabledHumidity = it },
        SettingItem("Dew Point Threshold",        dewPoint,{ dewPoint = it }, isEnabledDewPoint){ isEnabledDewPoint = it },
        SettingItem("Thunder Probability",        thunder, { thunder  = it }, isEnabledThunder){ isEnabledThunder  = it },
    )

    LaunchedEffect(configName) {
        viewModel.checkWeatherNameAvailability(configName)
    }

    ScreenContainer(title = if (config == null) "New Configuration" else "Edit Configuration") {
        val isNameError = updateStatus is SettingsViewModel.UpdateStatus.Error &&
                configName != config?.name

        // Name
        SectionCard("Configuration Name", Modifier.fillMaxWidth()) {
            AppOutlinedTextField(
                value         = configName,
                onValueChange = {
                    configName = it
                    viewModel.checkWeatherNameAvailability(it)
                },
                label    = { Text("Name") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(2.dp))
            if (isNameError) {
                Text(
                    (updateStatus as SettingsViewModel.UpdateStatus.Error).message,
                    color = Color.Red
                )
            }
        }
        Spacer(Modifier.height(16.dp))

        // Wind Settings
        SectionCard("Wind Settings", Modifier.fillMaxWidth()) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Text("Wind Direction", Modifier.weight(1f))
                ColoredSwitch(
                    checked = if (config == null) true else isEnabledWindDirection,
                    onCheckedChange = { isEnabledWindDirection = it },
                )
            }
            Spacer(Modifier.height(8.dp))
            windSettings.forEach { item ->
                SettingRow(
                    label           = item.label,
                    value           = item.value,
                    onValueChange   = item.onValueChange,
                    enabled         = if (config == null) true else item.enabled,
                    onEnabledChange = item.onEnabledChange,
                    modifier        = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(8.dp))
            }
        }
        Spacer(Modifier.height(16.dp))

        // Cloud Cover Settings
        SectionCard("Cloud Cover Settings", Modifier.fillMaxWidth()) {
            cloudSettings.forEach { item ->
                SettingRow(
                    label           = item.label,
                    value           = item.value,
                    onValueChange   = item.onValueChange,
                    enabled         = if (config == null) true else item.enabled,
                    onEnabledChange = item.onEnabledChange,
                    modifier        = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(8.dp))
            }
        }
        Spacer(Modifier.height(16.dp))

        // Water & Weather Settings
        SectionCard("Water & Weather Settings", Modifier.fillMaxWidth()) {
            weatherSettings.forEach { item ->
                SettingRow(
                    label           = item.label,
                    value           = item.value,
                    onValueChange   = item.onValueChange,
                    enabled         = if (config == null) true else item.enabled,
                    onEnabledChange = item.onEnabledChange,
                    modifier        = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(8.dp))
            }
        }
        Spacer(Modifier.height(16.dp))

        // Altitude Settings
        SectionCard("Altitude Settings", Modifier.fillMaxWidth()) {
            SettingRow(
                label           = "Upper Bound (m)",
                value           = altitude,
                onValueChange   = { altitude = it },
                enabled         = if (config == null) true else isEnabledAltitude,
                onEnabledChange = { isEnabledAltitude = it },
                modifier        = Modifier.fillMaxWidth()
            )
        }
        Spacer(Modifier.height(24.dp))

        // Save button
        Button(
            onClick = {
                val updated = ConfigProfile(
                    id                             = config?.id ?: 0,
                    name                           = configName,
                    groundWindThreshold            = groundWind.toDoubleOrNull()                ?: 8.6,
                    airWindThreshold               = airWind.toDoubleOrNull()                  ?: 17.2,
                    cloudCoverThreshold            = overallCloud.toDoubleOrNull()             ?: 15.0,
                    cloudCoverHighThreshold        = highCloud.toDoubleOrNull()                ?: 15.0,
                    cloudCoverMediumThreshold      = medCloud.toDoubleOrNull()                 ?: 15.0,
                    cloudCoverLowThreshold         = lowCloud.toDoubleOrNull()                 ?: 15.0,
                    humidityThreshold              = humidity.toDoubleOrNull()                 ?: 75.0,
                    dewPointThreshold              = dewPoint.toDoubleOrNull()                 ?: 15.0,
                    isEnabledGroundWind            = isEnabledGroundWind,
                    isEnabledAirWind               = isEnabledAirWind,
                    isEnabledCloudCover            = isEnabledOverallCloud,
                    isEnabledCloudCoverHigh        = isEnabledHighCloud,
                    isEnabledCloudCoverMedium      = isEnabledMedCloud,
                    isEnabledCloudCoverLow         = isEnabledLowCloud,
                    isEnabledHumidity              = isEnabledHumidity,
                    isEnabledDewPoint              = isEnabledDewPoint,
                    isEnabledWindDirection         = isEnabledWindDirection,
                    isEnabledFog                   = isEnabledFog,
                    fogThreshold                   = fog.toDoubleOrNull()                     ?: 0.0,
                    isEnabledPrecipitation         = isEnabledPrecip,
                    precipitationThreshold         = precip.toDoubleOrNull()                  ?: 0.0,
                    isEnabledProbabilityOfThunder  = isEnabledThunder,
                    probabilityOfThunderThreshold  = thunder.toDoubleOrNull()                  ?: 0.0,
                    isEnabledAltitudeUpperBound    = isEnabledAltitude,
                    altitudeUpperBound             = altitude.toDoubleOrNull()                 ?: 5000.0,
                    isEnabledWindShear             = isEnabledWindShear,
                    windShearSpeedThreshold        = windShear.toDoubleOrNull()                ?: 24.5,
                    isDefault                      = config?.isDefault == true
                )
                if (config == null) {
                    viewModel.saveWeatherConfig(updated)
                } else {
                    viewModel.updateWeatherConfig(updated)
                }
            },
            modifier = Modifier.fillMaxWidth(),
            colors   = ButtonDefaults.buttonColors(
                containerColor = WarmOrange,
                contentColor   = MaterialTheme.colorScheme.onPrimary
            )
        ) {
            Text("Save Configuration")
        }
        if (updateStatus is SettingsViewModel.UpdateStatus.Error) {
            Text(
                (updateStatus as SettingsViewModel.UpdateStatus.Error).message,
                color = Color.Red
            )
        }
    }
    LaunchedEffect(updateStatus) {
        if (updateStatus is SettingsViewModel.UpdateStatus.Success) {
            viewModel.resetWeatherStatus()
            onNavigateBack()
        }
    }
}
