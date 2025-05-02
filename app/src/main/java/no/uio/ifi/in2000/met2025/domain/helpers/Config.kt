package no.uio.ifi.in2000.met2025.domain.helpers

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import no.uio.ifi.in2000.met2025.R
import no.uio.ifi.in2000.met2025.data.local.database.ConfigProfile
import no.uio.ifi.in2000.met2025.data.models.ConfigParameter
import no.uio.ifi.in2000.met2025.data.models.safetyevaluation.EvaluationIcon

fun ConfigProfile.threshold(parameter: ConfigParameter): Double
= when (parameter) {
    ConfigParameter.GROUND_WIND -> groundWindThreshold
    ConfigParameter.AIR_WIND -> airWindThreshold
    ConfigParameter.WIND_DIRECTION -> Double.MIN_VALUE
    ConfigParameter.CLOUD_COVER -> cloudCoverThreshold
    ConfigParameter.CLOUD_COVER_HIGH -> cloudCoverHighThreshold
    ConfigParameter.CLOUD_COVER_MEDIUM -> cloudCoverMediumThreshold
    ConfigParameter.CLOUD_COVER_LOW -> cloudCoverLowThreshold
    ConfigParameter.FOG -> fogThreshold
    ConfigParameter.PRECIPITATION -> precipitationThreshold
    ConfigParameter.HUMIDITY -> humidityThreshold
    ConfigParameter.DEW_POINT -> dewPointThreshold
    ConfigParameter.PROBABILITY_OF_THUNDER -> probabilityOfThunderThreshold
    ConfigParameter.ALTITUDE_UPPER_BOUND -> altitudeUpperBound
    ConfigParameter.WIND_SHEAR_SPEED -> windShearSpeedThreshold
    ConfigParameter.WIND_SPEED_OF_GUST -> groundWindThreshold
    //else -> Double.MIN_VALUE
}

fun ConfigProfile.isEnabled(parameter: ConfigParameter): Boolean
= when (parameter) {
    ConfigParameter.GROUND_WIND -> isEnabledGroundWind
    ConfigParameter.AIR_WIND -> isEnabledAirWind
    ConfigParameter.WIND_DIRECTION -> isEnabledWindDirection
    ConfigParameter.CLOUD_COVER -> isEnabledCloudCover
    ConfigParameter.CLOUD_COVER_HIGH -> isEnabledCloudCoverHigh
    ConfigParameter.CLOUD_COVER_MEDIUM -> isEnabledCloudCoverMedium
    ConfigParameter.CLOUD_COVER_LOW -> isEnabledCloudCoverLow
    ConfigParameter.FOG -> isEnabledFog
    ConfigParameter.PRECIPITATION -> isEnabledPrecipitation
    ConfigParameter.HUMIDITY -> isEnabledHumidity
    ConfigParameter.DEW_POINT -> isEnabledDewPoint
    ConfigParameter.PROBABILITY_OF_THUNDER -> isEnabledProbabilityOfThunder
    ConfigParameter.ALTITUDE_UPPER_BOUND -> isEnabledAltitudeUpperBound
    ConfigParameter.WIND_SHEAR_SPEED -> isEnabledWindShear
    ConfigParameter.WIND_SPEED_OF_GUST -> isEnabledGroundWind
    //else -> false
}

fun ConfigProfile.thresholdsMap()
= mapOf(
    ConfigParameter.GROUND_WIND to groundWindThreshold,
    ConfigParameter.AIR_WIND to airWindThreshold,
    ConfigParameter.CLOUD_COVER to cloudCoverThreshold,
    ConfigParameter.CLOUD_COVER_HIGH to cloudCoverHighThreshold,
    ConfigParameter.CLOUD_COVER_MEDIUM to cloudCoverMediumThreshold,
    ConfigParameter.CLOUD_COVER_LOW to cloudCoverLowThreshold,
    ConfigParameter.FOG to fogThreshold,
    ConfigParameter.PRECIPITATION to precipitationThreshold,
    ConfigParameter.HUMIDITY to humidityThreshold,
    ConfigParameter.DEW_POINT to dewPointThreshold,
    ConfigParameter.PROBABILITY_OF_THUNDER to probabilityOfThunderThreshold,
    ConfigParameter.ALTITUDE_UPPER_BOUND to altitudeUpperBound,
    ConfigParameter.WIND_SHEAR_SPEED to windShearSpeedThreshold,
    ConfigParameter.WIND_SPEED_OF_GUST to groundWindThreshold
)

