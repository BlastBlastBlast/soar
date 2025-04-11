package no.uio.ifi.in2000.met2025.domain.helpers

import no.uio.ifi.in2000.met2025.data.local.database.ConfigProfile
import no.uio.ifi.in2000.met2025.data.models.ConfigParameter
import no.uio.ifi.in2000.met2025.data.models.ForecastDataItem

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