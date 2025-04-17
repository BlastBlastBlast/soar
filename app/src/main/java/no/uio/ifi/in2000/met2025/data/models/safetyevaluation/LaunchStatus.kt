package no.uio.ifi.in2000.met2025.data.models.safetyevaluation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import no.uio.ifi.in2000.met2025.data.local.database.ConfigProfile
import no.uio.ifi.in2000.met2025.data.models.Constants.Companion.CAUTION_THRESHOLD
import no.uio.ifi.in2000.met2025.data.models.Constants.Companion.UNSAFE_THRESHOLD
import no.uio.ifi.in2000.met2025.data.models.locationforecast.ForecastDataItem
import no.uio.ifi.in2000.met2025.data.models.isobaric.IsobaricData

sealed class EvaluationIcon {
    data class DrawableIcon(val resId: Int) : EvaluationIcon()
    data class VectorIcon(val icon: androidx.compose.ui.graphics.vector.ImageVector) : EvaluationIcon()
}

enum class LaunchStatus {
    SAFE,           // All values comfortably within spec
    CAUTION,        // Some values are close to threshold
    UNSAFE,         // One or more values exceed the allowed threshold
}

fun launchStatus(relativeUnsafety: Double): LaunchStatus {
    return when {
        relativeUnsafety > UNSAFE_THRESHOLD -> LaunchStatus.UNSAFE
        relativeUnsafety > CAUTION_THRESHOLD -> LaunchStatus.CAUTION
        else -> LaunchStatus.SAFE
    }
}

@Composable
fun LaunchStatusIcon(state: ParameterState) {
    val (color, icon, description) = when (state) {
        is ParameterState.Missing -> Triple(MaterialTheme.colorScheme.tertiary, Icons.Filled.CloudOff, "Data missing")
        is ParameterState.Disabled -> Triple(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f), Icons.Filled.Close, "Turned Off")
        is ParameterState.Available -> when (launchStatus(state.relativeUnsafety)) {
            LaunchStatus.SAFE -> Triple(MaterialTheme.colorScheme.primary, Icons.Filled.CheckCircle, "Safe")
            LaunchStatus.CAUTION -> Triple(MaterialTheme.colorScheme.secondary, Icons.Filled.Warning, "Caution")
            LaunchStatus.UNSAFE -> Triple(MaterialTheme.colorScheme.error, Icons.Filled.Cancel, "Unsafe")
        }
    }
    Icon(imageVector = icon, contentDescription = description, tint = color)
}

@Composable
fun LaunchStatusIndicator(forecast: ForecastDataItem, config: ConfigProfile) {
    val state = evaluateLaunchConditions(forecast, config)
    val (color, icon, description) = when (state) {
        is ParameterState.Missing -> Triple(MaterialTheme.colorScheme.tertiary, Icons.Filled.CloudOff, "Data missing")
        is ParameterState.Disabled -> Triple(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f), Icons.Filled.Close, "Turned Off")
        is ParameterState.Available -> when (launchStatus(state.relativeUnsafety)) {
            LaunchStatus.SAFE -> Triple(MaterialTheme.colorScheme.primary, Icons.Filled.CheckCircle, "Safe to launch")
            LaunchStatus.CAUTION -> Triple(MaterialTheme.colorScheme.secondary, Icons.Filled.Warning, "Caution: Check conditions")
            LaunchStatus.UNSAFE -> Triple(MaterialTheme.colorScheme.error, Icons.Filled.Cancel, "Unsafe to launch")
        }
    }
    Icon(imageVector = icon, contentDescription = description, tint = color)
}

@Composable
fun LaunchStatusIndicator(isobaricData: IsobaricData, config: ConfigProfile) {
    val state = evaluateLaunchConditions(isobaricData, config)
    val (color, icon, description) = when (state) {
        is ParameterState.Missing -> Triple(MaterialTheme.colorScheme.tertiary, Icons.Filled.CloudOff, "Data missing")
        is ParameterState.Disabled -> Triple(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f), Icons.Filled.Close, "Turned Off")
        is ParameterState.Available -> when (launchStatus(state.relativeUnsafety)) {
            LaunchStatus.SAFE -> Triple(MaterialTheme.colorScheme.primary, Icons.Filled.CheckCircle, "Safe to launch")
            LaunchStatus.CAUTION -> Triple(MaterialTheme.colorScheme.secondary, Icons.Filled.Warning, "Caution: Check conditions")
            LaunchStatus.UNSAFE -> Triple(MaterialTheme.colorScheme.error, Icons.Filled.Cancel, "Unsafe to launch")
        }
    }
    Icon(imageVector = icon, contentDescription = description, tint = color)
}