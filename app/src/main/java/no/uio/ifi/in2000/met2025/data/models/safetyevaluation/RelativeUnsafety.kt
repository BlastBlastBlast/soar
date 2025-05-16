package no.uio.ifi.in2000.met2025.data.models.safetyevaluation

import android.util.Log
import no.uio.ifi.in2000.met2025.data.local.database.WeatherConfig
import no.uio.ifi.in2000.met2025.data.models.locationforecast.ForecastDataItem
import no.uio.ifi.in2000.met2025.data.models.isobaric.IsobaricData
import no.uio.ifi.in2000.met2025.domain.helpers.toConfigList

/**
 * Calculate the relative unsafety of a foreCastDataItem and isobaricData object as an aggregate value.
 * Relative unsafety is calculated as the ratio of the value to the threshold for each parameter,
 * and lies within [0, 1] when the value is within the threshold.
 * The maximum relative unsafety is returned.
 *
 * This function can be called to only evaluate a forecastDataItem or isobaricData object,
 * or both.
 */
fun relativeUnsafety(config: WeatherConfig, forecastDataItem: ForecastDataItem? = null, isobaricData: IsobaricData? = null): Double? {
    val forecastList = forecastDataItem?.toConfigList(config) ?: emptyList()
    val isobaricList = isobaricData?.toConfigList(config) ?: emptyList()
    val valueThresholdList = (forecastList + isobaricList).filter { (_, _, enabled) -> enabled }

    return relativeUnsafety(valueThresholdList)
}

/**
 * Calculate the relative unsafety of a list of values and thresholds.
 * Relative unsafety is calculated as the ratio of the value to the threshold for each parameter,
 * and lies within [0, 1] when the value is within the threshold.
 * The maximum relative unsafety is returned.
 */
fun relativeUnsafety(valueThresholdList: List<Triple<Double, Double, Boolean>>): Double?
=
    valueThresholdList
        .mapNotNull { (value, threshold, isEnabled) ->
            Log.i("RelativeUnsafety", "Value: $value, Threshold: $threshold, isEnabled: $isEnabled")
            relativeUnsafety(value, threshold) }
        .maxOrNull()

/**
 * Calculate the relative unsafety of a value and threshold.
 */
fun relativeUnsafety(value: Double?, threshold: Double): Double? {
    if (value == null) return null
    if (threshold == 0.0) {
        return if (value <= threshold) 0.0 else Double.MAX_VALUE
    }
    return value / threshold
}
