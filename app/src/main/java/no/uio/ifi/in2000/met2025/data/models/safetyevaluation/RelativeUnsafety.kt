package no.uio.ifi.in2000.met2025.data.models.safetyevaluation

import no.uio.ifi.in2000.met2025.data.local.database.ConfigProfile
import no.uio.ifi.in2000.met2025.data.models.ForecastDataItem
import no.uio.ifi.in2000.met2025.data.models.IsobaricData
import no.uio.ifi.in2000.met2025.domain.helpers.toConfigList
import no.uio.ifi.in2000.met2025.domain.helpers.windShearSpeed

fun relativeUnsafety(value: Double?, threshold: Double): Double? {
    if (value == null) return null
    if (threshold == 0.0) {
        return if (value < threshold) 0.0 else Double.MAX_VALUE
    }
    return value / threshold
}

fun relativeUnsafety(config: ConfigProfile, forecastDataItem: ForecastDataItem? = null, isobaricData: IsobaricData? = null): Double? {
    val forecastList = forecastDataItem?.toConfigList(config) ?: emptyList()
    val isobaricList = isobaricData?.toConfigList(config) ?: emptyList()
    val valueThresholdList = forecastList + isobaricList
    return relativeUnsafety(valueThresholdList)
}

fun relativeUnsafety(valueThresholdList: List<Pair<Double, Double>>): Double?
=
    valueThresholdList
        .mapNotNull { (value, threshold) ->
            println("Value: $value, Threshold: $threshold")
            relativeUnsafety(value, threshold) }
        .maxOrNull()
