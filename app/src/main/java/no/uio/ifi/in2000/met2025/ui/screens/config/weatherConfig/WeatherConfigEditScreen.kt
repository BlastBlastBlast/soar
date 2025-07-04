package no.uio.ifi.in2000.met2025.ui.screens.config.weatherConfig

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
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import no.uio.ifi.in2000.met2025.data.local.database.WeatherConfig
import no.uio.ifi.in2000.met2025.ui.common.AppOutlinedTextField
import no.uio.ifi.in2000.met2025.ui.common.ColoredSwitch
import no.uio.ifi.in2000.met2025.ui.screens.config.weatherConfig.common.ScreenContainer
import no.uio.ifi.in2000.met2025.ui.screens.config.weatherConfig.common.SectionCard
import no.uio.ifi.in2000.met2025.ui.screens.config.weatherConfig.common.SettingItem
import no.uio.ifi.in2000.met2025.ui.screens.config.weatherConfig.common.SettingRow
import no.uio.ifi.in2000.met2025.ui.screens.config.ConfigViewModel
import no.uio.ifi.in2000.met2025.ui.theme.WarmOrange

/**
 * WeatherConfigEditScreen
 *
 * UI for creating or editing a WeatherConfig profile.
 * - Organizes settings into sections (name, wind, cloud, etc.).
 * - Validates unique config name and non-empty input.
 * - Saves or updates via ConfigViewModel.
 */
