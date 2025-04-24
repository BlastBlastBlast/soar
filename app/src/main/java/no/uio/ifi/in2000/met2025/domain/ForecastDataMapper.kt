package no.uio.ifi.in2000.met2025.domain

import no.uio.ifi.in2000.met2025.data.models.ForecastData
import javax.inject.Inject

//// TODO: Put this business logic in Repo
//
//class ForecastDataMapper @Inject constructor() {
//    fun mapForecastDataToDisplayData(forecastData: ForecastData): WeatherCardViewmodel.ForecastDisplayData {
//
//        val temperatures = forecastData.timeSeries.associate { it.time to it.values.airTemperature }
//        val humidities = forecastData.timeSeries.associate { it.time to it.values.relativeHumidity }
//        val windSpeeds = forecastData.timeSeries.associate { it.time to it.values.windSpeed }
//        val windGusts = forecastData.timeSeries.associate { it.time to it.values.windSpeedOfGust }
//        val windDirections = forecastData.timeSeries.associate { it.time to it.values.windFromDirection }
//        val precipitations = forecastData.timeSeries.associate { it.time to it.values.precipitationAmount }
//        val visibilities = forecastData.timeSeries.associate { it.time to (100.0 * (1 - it.values.fogAreaFraction)) }
//        val dewPoints = forecastData.timeSeries.associate { it.time to it.values.dewPointTemperature }
//        val cloudCovers = forecastData.timeSeries.associate { it.time to it.values.cloudAreaFraction }
//        val thunderProbabilities = forecastData.timeSeries.associate { it.time to it.values.probabilityOfThunder }
//
//        return WeatherCardViewmodel.ForecastDisplayData(
//            temperatures = temperatures,
//            humidities = humidities,
//            windSpeeds = windSpeeds,
//            windGusts = windGusts,
//            windDirections = windDirections,
//            precipitations = precipitations,
//            visibilities = visibilities,
//            dewPoints = dewPoints,
//            cloudCovers = cloudCovers,
//            thunderProbabilities = thunderProbabilities
//        )
//    }
//}