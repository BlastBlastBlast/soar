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
import androidx.compose.ui.Modifier
import no.uio.ifi.in2000.met2025.data.local.database.ConfigProfile
import no.uio.ifi.in2000.met2025.data.models.Constants.Companion.CAUTION_THRESHOLD
import no.uio.ifi.in2000.met2025.data.models.Constants.Companion.UNSAFE_THRESHOLD
import no.uio.ifi.in2000.met2025.data.models.ForecastDataItem
import no.uio.ifi.in2000.met2025.data.models.IsobaricData
import no.uio.ifi.in2000.met2025.ui.theme.*

// Definition for EvaluationIcon remains unchanged.
sealed class EvaluationIcon {
    data class DrawableIcon(val resId: Int) : EvaluationIcon()
    data class VectorIcon(val icon: androidx.compose.ui.graphics.vector.ImageVector) : EvaluationIcon()
}

enum class LaunchStatus {
    SAFE,           // All values comfortably within spec.
    CAUTION,        // Some values are close to threshold.
    UNSAFE,         // One or more values exceed the allowed threshold.
}

fun launchStatus(relativeUnsafety: Double): LaunchStatus {
    return when {
        relativeUnsafety > UNSAFE_THRESHOLD -> LaunchStatus.UNSAFE
        relativeUnsafety > CAUTION_THRESHOLD -> LaunchStatus.CAUTION
        else -> LaunchStatus.SAFE
    }
}

@Composable
fun LaunchStatusIcon(state: ParameterState, modifier: Modifier) {
    val (color, icon, description) = when (state) {
        is ParameterState.Missing ->
            Triple(IconPurple, Icons.Filled.CloudOff, "Data missing")
        is ParameterState.Disabled ->
            Triple(IconGrey, Icons.Filled.Close, "Turned Off")
        is ParameterState.Available ->
            when (launchStatus(state.relativeUnsafety)) {
                LaunchStatus.SAFE -> Triple(
                    MaterialTheme.colorScheme.onPrimary, Icons.Filled.CheckCircle, "Safe")
                LaunchStatus.CAUTION -> Triple(MaterialTheme.colorScheme.onPrimary, Icons.Filled.Warning, "Caution")
                LaunchStatus.UNSAFE -> Triple(MaterialTheme.colorScheme.onPrimary, Icons.Filled.Cancel, "Unsafe")
            }
    }
    Icon(imageVector = icon, contentDescription = description, tint = color, modifier = modifier)
}

@Composable
fun LaunchStatusIndicator(
    config: ConfigProfile,
    forecast: ForecastDataItem? = null,
    isobaric: IsobaricData? = null,
    modifier: Modifier
) {
    val state = evaluateLaunchConditions(config, forecast, isobaric)
    LaunchStatusIcon(state, modifier)
}
