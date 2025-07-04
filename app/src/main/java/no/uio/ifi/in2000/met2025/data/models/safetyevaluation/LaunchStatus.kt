package no.uio.ifi.in2000.met2025.data.models.safetyevaluation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import no.uio.ifi.in2000.met2025.data.local.database.WeatherConfig
import no.uio.ifi.in2000.met2025.data.models.Constants.Companion.CAUTION_THRESHOLD
import no.uio.ifi.in2000.met2025.data.models.Constants.Companion.UNSAFE_THRESHOLD
import no.uio.ifi.in2000.met2025.data.models.locationforecast.ForecastDataItem
import no.uio.ifi.in2000.met2025.data.models.isobaric.IsobaricData
import no.uio.ifi.in2000.met2025.ui.theme.*

/**
 * This enum class contains the three states that available data for a weather parameter, or an aggregate of parameters can be in.
 * The states relate to how close the parameter(s) are to the threshold(s).
 * Each state has a corresponding icon.
 */
enum class LaunchStatus {
    SAFE,           // All values comfortably within spec.
    CAUTION,        // At least one value is close to threshold, the rest within spec.
    UNSAFE,         // One or more values exceed the allowed threshold.
}

/**
 * Couples a relative unsafety value to a LaunchStatus.
 */
fun launchStatus(relativeUnsafety: Double): LaunchStatus {
    return when {
        relativeUnsafety > UNSAFE_THRESHOLD -> LaunchStatus.UNSAFE
        relativeUnsafety > CAUTION_THRESHOLD -> LaunchStatus.CAUTION
        else -> LaunchStatus.SAFE
    }
}

/**
 * Draws an icon given a collection of weather parameters.
 * This can either be in the form of forecast or isobaric data, or both.
 * Different icons are drawn for missing data, disabled parameters, and the three launch statuses.
 */
@Composable
fun LaunchStatusIcon(
    weatherConfig: WeatherConfig,
    forecast: ForecastDataItem? = null,
    isobaric: IsobaricData? = null,
    modifier: Modifier
) {
    val state = evaluateConditions(weatherConfig, forecast, isobaric)
    LaunchStatusIcon(state, modifier)
}

/**
 * Draws an icon for a single parameter state.
 * Different icons are drawn for missing data, disabled parameters, and the three launch statuses.
 */
@Composable
fun LaunchStatusIcon(state: ParameterState, modifier: Modifier) {
    // detect light vs dark
    val isDark = LocalIsDarkTheme.current

    val (color, icon, description) = when (state) {
        is ParameterState.Missing ->
            Triple(IconPurple,        Icons.Filled.CloudOff,   "Data missing")

        is ParameterState.Disabled ->
            Triple(IconGrey,          Icons.Filled.Close,      "Turned Off")

        is ParameterState.Available -> {
            when (launchStatus(state.relativeUnsafety)) {
                LaunchStatus.SAFE   -> Triple(if (isDark) IconSafeDark    else IconSafeLight,
                    Icons.Filled.CheckCircle,   "Safe")

                LaunchStatus.CAUTION-> Triple(if (isDark) IconCautionDark else IconCautionLight,
                    Icons.Filled.Warning,       "Caution")

                LaunchStatus.UNSAFE -> Triple(if (isDark) IconUnsafeDark  else IconUnsafeLight,
                    Icons.Filled.Cancel,        "Unsafe")
            }
        }
    }

    Icon(
        imageVector     = icon,
        contentDescription = description,
        tint            = color,
        modifier        = modifier
    )
}
