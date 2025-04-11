package no.uio.ifi.in2000.met2025.data.models.launchstatus

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
import no.uio.ifi.in2000.met2025.data.models.ConfigParameter
import no.uio.ifi.in2000.met2025.data.models.ForecastDataItem
import no.uio.ifi.in2000.met2025.data.models.IsobaricData
import no.uio.ifi.in2000.met2025.domain.helpers.icon
import no.uio.ifi.in2000.met2025.domain.helpers.label
import no.uio.ifi.in2000.met2025.domain.helpers.threshold
import no.uio.ifi.in2000.met2025.domain.helpers.unit
import no.uio.ifi.in2000.met2025.domain.helpers.toConfigMap
import no.uio.ifi.in2000.met2025.domain.helpers.valueAt

sealed class EvaluationIcon {
    data class DrawableIcon(val resId: Int) : EvaluationIcon()
    data class VectorIcon(val icon: androidx.compose.ui.graphics.vector.ImageVector) : EvaluationIcon()
}

enum class LaunchStatus {
    SAFE,           // All values comfortably within spec
    CAUTION,        // Some values are close to threshold
    UNSAFE,         // One or more values exceed the allowed threshold
    DISABLED,       // Parameter evaluation is turned off
    MISSING_DATA    // One or more required values are missing
}

fun evaluateLaunchConditions(forecastDataItem: ForecastDataItem, config: ConfigProfile): LaunchStatus
= launchStatus(relativeUnsafety(forecastDataItem, config))

fun relativeUnsafety(forecastDataItem: ForecastDataItem, config: ConfigProfile): Double?
= relativeUnsafety(forecastDataItem.toConfigMap(config))

fun relativeUnsafety(valueThresholdMap: Map<ConfigParameter, Pair<Double, Double>>): Double?
= valueThresholdMap
    .map{ (_, pair) -> relativeUnsafety(pair.first, pair.second)!! }
    .maxOrNull()

fun evaluateLaunchConditions(isobaricData: IsobaricData, config: ConfigProfile): LaunchStatus {
    return launchStatus(relativeUnsafety(isobaricData, config))
}

fun relativeUnsafety(isobaricData: IsobaricData, config: ConfigProfile): Double? {
    //val valueThresholds
    return 0.0//relativeUnsafety()
}

fun relativeUnsafety(valueThresholdList: List<Pair<Double, Double>>): Double?
= valueThresholdList.maxOfOrNull { (value, threshold) -> relativeUnsafety(value, threshold)!! }

fun relativeUnsafety(value: Double?, threshold: Double): Double? {
    if (value == null) return null
    if (threshold == 0.0) {
        return if (value > threshold) Double.MAX_VALUE else 0.0
    }
    return value / threshold
}

fun launchStatus(relativeUnsafety: Double?): LaunchStatus {
    if (relativeUnsafety == null) return LaunchStatus.MISSING_DATA
    return when {
        relativeUnsafety > 1.1 -> LaunchStatus.UNSAFE
        relativeUnsafety > 0.9 -> LaunchStatus.CAUTION
        else -> LaunchStatus.SAFE
    }
}

fun evaluateParameterConditions(forecast: ForecastDataItem, config: ConfigProfile): List<ParameterEvaluation> {
    val parameters = ConfigParameter.entries

    return parameters.map { parameter ->
        val value = forecast.valueAt(parameter)
        val status = launchStatus(relativeUnsafety(value, config.threshold(parameter)))

        ParameterEvaluation(
            label = parameter.label(),
            value = when (status) {
                LaunchStatus.MISSING_DATA -> "Not available"
                LaunchStatus.DISABLED -> "Turned Off"
                else -> "$value ${parameter.unit()}"
            },
            status = status,
            icon = parameter.icon()
        )
    }
}

@Composable
fun LaunchStatusIcon(status: LaunchStatus) {
    val (color, icon, description) = when (status) {
        LaunchStatus.SAFE -> Triple(MaterialTheme.colorScheme.primary, Icons.Filled.CheckCircle, "Safe")
        LaunchStatus.CAUTION -> Triple(MaterialTheme.colorScheme.secondary, Icons.Filled.Warning, "Caution")
        LaunchStatus.UNSAFE -> Triple(MaterialTheme.colorScheme.error, Icons.Filled.Cancel, "Unsafe")
        LaunchStatus.DISABLED -> Triple(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f), Icons.Filled.Close, "Turned Off")
        LaunchStatus.MISSING_DATA -> Triple(MaterialTheme.colorScheme.tertiary, Icons.Filled.CloudOff, "Data missing")
    }
    Icon(imageVector = icon, contentDescription = description, tint = color)
}

@Composable
fun LaunchStatusIndicator(forecast: ForecastDataItem, config: ConfigProfile) {
    val status = evaluateLaunchConditions(forecast, config)
    val (color, icon, description) = when (status) {
        LaunchStatus.SAFE -> Triple(MaterialTheme.colorScheme.primary, Icons.Filled.CheckCircle, "Safe to launch")
        LaunchStatus.CAUTION -> Triple(MaterialTheme.colorScheme.secondary, Icons.Filled.Warning, "Caution: Check conditions")
        LaunchStatus.UNSAFE -> Triple(MaterialTheme.colorScheme.error, Icons.Filled.Cancel, "Unsafe to launch")
        LaunchStatus.DISABLED -> Triple(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f), Icons.Filled.Close, "Turned Off")
        LaunchStatus.MISSING_DATA -> Triple(MaterialTheme.colorScheme.tertiary, Icons.Filled.CloudOff, "Data missing")
    }
    Icon(imageVector = icon, contentDescription = description, tint = color)
}