package no.uio.ifi.in2000.met2025.data.models.safetyevaluation

import no.uio.ifi.in2000.met2025.data.local.database.ConfigProfile
import no.uio.ifi.in2000.met2025.data.models.locationforecast.ForecastDataItem
import no.uio.ifi.in2000.met2025.data.models.isobaric.IsobaricData
import no.uio.ifi.in2000.met2025.domain.helpers.toConfigList

fun relativeUnsafety(value: Double?, threshold: Double): Double? {
    if (value == null) return null
    if (threshold == 0.0) {
        return if (value <= threshold) 0.0 else Double.MAX_VALUE
    }
    return value / threshold
}

fun relativeUnsafety(config: ConfigProfile, forecastDataItem: ForecastDataItem? = null, isobaricData: IsobaricData? = null): Double? {
    val forecastList = forecastDataItem?.toConfigList(config) ?: emptyList()
    val isobaricList = isobaricData?.toConfigList(config) ?: emptyList()
    val valueThresholdList = (forecastList + isobaricList).filter { (_, _, enabled) -> enabled }

    return relativeUnsafety(valueThresholdList)
}

fun relativeUnsafety(valueThresholdList: List<Triple<Double, Double, Boolean>>): Double?
=
    valueThresholdList
        .mapNotNull { (value, threshold, isEnabled) ->
            println("Value: $value, Threshold: $threshold, isEnabled: $isEnabled")
            relativeUnsafety(value, threshold) }
        .maxOrNull()
