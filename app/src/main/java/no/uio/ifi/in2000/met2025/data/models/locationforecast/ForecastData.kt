package no.uio.ifi.in2000.met2025.data.models.locationforecast

data class ForecastData(
    val updatedAt: String,
    val altitude: Double,
    val timeSeries: List<ForecastDataItem>
)

data class ForecastDataItem(
    val time: String,
    val values: ForecastDataValues
)

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


// ein mindre nøsta struktur enn responsdataen
// også uten enkelte verdiar frå responsen som eg ikkje turte å ta vekk, f.eks type

// kanskje legge til fleire variablar