@Composable
fun WeatherConfigEditScreen(
    weatherConfig: WeatherConfig? = null,
    viewModel: ConfigViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit
) {
    val updateStatus by viewModel.updateStatus.collectAsState()
    val weatherNames by viewModel.weatherNames.collectAsState()

    // Local state for all config fields, defaulting to the values of the default config
    var configName               by remember(weatherConfig) { mutableStateOf(weatherConfig?.name ?: "") }
    var groundWind               by remember(weatherConfig) { mutableStateOf(weatherConfig?.groundWindThreshold?.toString() ?: "8.6") }
    var isEnabledGroundWind      by remember(weatherConfig) { mutableStateOf(weatherConfig?.isEnabledGroundWind ?: true) }
    var airWind                  by remember(weatherConfig) { mutableStateOf(weatherConfig?.airWindThreshold?.toString() ?: "17.2") }
    var isEnabledAirWind         by remember(weatherConfig) { mutableStateOf(weatherConfig?.isEnabledAirWind ?: true) }
    var windShear                by remember(weatherConfig) { mutableStateOf(weatherConfig?.windShearSpeedThreshold?.toString() ?: "24.5") }
    var isEnabledWindShear       by remember(weatherConfig) { mutableStateOf(weatherConfig?.isEnabledWindShear ?: true) }
    var isEnabledWindDirection   by remember(weatherConfig) { mutableStateOf(weatherConfig?.isEnabledWindDirection ?: true) }
    var overallCloud             by remember(weatherConfig) { mutableStateOf(weatherConfig?.cloudCoverThreshold?.toString() ?: "15.0") }
    var isEnabledOverallCloud    by remember(weatherConfig) { mutableStateOf(weatherConfig?.isEnabledCloudCover ?: true) }
    var highCloud                by remember(weatherConfig) { mutableStateOf(weatherConfig?.cloudCoverHighThreshold?.toString() ?: "15.0") }
    var isEnabledHighCloud       by remember(weatherConfig) { mutableStateOf(weatherConfig?.isEnabledCloudCoverHigh ?: true) }
    var medCloud                 by remember(weatherConfig) { mutableStateOf(weatherConfig?.cloudCoverMediumThreshold?.toString() ?: "15.0") }
    var isEnabledMedCloud        by remember(weatherConfig) { mutableStateOf(weatherConfig?.isEnabledCloudCoverMedium ?: true) }
    var lowCloud                 by remember(weatherConfig) { mutableStateOf(weatherConfig?.cloudCoverLowThreshold?.toString() ?: "15.0") }
    var isEnabledLowCloud        by remember(weatherConfig) { mutableStateOf(weatherConfig?.isEnabledCloudCoverLow ?: true) }
    var fog                      by remember(weatherConfig) { mutableStateOf(weatherConfig?.fogThreshold?.toString() ?: "0.0") }
    var isEnabledFog             by remember(weatherConfig) { mutableStateOf(weatherConfig?.isEnabledFog ?: true) }
    var precipitation            by remember(weatherConfig) { mutableStateOf(weatherConfig?.precipitationThreshold?.toString() ?: "0.0") }
    var isEnabledPrecipitation   by remember(weatherConfig) { mutableStateOf(weatherConfig?.isEnabledPrecipitation ?: true) }
    var humidity                 by remember(weatherConfig) { mutableStateOf(weatherConfig?.humidityThreshold?.toString() ?: "75.0") }
    var isEnabledHumidity        by remember(weatherConfig) { mutableStateOf(weatherConfig?.isEnabledHumidity ?: true) }
    var dewPoint                 by remember(weatherConfig) { mutableStateOf(weatherConfig?.dewPointThreshold?.toString() ?: "15.0") }
    var isEnabledDewPoint        by remember(weatherConfig) { mutableStateOf(weatherConfig?.isEnabledDewPoint ?: true) }
    var thunder                  by remember(weatherConfig) { mutableStateOf(weatherConfig?.probabilityOfThunderThreshold?.toString() ?: "0.0") }
    var isEnabledThunder         by remember(weatherConfig) { mutableStateOf(weatherConfig?.isEnabledProbabilityOfThunder ?: true) }
    var altitude                 by remember(weatherConfig) { mutableStateOf(weatherConfig?.altitudeUpperBound?.toString() ?: "5000.0") }
    var isEnabledAltitude        by remember(weatherConfig) { mutableStateOf(weatherConfig?.isEnabledAltitudeUpperBound ?: true) }

    // Group settings into lists for reuse
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
        SettingItem("Precipitation Threshold",    precipitation,  { precipitation  = it }, isEnabledPrecipitation)  { isEnabledPrecipitation  = it },
        SettingItem("Humidity Threshold",         humidity,{ humidity = it }, isEnabledHumidity){ isEnabledHumidity = it },
        SettingItem("Dew Point Threshold",        dewPoint,{ dewPoint = it }, isEnabledDewPoint){ isEnabledDewPoint = it },
        SettingItem("Thunder Probability",        thunder, { thunder  = it }, isEnabledThunder){ isEnabledThunder  = it },
    )

    ScreenContainer(title = if (weatherConfig == null) "NEW WEATHER CONFIG" else "EDIT WEATHER CONFIG") {
        val isNameError = configName in weatherNames && configName != weatherConfig?.name

        // Name section with duplicate-name validation
        SectionCard("Configuration Name", Modifier.fillMaxWidth()) {
            AppOutlinedTextField(
                value         = configName,
                onValueChange = {
                    configName = it
                },
                labelText    = "Name",
                modifier = Modifier.fillMaxWidth(),
                //Regex limits names to 14 characters
                filterRegex = Regex("^.{0,14}\$")
            )
            Spacer(Modifier.height(2.dp))
            if (isNameError) {
                Text(
                    text = "A config named \"$configName\" already exists",
                    color = Color.Red
                )
            } else if (configName.length == 14) {
                Text(
                    text = "Name length limit reached: 14 characters",
                    color = MaterialTheme.colorScheme.onPrimary
                )
            }
        }
        Spacer(Modifier.height(16.dp))

        // Wind Settings
        SectionCard("Wind Settings", Modifier.fillMaxWidth()) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Text("Wind Direction", Modifier.weight(1f))
                ColoredSwitch(
                    checked =  isEnabledWindDirection,
                    onCheckedChange = { isEnabledWindDirection = it },
                )
            }
            Spacer(Modifier.height(8.dp))
            windSettings.forEach { item ->
                SettingRow(
                    label           = item.label,
                    value           = item.value,
                    onValueChange   = item.onValueChange,
                    enabled         =  item.enabled,
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
                    enabled         =  item.enabled,
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
                    enabled         =  item.enabled,
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
                enabled         =  isEnabledAltitude,
                onEnabledChange = { isEnabledAltitude = it },
                modifier        = Modifier.fillMaxWidth()
            )
        }
        Spacer(Modifier.height(24.dp))

        // Save button, enabled only when name is valid
        Button(
            onClick = {
                val updated = WeatherConfig(
                    id                             = weatherConfig?.id ?: 0,
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
                    isEnabledPrecipitation         = isEnabledPrecipitation,
                    precipitationThreshold         = precipitation.toDoubleOrNull()                  ?: 0.0,
                    isEnabledProbabilityOfThunder  = isEnabledThunder,
                    probabilityOfThunderThreshold  = thunder.toDoubleOrNull()                  ?: 0.0,
                    isEnabledAltitudeUpperBound    = isEnabledAltitude,
                    altitudeUpperBound             = altitude.toDoubleOrNull()                 ?: 5000.0,
                    isEnabledWindShear             = isEnabledWindShear,
                    windShearSpeedThreshold        = windShear.toDoubleOrNull()                ?: 24.5,
                    isDefault                      = weatherConfig?.isDefault == true
                )
                if (weatherConfig == null) {
                    viewModel.saveWeatherConfig(updated)
                } else {
                    viewModel.updateWeatherConfig(updated)
                }
            },
            enabled = !isNameError && configName.isNotBlank(),
            modifier = Modifier.fillMaxWidth()
                .semantics {
                    role = Role.Button
                    contentDescription = "Save configuration"
                },
            colors   = ButtonDefaults.buttonColors(
                containerColor = WarmOrange,
                contentColor   = MaterialTheme.colorScheme.onPrimary
            )
        ) {
            Text("Save Configuration")
        }
        if (isNameError) {
            Text(
                text = "A config named \"$configName\" already exists",
                color = Color.Red,
                modifier = Modifier.semantics {
                    liveRegion = LiveRegionMode.Polite
                    contentDescription = "A config named $configName already exists"
                }
            )
        }
        if (configName.isBlank()) {
            Text(
                text = "Configuration Name field must not be empty",
                color = Color.Red,
                modifier = Modifier.semantics {
                    liveRegion = LiveRegionMode.Polite
                    contentDescription = "Configuration Name field must not be empty"
                }
            )
        }
    }

    //Navigate back when save succeeds
    LaunchedEffect(updateStatus) {
        if (updateStatus is ConfigViewModel.UpdateStatus.Success) {
            viewModel.resetWeatherStatus()
            onNavigateBack()
        }
    }
}
