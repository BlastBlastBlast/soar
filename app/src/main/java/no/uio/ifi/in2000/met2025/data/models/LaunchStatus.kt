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

fun evaluateLaunchConditions(forecast: ForecastDataItem, config: ConfigProfile): LaunchStatus {
    var caution = false
    var missing = false

    // Ground Wind (non-null)
    if (config.isEnabledGroundWind) {
        val groundWind = forecast.values.windSpeed
        if (groundWind > config.groundWindThreshold * 1.1) return LaunchStatus.UNSAFE
        else if (groundWind > config.groundWindThreshold * 0.9) caution = true
    }

    // Air Wind (nullable)
    if (config.isEnabledAirWind) {
        val airWind = forecast.values.windSpeedOfGust
        if (airWind == null) missing = true
        else {
            if (airWind > config.airWindThreshold * 1.1) return LaunchStatus.UNSAFE
            else if (airWind > config.airWindThreshold * 0.9) caution = true
        }
    }

    // Overall Cloud Cover (non-null)
    if (config.isEnabledCloudCover) {
        val overallCloud = forecast.values.cloudAreaFraction
        if (overallCloud > config.cloudCoverThreshold * 1.1) return LaunchStatus.UNSAFE
        else if (overallCloud > config.cloudCoverThreshold * 0.9) caution = true
    }

    // Cloud Cover High (non-null)
    if (config.isEnabledCloudCoverHigh) {
        val cloudHigh = forecast.values.cloudAreaFractionHigh
        if (cloudHigh > config.cloudCoverHighThreshold * 1.1) return LaunchStatus.UNSAFE
        else if (cloudHigh > config.cloudCoverHighThreshold * 0.9) caution = true
    }

    // Cloud Cover Medium (non-null)
    if (config.isEnabledCloudCoverMedium) {
        val cloudMedium = forecast.values.cloudAreaFractionMedium
        if (cloudMedium > config.cloudCoverMediumThreshold * 1.1) return LaunchStatus.UNSAFE
        else if (cloudMedium > config.cloudCoverMediumThreshold * 0.9) caution = true
    }

    // Cloud Cover Low (non-null)
    if (config.isEnabledCloudCoverLow) {
        val cloudLow = forecast.values.cloudAreaFractionLow
        if (cloudLow > config.cloudCoverLowThreshold * 1.1) return LaunchStatus.UNSAFE
        else if (cloudLow > config.cloudCoverLowThreshold * 0.9) caution = true
    }

    // Fog (nullable)
    if (config.isEnabledFog) {
        val fog = forecast.values.fogAreaFraction
        if (config.fogThreshold == 0.0) {
            if (fog == null) missing = true
            else if (fog > 0.0) return LaunchStatus.UNSAFE
        } else {
            if (fog == null) missing = true
            else {
                if (fog > config.fogThreshold * 1.1) return LaunchStatus.UNSAFE
                else if (fog > config.fogThreshold * 0.9) caution = true
            }
        }
    }

    // Precipitation (nullable)
    if (config.isEnabledPrecipitation) {
        val precip = forecast.values.precipitationAmount
        if (config.precipitationThreshold == 0.0) {
            if (precip == null) missing = true
            else if (precip > 0.0) caution = true
        } else {
            if (precip == null) missing = true
            else if (precip > config.precipitationThreshold * 1.1) return LaunchStatus.UNSAFE
            else if (precip > config.precipitationThreshold * 0.9) caution = true
        }
    }

    // Probability of Thunder (nullable)
    if (config.isEnabledProbabilityOfThunder) {
        val thunder = forecast.values.probabilityOfThunder
        if (config.probabilityOfThunderThreshold == 0.0) {
            if (thunder == null) missing = true
            else if (thunder > 0.0) caution = true
        } else {
            if (thunder == null) missing = true
            else if (thunder > config.probabilityOfThunderThreshold * 1.1) return LaunchStatus.UNSAFE
            else if (thunder > config.probabilityOfThunderThreshold * 0.9) caution = true
        }
    }

    // Humidity (non-null)
    if (config.isEnabledHumidity) {
        val humidity = forecast.values.relativeHumidity
        if (humidity > config.humidityThreshold * 1.1) return LaunchStatus.UNSAFE
        else if (humidity > config.humidityThreshold * 0.9) caution = true
    }

    // Dew point (nullable)
    if (config.isEnabledDewPoint) {
        val dew = forecast.values.dewPointTemperature
        if (dew == null) missing = true
        else {
            if (dew > config.dewPointThreshold * 1.1) return LaunchStatus.UNSAFE
            else if (dew > config.dewPointThreshold * 0.9) caution = true
        }
    }

    if (missing) return LaunchStatus.MISSING_DATA
    return if (caution) LaunchStatus.CAUTION else LaunchStatus.SAFE
}

fun evaluateValue(value: Double?, threshold: Double): LaunchStatus {
    if (value == null) return LaunchStatus.MISSING_DATA
    return when {
        value > threshold * 1.1 -> LaunchStatus.UNSAFE
        value > threshold * 0.9 -> LaunchStatus.CAUTION
        else -> LaunchStatus.SAFE
    }
}

