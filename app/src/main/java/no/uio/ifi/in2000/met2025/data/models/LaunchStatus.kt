package no.uio.ifi.in2000.met2025.data.models

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import no.uio.ifi.in2000.met2025.data.local.database.ConfigProfile

enum class LaunchStatus {
    SAFE,           // All values comfortably within spec
    CAUTION,        // Some values are close to threshold
    UNSAFE,         // One or more values exceed the allowed threshold
    DISABLED,       // Parameter evaluation is turned off
    MISSING_DATA    // One or more required values are missing
}

/**
 * Evaluates the overall launch conditions from a forecast and a configuration.
 *
 * For parameters that might be missing (i.e. their value is null), we mark the global status as MISSING_DATA.
 * Otherwise, if any parameter exceeds 110% of the threshold, we return UNSAFE;
 * if at least one parameter is in the “caution” range (above 90% of the threshold)
 * then overall status is CAUTION (unless an unsafe condition was found).
 */
fun evaluateLaunchConditions(forecast: ForecastDataItem, config: ConfigProfile): LaunchStatus {
    var caution = false
    var missing = false

    // Ground wind (non-null)
    if (config.isEnabledGroundWind) {
        val groundWind = forecast.values.windSpeed
        if (groundWind > config.groundWindThreshold * 1.1) return LaunchStatus.UNSAFE
        else if (groundWind > config.groundWindThreshold * 0.9) caution = true
    }

    // Air wind (nullable)
    if (config.isEnabledAirWind) {
        val airWind = forecast.values.windSpeedOfGust
        if (airWind == null) {
            missing = true
        } else {
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
            if (fog != null && fog > 0.0) return LaunchStatus.UNSAFE
            else if (fog == null) missing = true
        } else {
            if (fog == null) {
                missing = true
            } else {
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
        if (dew == null) {
            missing = true
        } else {
            if (dew > config.dewPointThreshold * 1.1) return LaunchStatus.UNSAFE
            else if (dew > config.dewPointThreshold * 0.9) caution = true
        }
    }

    // If any parameter was missing, return MISSING_DATA;
    // otherwise, if any value caused caution then overall status is CAUTION; else SAFE.
    if (missing) return LaunchStatus.MISSING_DATA
    return if (caution) LaunchStatus.CAUTION else LaunchStatus.SAFE
}

/**
 * Data class representing a single parameter's evaluation result.
 *
 * @param label A descriptive name (e.g. "Ground Wind").
 * @param value The measurement (e.g. "8.6 m/s" or "Not available").
 * @param status The evaluation status for this parameter.
 */
data class ParameterEvaluation(
    val label: String,
    val value: String,
    val status: LaunchStatus
)

/**
 * Evaluates each parameter in [forecast] against the thresholds in [config] and returns a list
 * of [ParameterEvaluation]. For missing data (when a value is null), displays "Not available"
 * and the MISSING_DATA status.
 */
fun evaluateParameterConditions(forecast: ForecastDataItem, config: ConfigProfile): List<ParameterEvaluation> {
    val evaluations = mutableListOf<ParameterEvaluation>()

    // Ground Wind (non-null)
    if (config.isEnabledGroundWind) {
        val value = forecast.values.windSpeed
        val status = evaluateValue(value, config.groundWindThreshold)
        evaluations.add(ParameterEvaluation("Ground Wind", "$value m/s", status))
    } else {
        evaluations.add(ParameterEvaluation("Ground Wind", "Turned Off", LaunchStatus.DISABLED))
    }

    // Air Wind (nullable)
    if (config.isEnabledAirWind) {
        val value = forecast.values.windSpeedOfGust
        if (value == null) {
            evaluations.add(ParameterEvaluation("Air Wind", "Not available", LaunchStatus.MISSING_DATA))
        } else {
            val status = evaluateValue(value, config.airWindThreshold)
            evaluations.add(ParameterEvaluation("Air Wind", "$value m/s", status))
        }
    } else {
        evaluations.add(ParameterEvaluation("Air Wind", "Turned Off", LaunchStatus.DISABLED))
    }

    // Wind Direction (non-null)
    if (config.isEnabledWindDirection) {
        val value = forecast.values.windFromDirection
        evaluations.add(ParameterEvaluation("Wind Direction", "$value°", LaunchStatus.SAFE))
    } else {
        evaluations.add(ParameterEvaluation("Wind Direction", "Turned Off", LaunchStatus.DISABLED))
    }

    // Overall Cloud Cover (non-null)
    if (config.isEnabledCloudCover) {
        val value = forecast.values.cloudAreaFraction
        val status = evaluateValue(value, config.cloudCoverThreshold)
        evaluations.add(ParameterEvaluation("Cloud Cover", "$value%", status))
    } else {
        evaluations.add(ParameterEvaluation("Cloud Cover", "Turned Off", LaunchStatus.DISABLED))
    }

    // Cloud Cover High (non-null)
    if (config.isEnabledCloudCoverHigh) {
        val value = forecast.values.cloudAreaFractionHigh
        val status = evaluateValue(value, config.cloudCoverHighThreshold)
        evaluations.add(ParameterEvaluation("Cloud Cover High", "$value%", status))
    } else {
        evaluations.add(ParameterEvaluation("Cloud Cover High", "Turned Off", LaunchStatus.DISABLED))
    }

    // Cloud Cover Medium (non-null)
    if (config.isEnabledCloudCoverMedium) {
        val value = forecast.values.cloudAreaFractionMedium
        val status = evaluateValue(value, config.cloudCoverMediumThreshold)
        evaluations.add(ParameterEvaluation("Cloud Cover Medium", "$value%", status))
    } else {
        evaluations.add(ParameterEvaluation("Cloud Cover Medium", "Turned Off", LaunchStatus.DISABLED))
    }

    // Cloud Cover Low (non-null)
    if (config.isEnabledCloudCoverLow) {
        val value = forecast.values.cloudAreaFractionLow
        val status = evaluateValue(value, config.cloudCoverLowThreshold)
        evaluations.add(ParameterEvaluation("Cloud Cover Low", "$value%", status))
    } else {
        evaluations.add(ParameterEvaluation("Cloud Cover Low", "Turned Off", LaunchStatus.DISABLED))
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
        evaluations.add(ParameterEvaluation("Fog", display, status))
    } else {
        evaluations.add(ParameterEvaluation("Fog", "Turned Off", LaunchStatus.DISABLED))
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
        evaluations.add(ParameterEvaluation("Precipitation", display, status))
    } else {
        evaluations.add(ParameterEvaluation("Precipitation", "Turned Off", LaunchStatus.DISABLED))
    }

    // Humidity (non-null)
    if (config.isEnabledHumidity) {
        val value = forecast.values.relativeHumidity
        val status = evaluateValue(value, config.humidityThreshold)
        evaluations.add(ParameterEvaluation("Humidity", "$value%", status))
    } else {
        evaluations.add(ParameterEvaluation("Humidity", "Turned Off", LaunchStatus.DISABLED))
    }

    // Dew Point (nullable)
    if (config.isEnabledDewPoint) {
        val value = forecast.values.dewPointTemperature
        if (value == null) {
            evaluations.add(ParameterEvaluation("Dew Point", "Not available", LaunchStatus.MISSING_DATA))
        } else {
            val status = evaluateValue(value, config.dewPointThreshold)
            evaluations.add(ParameterEvaluation("Dew Point", "$value°C", status))
        }
    } else {
        evaluations.add(ParameterEvaluation("Dew Point", "Turned Off", LaunchStatus.DISABLED))
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
        evaluations.add(ParameterEvaluation("Probability of Thunder", display, status))
    } else {
        evaluations.add(ParameterEvaluation("Probability of Thunder", "Turned Off", LaunchStatus.DISABLED))
    }

    return evaluations
}

/**
 * Displays an icon based on the provided [status]. A specific icon, color, and description
 * is selected for each [LaunchStatus] including MISSING_DATA.
 */
@Composable
fun LaunchStatusIcon(status: LaunchStatus) {
    val (color, icon, description) = when (status) {
        LaunchStatus.SAFE -> Triple(MaterialTheme.colorScheme.primary, Icons.Filled.CheckCircle, "Safe")
        LaunchStatus.CAUTION -> Triple(MaterialTheme.colorScheme.secondary, Icons.Filled.Warning, "Caution")
        LaunchStatus.UNSAFE -> Triple(MaterialTheme.colorScheme.error, Icons.Filled.Close, "Unsafe")
        LaunchStatus.DISABLED -> Triple(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f), Icons.Filled.Close, "Turned Off")
        LaunchStatus.MISSING_DATA -> Triple(MaterialTheme.colorScheme.tertiary, Icons.Filled.Warning, "Data missing")
    }
    Icon(imageVector = icon, contentDescription = description, tint = color)
}

/**
 * Evaluates an individual forecast value (which may be null) against a threshold.
 * If the value is null, returns MISSING_DATA; otherwise, compares the value to the threshold.
 */
fun evaluateValue(value: Double?, threshold: Double): LaunchStatus {
    if (value == null) return LaunchStatus.MISSING_DATA
    return when {
        value > threshold * 1.1 -> LaunchStatus.UNSAFE
        value > threshold * 0.9 -> LaunchStatus.CAUTION
        else -> LaunchStatus.SAFE
    }
}

/**
 * A composable indicator that displays an overall launch status icon for the forecast
 * according to the evaluated launch conditions.
 */
@Composable
fun LaunchStatusIndicator(forecast: ForecastDataItem, config: ConfigProfile) {
    val status = evaluateLaunchConditions(forecast, config)
    val (color, icon, description) = when (status) {
        LaunchStatus.SAFE -> Triple(MaterialTheme.colorScheme.primary, Icons.Filled.CheckCircle, "Safe to launch")
        LaunchStatus.CAUTION -> Triple(MaterialTheme.colorScheme.secondary, Icons.Filled.Warning, "Caution: Check conditions")
        LaunchStatus.UNSAFE -> Triple(MaterialTheme.colorScheme.error, Icons.Filled.Close, "Unsafe to launch")
        LaunchStatus.DISABLED -> Triple(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f), Icons.Filled.Close, "Turned Off")
        LaunchStatus.MISSING_DATA -> Triple(MaterialTheme.colorScheme.tertiary, Icons.Filled.Warning, "Data missing")
    }
    Icon(imageVector = icon, contentDescription = description, tint = color)
}