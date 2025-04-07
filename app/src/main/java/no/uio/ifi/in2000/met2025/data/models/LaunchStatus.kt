package no.uio.ifi.in2000.met2025.data.models

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import no.uio.ifi.in2000.met2025.data.local.Database.ConfigProfile

enum class LaunchStatus {
    SAFE,      // All values comfortably within spec
    CAUTION,   // Some values are close to threshold
    UNSAFE     // One or more values exceed the allowed threshold
}

// Modified to use config thresholds
fun evaluateLaunchConditions(forecast: ForecastDataItem, config: ConfigProfile): LaunchStatus {
    var caution = false

    // Ground wind: measured as windSpeed
    if (forecast.values.windSpeed > config.groundWindThreshold * 1.1) {
        return LaunchStatus.UNSAFE
    } else if (forecast.values.windSpeed > config.groundWindThreshold * 0.9) {
        caution = true
    }

    // Air wind (using wind gust as a proxy)
    if (forecast.values.windSpeedOfGust > config.airWindThreshold * 1.1) {
        return LaunchStatus.UNSAFE
    } else if (forecast.values.windSpeedOfGust > config.airWindThreshold * 0.9) {
        caution = true
    }

    // Cloud cover (overall)
    if (forecast.values.cloudAreaFraction > config.cloudCoverThreshold * 1.1) {
        return LaunchStatus.UNSAFE
    } else if (forecast.values.cloudAreaFraction > config.cloudCoverThreshold * 0.9) {
        caution = true
    }

    // Cloud cover (sub-levels)
    if (forecast.values.cloudAreaFractionHigh > config.cloudCoverThreshold * 1.1 ||
        forecast.values.cloudAreaFractionLow > config.cloudCoverThreshold * 1.1 ||
        forecast.values.cloudAreaFractionMedium > config.cloudCoverThreshold * 1.1) {
        return LaunchStatus.UNSAFE
    } else if (forecast.values.cloudAreaFractionHigh > config.cloudCoverThreshold * 0.9 ||
        forecast.values.cloudAreaFractionLow > config.cloudCoverThreshold * 0.9 ||
        forecast.values.cloudAreaFractionMedium > config.cloudCoverThreshold * 0.9) {
        caution = true
    }

    // Fog: should be exactly 0%
    if (forecast.values.fogAreaFraction > 0.0) {
        return LaunchStatus.UNSAFE
    }

    // Precipitation: should be 0 at launch time.
    if (forecast.values.precipitationAmount > 0.0) {
        return LaunchStatus.UNSAFE
    }

    // Humidity: target is around the threshold specified in the config
    if (forecast.values.relativeHumidity > config.humidityThreshold * 1.1) {
        return LaunchStatus.UNSAFE
    } else if (forecast.values.relativeHumidity > config.humidityThreshold * 0.9) {
        caution = true
    }

    // Dew point: max as specified in the config
    if (forecast.values.dewPointTemperature > config.dewPointThreshold * 1.1) {
        return LaunchStatus.UNSAFE
    } else if (forecast.values.dewPointTemperature > config.dewPointThreshold * 0.9) {
        caution = true
    }

    return if (caution) LaunchStatus.CAUTION else LaunchStatus.SAFE
}

// New data class for individual parameter evaluations
data class ParameterEvaluation(
    val label: String,
    val value: String,
    val status: LaunchStatus
)