data class ParameterEvaluation(
    val label: String,
    val value: String,
    val status: LaunchStatus,
    val icon: EvaluationIcon? = null
)

fun evaluateParameterConditions(forecast: ForecastDataItem, config: ConfigProfile): List<ParameterEvaluation> {
    val evaluations = mutableListOf<ParameterEvaluation>()

    // Ground Wind (non-null)
    if (config.isEnabledGroundWind) {
        val value = forecast.values.windSpeed
        val status = evaluateValue(value, config.groundWindThreshold)
        evaluations.add(
            ParameterEvaluation(
                label = "Ground Wind",
                value = "$value m/s",
                status = status,
                icon = EvaluationIcon.DrawableIcon(R.drawable.wind)
            )
        )
    } else {
        evaluations.add(
            ParameterEvaluation(
                label = "Ground Wind",
                value = "Turned Off",
                status = LaunchStatus.DISABLED,
                icon = EvaluationIcon.DrawableIcon(R.drawable.wind)
            )
        )
    }

    // Air Wind (nullable)
    if (config.isEnabledAirWind) {
        val value = forecast.values.windSpeedOfGust
        if (value == null) {
            evaluations.add(
                ParameterEvaluation(
                    label = "Air Wind",
                    value = "Not available",
                    status = LaunchStatus.MISSING_DATA,
                    icon = EvaluationIcon.DrawableIcon(R.drawable.wind)
                )
            )
        } else {
            val status = evaluateValue(value, config.airWindThreshold)
            evaluations.add(
                ParameterEvaluation(
                    label = "Air Wind",
                    value = "$value m/s",
                    status = status,
                    icon = EvaluationIcon.DrawableIcon(R.drawable.wind)
                )
            )
        }
    } else {
        evaluations.add(
            ParameterEvaluation(
                label = "Air Wind",
                value = "Turned Off",
                status = LaunchStatus.DISABLED,
                icon = EvaluationIcon.DrawableIcon(R.drawable.wind)
            )
        )
    }

    // Wind Direction (non-null) using a vector icon.
    if (config.isEnabledWindDirection) {
        val value = forecast.values.windFromDirection
        evaluations.add(
            ParameterEvaluation(
                label = "Wind Direction",
                value = "$value°",
                status = LaunchStatus.SAFE,
                icon = EvaluationIcon.VectorIcon(Icons.Filled.ArrowDownward)
            )
        )
    } else {
        evaluations.add(
            ParameterEvaluation(
                label = "Wind Direction",
                value = "Turned Off",
                status = LaunchStatus.DISABLED,
                icon = EvaluationIcon.VectorIcon(Icons.Filled.ArrowDownward)
            )
        )
    }

    // Overall Cloud Cover (non-null)
    if (config.isEnabledCloudCover) {
        val value = forecast.values.cloudAreaFraction
        val status = evaluateValue(value, config.cloudCoverThreshold)
        evaluations.add(
            ParameterEvaluation(
                label = "Cloud Cover",
                value = "$value%",
                status = status,
                icon = EvaluationIcon.DrawableIcon(R.drawable.cloud)
            )
        )
    } else {
        evaluations.add(
            ParameterEvaluation(
                label = "Cloud Cover",
                value = "Turned Off",
                status = LaunchStatus.DISABLED,
                icon = EvaluationIcon.DrawableIcon(R.drawable.cloud)
            )
        )
    }

    // Cloud Cover High (non-null)
    if (config.isEnabledCloudCoverHigh) {
        val value = forecast.values.cloudAreaFractionHigh
        val status = evaluateValue(value, config.cloudCoverHighThreshold)
        evaluations.add(
            ParameterEvaluation(
                label = "Cloud Cover High",
                value = "$value%",
                status = status,
                icon = EvaluationIcon.DrawableIcon(R.drawable.cloud)
            )
        )
    } else {
        evaluations.add(
            ParameterEvaluation(
                label = "Cloud Cover High",
                value = "Turned Off",
                status = LaunchStatus.DISABLED,
                icon = EvaluationIcon.DrawableIcon(R.drawable.cloud)
            )
        )
    }

    // Cloud Cover Medium (non-null)
    if (config.isEnabledCloudCoverMedium) {
        val value = forecast.values.cloudAreaFractionMedium
        val status = evaluateValue(value, config.cloudCoverMediumThreshold)
        evaluations.add(
            ParameterEvaluation(
                label = "Cloud Cover Medium",
                value = "$value%",
                status = status,
                icon = EvaluationIcon.DrawableIcon(R.drawable.cloud)
            )
        )
    } else {
        evaluations.add(
            ParameterEvaluation(
                label = "Cloud Cover Medium",
                value = "Turned Off",
                status = LaunchStatus.DISABLED,
                icon = EvaluationIcon.DrawableIcon(R.drawable.cloud)
            )
        )
    }

    // Cloud Cover Low (non-null)
    if (config.isEnabledCloudCoverLow) {
        val value = forecast.values.cloudAreaFractionLow
        val status = evaluateValue(value, config.cloudCoverLowThreshold)
        evaluations.add(
            ParameterEvaluation(
                label = "Cloud Cover Low",
                value = "$value%",
                status = status,
                icon = EvaluationIcon.DrawableIcon(R.drawable.cloud)
            )
        )
    } else {
        evaluations.add(
            ParameterEvaluation(
                label = "Cloud Cover Low",
                value = "Turned Off",
                status = LaunchStatus.DISABLED,
                icon = EvaluationIcon.DrawableIcon(R.drawable.cloud)
            )
        )
    }

    // Fog (nullable)
    if (config.isEnabledFog) {
        val value = forecast.values.fogAreaFraction
        val status = if (config.fogThreshold == 0.0) {
            if (value == null) LaunchStatus.MISSING_DATA else if (value > 0.0) LaunchStatus.UNSAFE else LaunchStatus.SAFE
        } else {
            evaluateValue(value, config.fogThreshold)
        }
        val display = if (value == null) "Not available" else "$value%"
        evaluations.add(
            ParameterEvaluation(
                label = "Fog",
                value = display,
                status = status,
                icon = EvaluationIcon.DrawableIcon(R.drawable.fog)
            )
        )
    } else {
        evaluations.add(
            ParameterEvaluation(
                label = "Fog",
                value = "Turned Off",
                status = LaunchStatus.DISABLED,
                icon = EvaluationIcon.DrawableIcon(R.drawable.fog)
            )
        )
    }

    // Precipitation (nullable)
    if (config.isEnabledPrecipitation) {
        val value = forecast.values.precipitationAmount
        val status = if (config.precipitationThreshold == 0.0) {
            if (value == null) LaunchStatus.MISSING_DATA else if (value > 0.0) LaunchStatus.CAUTION else LaunchStatus.SAFE
        } else {
            evaluateValue(value, config.precipitationThreshold)
        }
        val display = if (value == null) "Not available" else "$value mm"
        evaluations.add(
            ParameterEvaluation(
                label = "Precipitation",
                value = display,
                status = status,
                icon = EvaluationIcon.DrawableIcon(R.drawable.rain)
            )
        )
    } else {
        evaluations.add(
            ParameterEvaluation(
                label = "Precipitation",
                value = "Turned Off",
                status = LaunchStatus.DISABLED,
                icon = EvaluationIcon.DrawableIcon(R.drawable.rain)
            )
        )
    }

    // Humidity (non-null)
    if (config.isEnabledHumidity) {
        val value = forecast.values.relativeHumidity
        val status = evaluateValue(value, config.humidityThreshold)
        evaluations.add(
            ParameterEvaluation(
                label = "Humidity",
                value = "$value%",
                status = status,
                icon = EvaluationIcon.DrawableIcon(R.drawable.humidity)
            )
        )
    } else {
        evaluations.add(
            ParameterEvaluation(
                label = "Humidity",
                value = "Turned Off",
                status = LaunchStatus.DISABLED,
                icon = EvaluationIcon.DrawableIcon(R.drawable.humidity)
            )
        )
    }

    // Dew Point (nullable)
    if (config.isEnabledDewPoint) {
        val value = forecast.values.dewPointTemperature
        if (value == null) {
            evaluations.add(
                ParameterEvaluation(
                    label = "Dew Point",
                    value = "Not available",
                    status = LaunchStatus.MISSING_DATA,
                    icon = EvaluationIcon.DrawableIcon(R.drawable.mist)
                )
            )
        } else {
            val status = evaluateValue(value, config.dewPointThreshold)
            evaluations.add(
                ParameterEvaluation(
                    label = "Dew Point",
                    value = "$value°C",
                    status = status,
                    icon = EvaluationIcon.DrawableIcon(R.drawable.mist)
                )
            )
        }
    } else {
        evaluations.add(
            ParameterEvaluation(
                label = "Dew Point",
                value = "Turned Off",
                status = LaunchStatus.DISABLED,
                icon = EvaluationIcon.DrawableIcon(R.drawable.mist)
            )
        )
    }

    // Probability of Thunder (nullable)
    if (config.isEnabledProbabilityOfThunder) {
        val value = forecast.values.probabilityOfThunder
        val status = if (config.probabilityOfThunderThreshold == 0.0) {
            if (value == null) LaunchStatus.MISSING_DATA else if (value > 0.0) LaunchStatus.CAUTION else LaunchStatus.SAFE
        } else {
            evaluateValue(value, config.probabilityOfThunderThreshold)
        }
        val display = if (value == null) "Not available" else "$value%"
        evaluations.add(
            ParameterEvaluation(
                label = "Thunder %",
                value = display,
                status = status,
                icon = EvaluationIcon.DrawableIcon(R.drawable.thunder)
            )
        )
    } else {
        evaluations.add(
            ParameterEvaluation(
                label = "Thunder %",
                value = "Turned Off",
                status = LaunchStatus.DISABLED,
                icon = EvaluationIcon.DrawableIcon(R.drawable.thunder)
            )
        )
    }

    return evaluations
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