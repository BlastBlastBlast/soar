package no.uio.ifi.in2000.met2025.data.models

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import no.uio.ifi.in2000.met2025.R
import no.uio.ifi.in2000.met2025.data.local.database.ConfigProfile
import no.uio.ifi.in2000.met2025.domain.helpers.icon
import no.uio.ifi.in2000.met2025.domain.helpers.label
import no.uio.ifi.in2000.met2025.domain.helpers.threshold
import no.uio.ifi.in2000.met2025.domain.helpers.unit

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

fun ForecastDataItem.toConfigMap(config: ConfigProfile): Map<ConfigParameter, Pair<Double, Double>>
= mapOf( //maybe use two maps, one for values and one for thresholds
    ConfigParameter.GROUND_WIND to Pair(values.windSpeed, config.groundWindThreshold),
    ConfigParameter.AIR_WIND to Pair(values.windSpeedOfGust, config.airWindThreshold),
    ConfigParameter.WIND_DIRECTION to Pair(values.windFromDirection, 0.0),
    ConfigParameter.CLOUD_COVER to Pair(values.cloudAreaFraction, config.cloudCoverThreshold),
    ConfigParameter.CLOUD_COVER_HIGH to Pair(values.cloudAreaFractionHigh, config.cloudCoverHighThreshold),
    ConfigParameter.CLOUD_COVER_MEDIUM to Pair(values.cloudAreaFractionMedium, config.cloudCoverMediumThreshold),
    ConfigParameter.CLOUD_COVER_LOW to Pair(values.cloudAreaFractionLow, config.cloudCoverLowThreshold),
    ConfigParameter.FOG to Pair(values.fogAreaFraction, config.fogThreshold),
    ConfigParameter.PRECIPITATION to Pair(values.precipitationAmount, config.precipitationThreshold),
    ConfigParameter.HUMIDITY to Pair(values.relativeHumidity, config.humidityThreshold),
    ConfigParameter.DEW_POINT to Pair(values.dewPointTemperature, config.dewPointThreshold),
    ConfigParameter.PROBABILITY_OF_THUNDER to Pair(values.probabilityOfThunder, config.probabilityOfThunderThreshold)
)
    .filter { (_, pair) -> pair.first != null }
    .mapValues { it.value.first!! to it.value.second }

fun evaluateLaunchConditions(forecastDataItem: ForecastDataItem, config: ConfigProfile): LaunchStatus
= launchStatus(relativeUnsafety(forecastDataItem, config))

fun relativeUnsafety(forecastDataItem: ForecastDataItem, config: ConfigProfile): Double?
= relativeUnsafety(forecastDataItem.toConfigMap(config))

fun relativeUnsafety(valueThresholdMap: Map<ConfigParameter, Pair<Double, Double>>): Double?
= valueThresholdMap
    .map{ (_, pair) -> relativeUnsafety(pair.first, pair.second)!! }
    .maxOrNull()

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

fun ForecastDataItem.valueAt(parameter: ConfigParameter): Double? {
    return when (parameter) {
        ConfigParameter.GROUND_WIND -> values.windSpeed
        ConfigParameter.AIR_WIND -> values.windSpeedOfGust
        ConfigParameter.WIND_DIRECTION -> values.windFromDirection
        ConfigParameter.CLOUD_COVER -> values.cloudAreaFraction
        ConfigParameter.CLOUD_COVER_HIGH -> values.cloudAreaFractionHigh
        ConfigParameter.CLOUD_COVER_MEDIUM -> values.cloudAreaFractionMedium
        ConfigParameter.CLOUD_COVER_LOW -> values.cloudAreaFractionLow
        ConfigParameter.FOG -> values.fogAreaFraction
        ConfigParameter.PRECIPITATION -> values.precipitationAmount
        ConfigParameter.HUMIDITY -> values.relativeHumidity
        ConfigParameter.DEW_POINT -> values.dewPointTemperature
        ConfigParameter.PROBABILITY_OF_THUNDER -> values.probabilityOfThunder
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

data class ParameterEvaluation(
    val label: String,
    val value: String,
    val status: LaunchStatus,
    val icon: EvaluationIcon? = null
)

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