// Modified to use thresholds from the provided config
fun evaluateParameterConditions(forecast: ForecastDataItem, config: ConfigProfile): List<ParameterEvaluation> {
    val evaluations = mutableListOf<ParameterEvaluation>()

    // Ground Wind (using windSpeed)
    val groundWindValue = forecast.values.windSpeed
    val groundWindStatus = evaluateValue(groundWindValue, config.groundWindThreshold)
    evaluations.add(ParameterEvaluation("Ground Wind", "$groundWindValue m/s", groundWindStatus))

    // Air Wind (using wind gust as a proxy)
    val airWindValue = forecast.values.windSpeedOfGust
    val airWindStatus = evaluateValue(airWindValue, config.airWindThreshold)
    evaluations.add(ParameterEvaluation("Air Wind", "$airWindValue m/s", airWindStatus))

    // Wind Direction is always displayed (assumed safe)
    val windDirectionValue = forecast.values.windFromDirection
    evaluations.add(ParameterEvaluation("Wind Direction", "$windDirectionValue°", LaunchStatus.SAFE))

    // Cloud Cover: overall
    val cloudCoverValue = forecast.values.cloudAreaFraction
    val cloudCoverStatus = evaluateValue(cloudCoverValue, config.cloudCoverThreshold)
    evaluations.add(ParameterEvaluation("Cloud Cover", "$cloudCoverValue%", cloudCoverStatus))

    // Cloud Cover High
    val cloudCoverHighValue = forecast.values.cloudAreaFractionHigh
    val cloudCoverHighStatus = evaluateValue(cloudCoverHighValue, config.cloudCoverThreshold)
    evaluations.add(ParameterEvaluation("Cloud Cover High", "$cloudCoverHighValue%", cloudCoverHighStatus))

    // Cloud Cover Low
    val cloudCoverLowValue = forecast.values.cloudAreaFractionLow
    val cloudCoverLowStatus = evaluateValue(cloudCoverLowValue, config.cloudCoverThreshold)
    evaluations.add(ParameterEvaluation("Cloud Cover Low", "$cloudCoverLowValue%", cloudCoverLowStatus))

    // Cloud Cover Medium
    val cloudCoverMediumValue = forecast.values.cloudAreaFractionMedium
    val cloudCoverMediumStatus = evaluateValue(cloudCoverMediumValue, config.cloudCoverThreshold)
    evaluations.add(ParameterEvaluation("Cloud Cover Medium", "$cloudCoverMediumValue%", cloudCoverMediumStatus))

    // Fog
    val fogValue = forecast.values.fogAreaFraction
    val fogStatus = if (fogValue > 0.0) LaunchStatus.UNSAFE else LaunchStatus.SAFE
    evaluations.add(ParameterEvaluation("Fog", "$fogValue%", fogStatus))

    // Precipitation
    val precipitationValue = forecast.values.precipitationAmount
    val precipitationStatus = if (precipitationValue > 0.0) LaunchStatus.UNSAFE else LaunchStatus.SAFE
    evaluations.add(ParameterEvaluation("Precipitation", "$precipitationValue mm", precipitationStatus))

    // Humidity
    val humidityValue = forecast.values.relativeHumidity
    val humidityStatus = evaluateValue(humidityValue, config.humidityThreshold)
    evaluations.add(ParameterEvaluation("Humidity", "$humidityValue%", humidityStatus))

    // Dew Point
    val dewPointValue = forecast.values.dewPointTemperature
    val dewPointStatus = evaluateValue(dewPointValue, config.dewPointThreshold)
    evaluations.add(ParameterEvaluation("Dew Point", "$dewPointValue°C", dewPointStatus))

    return evaluations
}

// Helper function for numeric parameters with a buffer of ±10%
fun evaluateValue(value: Double, threshold: Double): LaunchStatus {
    return when {
        value > threshold * 1.1 -> LaunchStatus.UNSAFE
        value > threshold * 0.9 -> LaunchStatus.CAUTION
        else -> LaunchStatus.SAFE
    }
}

// A composable to display an icon based on a single parameter's status.
@Composable
fun LaunchStatusIcon(status: LaunchStatus) {
    val (color, icon, description) = when (status) {
        LaunchStatus.SAFE -> Triple(MaterialTheme.colorScheme.primary, Icons.Filled.CheckCircle, "Safe")
        LaunchStatus.CAUTION -> Triple(MaterialTheme.colorScheme.secondary, Icons.Filled.Warning, "Caution")
        LaunchStatus.UNSAFE -> Triple(MaterialTheme.colorScheme.error, Icons.Filled.Close, "Unsafe")
    }
    Icon(imageVector = icon, contentDescription = description, tint = color)
}

@Composable
fun LaunchStatusIndicator(forecast: ForecastDataItem, config: ConfigProfile) {
    val status = evaluateLaunchConditions(forecast, config)
    val (color, icon, description) = when (status) {
        LaunchStatus.SAFE -> Triple(MaterialTheme.colorScheme.primary, Icons.Default.CheckCircle, "Safe to launch")
        LaunchStatus.CAUTION -> Triple(MaterialTheme.colorScheme.secondary, Icons.Default.Warning, "Caution: Check conditions")
        LaunchStatus.UNSAFE -> Triple(MaterialTheme.colorScheme.error, Icons.Default.Close, "Unsafe to launch")
    }
    Icon(imageVector = icon, contentDescription = description, tint = color)
}
