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
    SAFE,      // All values comfortably within spec
    CAUTION,   // Some values are close to threshold
    UNSAFE,    // One or more values exceed the allowed threshold
    DISABLED   // Parameter evaluation is turned off
}


fun evaluateLaunchConditions(forecast: ForecastDataItem, config: ConfigProfile): LaunchStatus {
    var caution = false

    // Ground wind
    if (config.isEnabledGroundWind) {
        if (forecast.values.windSpeed > config.groundWindThreshold * 1.1) return LaunchStatus.UNSAFE
        else if (forecast.values.windSpeed > config.groundWindThreshold * 0.9) caution = true
    }

    // Air wind
    if (config.isEnabledAirWind) {
        if (forecast.values.windSpeedOfGust > config.airWindThreshold * 1.1) return LaunchStatus.UNSAFE
        else if (forecast.values.windSpeedOfGust > config.airWindThreshold * 0.9) caution = true
    }

    // Overall Cloud Cover (from API overall value)
    if (config.isEnabledCloudCover) {
        if (forecast.values.cloudAreaFraction > config.cloudCoverThreshold * 1.1) return LaunchStatus.UNSAFE
        else if (forecast.values.cloudAreaFraction > config.cloudCoverThreshold * 0.9) caution = true
    }

    // Cloud Cover High
    if (config.isEnabledCloudCoverHigh) {
        if (forecast.values.cloudAreaFractionHigh > config.cloudCoverHighThreshold * 1.1) return LaunchStatus.UNSAFE
        else if (forecast.values.cloudAreaFractionHigh > config.cloudCoverHighThreshold * 0.9) caution = true
    }
    // Cloud Cover Medium
    if (config.isEnabledCloudCoverMedium) {
        if (forecast.values.cloudAreaFractionMedium > config.cloudCoverMediumThreshold * 1.1) return LaunchStatus.UNSAFE
        else if (forecast.values.cloudAreaFractionMedium > config.cloudCoverMediumThreshold * 0.9) caution = true
    }
    // Cloud Cover Low
    if (config.isEnabledCloudCoverLow) {
        if (forecast.values.cloudAreaFractionLow > config.cloudCoverLowThreshold * 1.1) return LaunchStatus.UNSAFE
        else if (forecast.values.cloudAreaFractionLow > config.cloudCoverLowThreshold * 0.9) caution = true
    }

    // Fog: always evaluate.
    if (forecast.values.fogAreaFraction > 0.0) return LaunchStatus.UNSAFE

    // Precipitation: always evaluate.
    if (forecast.values.precipitationAmount > 0.0) return LaunchStatus.UNSAFE

    // Humidity
    if (config.isEnabledHumidity) {
        if (forecast.values.relativeHumidity > config.humidityThreshold * 1.1) return LaunchStatus.UNSAFE
        else if (forecast.values.relativeHumidity > config.humidityThreshold * 0.9) caution = true
    }

    // Dew point
    if (config.isEnabledDewPoint) {
        if (forecast.values.dewPointTemperature > config.dewPointThreshold * 1.1) return LaunchStatus.UNSAFE
        else if (forecast.values.dewPointTemperature > config.dewPointThreshold * 0.9) caution = true
    }

    return if (caution) LaunchStatus.CAUTION else LaunchStatus.SAFE
}

// New data class for individual parameter evaluations
data class ParameterEvaluation(
    val label: String,
    val value: String,
    val status: LaunchStatus
)

