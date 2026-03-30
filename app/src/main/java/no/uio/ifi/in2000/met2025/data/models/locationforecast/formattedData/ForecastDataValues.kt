package no.uio.ifi.in2000.met2025.data.models.locationforecast.formattedData

data class ForecastDataValues(
    val airPressureAtSeaLevel: Double,
    val airTemperature: Double,
    val relativeHumidity: Double,
    val windSpeed: Double,
    val windSpeedOfGust: Double?,
    val windFromDirection: Double,
    val fogAreaFraction: Double?,
    val dewPointTemperature: Double?,
    val cloudAreaFraction: Double,
    val cloudAreaFractionHigh: Double,
    val cloudAreaFractionLow: Double,
    val cloudAreaFractionMedium: Double,
    val precipitationAmount: Double?,
    val probabilityOfThunder: Double?,
    val symbolCode: String? = null
)