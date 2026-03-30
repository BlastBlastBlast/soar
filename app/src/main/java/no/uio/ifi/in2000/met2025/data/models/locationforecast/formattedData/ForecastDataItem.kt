package no.uio.ifi.in2000.met2025.data.models.locationforecast.formattedData

import kotlin.reflect.full.memberProperties

data class ForecastDataItem(
    val time: String,
    val values: ForecastDataValues
) {
    fun hasMissingValues(): Boolean
    = ForecastDataValues::class.memberProperties.any { it.get(values) == null }
}