fun evaluateParameterConditions(forecast: ForecastDataItem, config: ConfigProfile): List<ParameterEvaluation> {
    val evaluations = mutableListOf<ParameterEvaluation>()

    // Ground Wind
    if (config.isEnabledGroundWind) {
        val groundWindValue = forecast.values.windSpeed
        val groundWindStatus = evaluateValue(groundWindValue, config.groundWindThreshold)
        evaluations.add(ParameterEvaluation("Ground Wind", "$groundWindValue m/s", groundWindStatus))
    } else {
        evaluations.add(ParameterEvaluation("Ground Wind", "Turned Off", LaunchStatus.DISABLED))
    }

    // Air Wind
    if (config.isEnabledAirWind) {
        val airWindValue = forecast.values.windSpeedOfGust
        val airWindStatus = evaluateValue(airWindValue, config.airWindThreshold)
        evaluations.add(ParameterEvaluation("Air Wind", "$airWindValue m/s", airWindStatus))
    } else {
        evaluations.add(ParameterEvaluation("Air Wind", "Turned Off", LaunchStatus.DISABLED))
    }

    // Wind Direction (always displayed)
    val windDirectionValue = forecast.values.windFromDirection
    evaluations.add(ParameterEvaluation("Wind Direction", "$windDirectionValue°", LaunchStatus.SAFE))

    // Overall Cloud Cover (using API's overall cloudAreaFraction)
    if (config.isEnabledCloudCover) {
        val overallCloudCover = forecast.values.cloudAreaFraction
        val overallCloudStatus = evaluateValue(overallCloudCover, config.cloudCoverThreshold)
        evaluations.add(ParameterEvaluation("Cloud Cover", "$overallCloudCover%", overallCloudStatus))
    } else {
        evaluations.add(ParameterEvaluation("Cloud Cover", "Turned Off", LaunchStatus.DISABLED))
    }

    // Cloud Cover High
    if (config.isEnabledCloudCoverHigh) {
        val cloudCoverHighValue = forecast.values.cloudAreaFractionHigh
        val cloudCoverHighStatus = evaluateValue(cloudCoverHighValue, config.cloudCoverHighThreshold)
        evaluations.add(ParameterEvaluation("Cloud Cover High", "$cloudCoverHighValue%", cloudCoverHighStatus))
    } else {
        evaluations.add(ParameterEvaluation("Cloud Cover High", "Turned Off", LaunchStatus.DISABLED))
    }

    // Cloud Cover Medium
    if (config.isEnabledCloudCoverMedium) {
        val cloudCoverMediumValue = forecast.values.cloudAreaFractionMedium
        val cloudCoverMediumStatus = evaluateValue(cloudCoverMediumValue, config.cloudCoverMediumThreshold)
        evaluations.add(ParameterEvaluation("Cloud Cover Medium", "$cloudCoverMediumValue%", cloudCoverMediumStatus))
    } else {
        evaluations.add(ParameterEvaluation("Cloud Cover Medium", "Turned Off", LaunchStatus.DISABLED))
    }

    // Cloud Cover Low
    if (config.isEnabledCloudCoverLow) {
        val cloudCoverLowValue = forecast.values.cloudAreaFractionLow
        val cloudCoverLowStatus = evaluateValue(cloudCoverLowValue, config.cloudCoverLowThreshold)
        evaluations.add(ParameterEvaluation("Cloud Cover Low", "$cloudCoverLowValue%", cloudCoverLowStatus))
    } else {
        evaluations.add(ParameterEvaluation("Cloud Cover Low", "Turned Off", LaunchStatus.DISABLED))
    }

    // Fog: Always evaluated.
    val fogValue = forecast.values.fogAreaFraction
    val fogStatus = if (fogValue > 0.0) LaunchStatus.UNSAFE else LaunchStatus.SAFE
    evaluations.add(ParameterEvaluation("Fog", "$fogValue%", fogStatus))

    // Precipitation: Always evaluated.
    val precipitationValue = forecast.values.precipitationAmount
    val precipitationStatus = if (precipitationValue > 0.0) LaunchStatus.UNSAFE else LaunchStatus.SAFE
    evaluations.add(ParameterEvaluation("Precipitation", "$precipitationValue mm", precipitationStatus))

    // Humidity
    if (config.isEnabledHumidity) {
        val humidityValue = forecast.values.relativeHumidity
        val humidityStatus = evaluateValue(humidityValue, config.humidityThreshold)
        evaluations.add(ParameterEvaluation("Humidity", "$humidityValue%", humidityStatus))
    } else {
        evaluations.add(ParameterEvaluation("Humidity", "Turned Off", LaunchStatus.DISABLED))
    }

    // Dew Point
    if (config.isEnabledDewPoint) {
        val dewPointValue = forecast.values.dewPointTemperature
        val dewPointStatus = evaluateValue(dewPointValue, config.dewPointThreshold)
        evaluations.add(ParameterEvaluation("Dew Point", "$dewPointValue°C", dewPointStatus))
    } else {
        evaluations.add(ParameterEvaluation("Dew Point", "Turned Off", LaunchStatus.DISABLED))
    }

    return evaluations
}



// A composable to display an icon based on a single parameter's status.
@Composable
fun LaunchStatusIcon(status: LaunchStatus) {
    val (color, icon, description) = when (status) {
        LaunchStatus.SAFE -> Triple(MaterialTheme.colorScheme.primary, Icons.Filled.CheckCircle, "Safe")
        LaunchStatus.CAUTION -> Triple(MaterialTheme.colorScheme.secondary, Icons.Filled.Warning, "Caution")
        LaunchStatus.UNSAFE -> Triple(MaterialTheme.colorScheme.error, Icons.Filled.Close, "Unsafe")
        LaunchStatus.DISABLED -> Triple(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f), Icons.Filled.Close, "Turned Off")
    }
    Icon(imageVector = icon, contentDescription = description, tint = color)
}

fun evaluateValue(value: Double, threshold: Double): LaunchStatus {
    return when {
        value > threshold * 1.1 -> LaunchStatus.UNSAFE
        value > threshold * 0.9 -> LaunchStatus.CAUTION
        else -> LaunchStatus.SAFE
    }
}

@Composable
fun LaunchStatusIndicator(forecast: ForecastDataItem, config: ConfigProfile) {
    val status = evaluateLaunchConditions(forecast, config)
    val (color, icon, description) = when (status) {
        LaunchStatus.SAFE -> Triple(MaterialTheme.colorScheme.primary, Icons.Default.CheckCircle, "Safe to launch")
        LaunchStatus.CAUTION -> Triple(MaterialTheme.colorScheme.secondary, Icons.Default.Warning, "Caution: Check conditions")
        LaunchStatus.UNSAFE -> Triple(MaterialTheme.colorScheme.error, Icons.Default.Close, "Unsafe to launch")
        LaunchStatus.DISABLED -> Triple(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f), Icons.Filled.Close, "Turned Off")
    }
    Icon(imageVector = icon, contentDescription = description, tint = color)
}
