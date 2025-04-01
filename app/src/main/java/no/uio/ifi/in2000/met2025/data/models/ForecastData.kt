package no.uio.ifi.in2000.met2025.data.models

import kotlinx.serialization.SerialName

data class ForecastData(
    val updatedAt: String,
    val timeSeries: List<ForecastDataItem>
)

data class ForecastDataItem(
    val time: String,
    val values: ForecastDataValues
)

data class ForecastDataValues(
    val airTemperature: Double,
    val relativeHumidity: Double,
    val windSpeed: Double,
    val windSpeedOfGust: Double,
    val windFromDirection: Double,
    val fogAreaFraction: Double,
    val dewPointTemperature: Double,
    val cloudAreaFraction: Double,
    val cloudAreaFractionHigh: Double,
    val cloudAreaFractionLow: Double,
    val cloudAreaFractionMedium: Double,
    val precipitationAmount: Double,
    val probabilityOfThunder: Double
)


// ein mindre nøsta struktur enn responsdataen
// også uten enkelte verdiar frå responsen som eg ikkje turte å ta vekk, f.eks type

// kanskje legge til fleire variablar