fun ConfigParameter.label(): String
= when (this) {
    ConfigParameter.GROUND_WIND -> "Ground Wind"
    ConfigParameter.WIND_SPEED_OF_GUST -> "Wind Speed of Gust"
    ConfigParameter.AIR_WIND -> "Air Wind"
    ConfigParameter.WIND_DIRECTION -> "Wind Direction"
    ConfigParameter.CLOUD_COVER -> "Cloud Cover"
    ConfigParameter.CLOUD_COVER_HIGH -> "High Cloud Cover"
    ConfigParameter.CLOUD_COVER_MEDIUM -> "Medium Cloud Cover"
    ConfigParameter.CLOUD_COVER_LOW -> "Low Cloud Cover"
    ConfigParameter.FOG -> "Fog"
    ConfigParameter.PRECIPITATION -> "Precipitation"
    ConfigParameter.HUMIDITY -> "Humidity"
    ConfigParameter.DEW_POINT -> "Dew Point"
    ConfigParameter.PROBABILITY_OF_THUNDER -> "Probability of Thunder"
    else -> ""
}

fun ConfigParameter.unit(): String
= when (this) {
    ConfigParameter.GROUND_WIND -> "m/s"
    ConfigParameter.WIND_SPEED_OF_GUST -> "m/s"
    ConfigParameter.AIR_WIND -> "m/s"
    ConfigParameter.WIND_DIRECTION -> "°"
    ConfigParameter.CLOUD_COVER -> "%"
    ConfigParameter.CLOUD_COVER_HIGH -> "%"
    ConfigParameter.CLOUD_COVER_MEDIUM -> "%"
    ConfigParameter.CLOUD_COVER_LOW -> "%"
    ConfigParameter.FOG -> "%"
    ConfigParameter.PRECIPITATION -> "mm"
    ConfigParameter.HUMIDITY -> "%"
    ConfigParameter.DEW_POINT -> "°C"
    ConfigParameter.PROBABILITY_OF_THUNDER -> "%"
    ConfigParameter.ALTITUDE_UPPER_BOUND -> "m"
    ConfigParameter.WIND_SHEAR_SPEED -> "m/s"
    //else -> ""
}

fun ConfigParameter.icon(): EvaluationIcon
= when (this) {
    ConfigParameter.GROUND_WIND -> EvaluationIcon.DrawableIcon(R.drawable.wind)
    ConfigParameter.WIND_SPEED_OF_GUST -> EvaluationIcon.DrawableIcon(R.drawable.wind)
    ConfigParameter.WIND_DIRECTION -> EvaluationIcon.VectorIcon(Icons.Filled.ArrowDownward)
    ConfigParameter.CLOUD_COVER -> EvaluationIcon.DrawableIcon(R.drawable.cloud_filled)
    ConfigParameter.CLOUD_COVER_HIGH -> EvaluationIcon.DrawableIcon(R.drawable.cloud_high)
    ConfigParameter.CLOUD_COVER_MEDIUM -> EvaluationIcon.DrawableIcon(R.drawable.cloud_medium)
    ConfigParameter.CLOUD_COVER_LOW -> EvaluationIcon.DrawableIcon(R.drawable.cloud_low)
    ConfigParameter.FOG -> EvaluationIcon.DrawableIcon(R.drawable.fog)
    ConfigParameter.PRECIPITATION -> EvaluationIcon.DrawableIcon(R.drawable.rain)
    ConfigParameter.HUMIDITY -> EvaluationIcon.DrawableIcon(R.drawable.humidity)
    ConfigParameter.DEW_POINT -> EvaluationIcon.DrawableIcon(R.drawable.dewpoint)
    ConfigParameter.PROBABILITY_OF_THUNDER -> EvaluationIcon.DrawableIcon(R.drawable.thunder)
    else -> EvaluationIcon.VectorIcon(Icons.Filled.ArrowDownward)
}