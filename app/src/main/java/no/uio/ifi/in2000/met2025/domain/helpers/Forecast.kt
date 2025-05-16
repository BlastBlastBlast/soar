package no.uio.ifi.in2000.met2025.domain.helpers

import no.uio.ifi.in2000.met2025.data.local.database.WeatherConfig
import no.uio.ifi.in2000.met2025.data.models.ConfigParameter
import no.uio.ifi.in2000.met2025.data.models.locationforecast.ForecastDataItem
import no.uio.ifi.in2000.met2025.data.models.locationforecast.ForecastDataValues
import kotlin.reflect.KProperty1
import kotlin.Triple

fun ForecastDataItem.toConfigList(config: WeatherConfig): List<Triple<Double, Double, Boolean>> {
    return listOf(
        Triple(values.windSpeed, config.groundWindThreshold, config.isEnabledGroundWind),
        Triple(values.windSpeedOfGust, config.groundWindThreshold, config.isEnabledGroundWind),
        Triple(values.windFromDirection, 0.0, false),
        Triple(values.cloudAreaFraction, config.cloudCoverThreshold, config.isEnabledCloudCover),
        Triple(values.cloudAreaFractionHigh, config.cloudCoverHighThreshold, config.isEnabledCloudCoverHigh),
        Triple(values.cloudAreaFractionMedium, config.cloudCoverMediumThreshold, config.isEnabledCloudCoverMedium),
        Triple(values.cloudAreaFractionLow, config.cloudCoverLowThreshold, config.isEnabledCloudCoverLow),
        Triple(values.fogAreaFraction, config.fogThreshold, config.isEnabledFog),
        Triple(values.precipitationAmount, config.precipitationThreshold, config.isEnabledPrecipitation),
        Triple(values.relativeHumidity, config.humidityThreshold, config.isEnabledHumidity),
        Triple(values.dewPointTemperature, config.dewPointThreshold, config.isEnabledDewPoint),
        Triple(values.probabilityOfThunder, config.probabilityOfThunderThreshold, config.isEnabledProbabilityOfThunder)
    ).filter { it.first != null }
        .map { Triple(it.first!!, it.second, it.third) } // map back to Triple if filtering
}

fun ForecastDataItem.valueAt(parameter: ConfigParameter): Double? {
    return when (parameter) {
        ConfigParameter.GROUND_WIND -> values.windSpeed
        ConfigParameter.WIND_SPEED_OF_GUST -> values.windSpeedOfGust
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
        else -> null
    }
}

fun ForecastDataItem.isEnabledAt(parameter: ConfigParameter, config: WeatherConfig): Boolean {
    return when (parameter) {
        ConfigParameter.GROUND_WIND -> config.isEnabledGroundWind
        ConfigParameter.WIND_SPEED_OF_GUST -> config.isEnabledGroundWind
        ConfigParameter.WIND_DIRECTION -> config.isEnabledWindDirection
        ConfigParameter.CLOUD_COVER -> config.isEnabledCloudCover
        ConfigParameter.CLOUD_COVER_HIGH -> config.isEnabledCloudCoverHigh
        ConfigParameter.CLOUD_COVER_MEDIUM -> config.isEnabledCloudCoverMedium
        ConfigParameter.CLOUD_COVER_LOW -> config.isEnabledCloudCoverLow
        ConfigParameter.FOG -> config.isEnabledFog
        ConfigParameter.PRECIPITATION -> config.isEnabledPrecipitation
        ConfigParameter.HUMIDITY -> config.isEnabledHumidity
        ConfigParameter.DEW_POINT -> config.isEnabledDewPoint
        ConfigParameter.PROBABILITY_OF_THUNDER -> config.isEnabledProbabilityOfThunder
        else -> false
    }
}

fun KProperty1<ForecastDataValues, *>.toConfigParameter(): ConfigParameter? {
    return when (this.name) {
        "airPressureAtSeaLevel" -> null // Not used
        "airTemperature" -> null // Not used
        "windSpeed" -> ConfigParameter.GROUND_WIND
        "windSpeedOfGust" -> ConfigParameter.WIND_SPEED_OF_GUST
        "windFromDirection" -> ConfigParameter.WIND_DIRECTION
        "cloudAreaFraction" -> ConfigParameter.CLOUD_COVER
        "cloudAreaFractionHigh" -> ConfigParameter.CLOUD_COVER_HIGH
        "cloudAreaFractionMedium" -> ConfigParameter.CLOUD_COVER_MEDIUM
        "cloudAreaFractionLow" -> ConfigParameter.CLOUD_COVER_LOW
        "fogAreaFraction" -> ConfigParameter.FOG
        "precipitationAmount" -> ConfigParameter.PRECIPITATION
        "relativeHumidity" -> ConfigParameter.HUMIDITY
        "dewPointTemperature" -> ConfigParameter.DEW_POINT
        "probabilityOfThunder" -> ConfigParameter.PROBABILITY_OF_THUNDER
        else -> null
    }
}

fun KProperty1<ForecastDataValues, *>.threshold(config: WeatherConfig): Double {
    return when (this.name) {
        "airPressureAtSeaLevel" -> 0.0 // Not used
        "airTemperature" -> 0.0 // Not used
        "windSpeed" -> config.groundWindThreshold
        "windSpeedOfGust" -> config.groundWindThreshold
        "windFromDirection" -> 0.0
        "cloudAreaFraction" -> config.cloudCoverThreshold
        "cloudAreaFractionHigh" -> config.cloudCoverHighThreshold
        "cloudAreaFractionMedium" -> config.cloudCoverMediumThreshold
        "cloudAreaFractionLow" -> config.cloudCoverLowThreshold
        "fogAreaFraction" -> config.fogThreshold
        "precipitationAmount" -> config.precipitationThreshold
        "relativeHumidity" -> config.humidityThreshold
        "dewPointTemperature" -> config.dewPointThreshold
        "probabilityOfThunder" -> config.probabilityOfThunderThreshold
        else -> 0.0 // Default value for unknown properties
    }
}

fun KProperty1<ForecastDataItem, *>.isEnabled(config: WeatherConfig): Boolean {
    return when (this.name) {
        "airPressureAtSeaLevel" -> false // Not used
        "airTemperature" -> false // Not used
        "windSpeed" -> config.isEnabledGroundWind
        "windSpeedOfGust" -> config.isEnabledGroundWind
        "windFromDirection" -> config.isEnabledWindDirection
        "cloudAreaFraction" -> config.isEnabledCloudCover
        "cloudAreaFractionHigh" -> config.isEnabledCloudCoverHigh
        "cloudAreaFractionMedium" -> config.isEnabledCloudCoverMedium
        "cloudAreaFractionLow" -> config.isEnabledCloudCoverLow
        "fogAreaFraction" -> config.isEnabledFog
        "precipitationAmount" -> config.isEnabledPrecipitation
        "relativeHumidity" -> config.isEnabledHumidity
        "dewPointTemperature" -> config.isEnabledDewPoint
        "probabilityOfThunder" -> config.isEnabledProbabilityOfThunder
        else -> false // Default value for unknown properties
    }
}