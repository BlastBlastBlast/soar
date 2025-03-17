package no.uio.ifi.in2000.met2025.data.models

data class ForecastData(
    val updatedAt: String,
    val timeSeries: List<ForecastDataItem>
)

data class ForecastDataItem(
    val time: String,
    val values: Details
)

// ein mindre nøsta struktur enn responsdataen
// også uten enkelte verdiar frå responsen som eg ikkje turte å ta vekk, f.eks type

// kanskje legge til fleire variablar