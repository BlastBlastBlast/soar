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

fun relativeUnsafety(forecastDataItem: ForecastDataItem, config: ConfigProfile): Double? {
    val valueThresholdList = forecastDataItem.toConfigList(config)
    return relativeUnsafety(valueThresholdList)
}

fun relativeUnsafety(isobaricData: IsobaricData, config: ConfigProfile): Double? {
    val valuesAtLayer = isobaricData.valuesAtLayer

    val relevantLayers = valuesAtLayer.keys.reversed()
        .takeLastWhile {
            valuesAtLayer[it]!!.altitude <= config.altitudeUpperBound
        }
        .map { valuesAtLayer[it]!! }

    val windThresholdsList = relevantLayers.map {
        Pair(it.windSpeed, config.airWindThreshold)
    }

    val shearThresholdsList = relevantLayers.zipWithNext { currentValues, nextValues ->
        Pair(windShearSpeed(currentValues, nextValues), config.windShearSpeedThreshold)
    }

    val valueThresholdList = (1..<(windThresholdsList.size + shearThresholdsList.size))
        .map { index ->
            if (index % 2 == 0) {
                windThresholdsList[index / 2]
            } else {
                shearThresholdsList[index / 2]
            }
        }

    return relativeUnsafety(valueThresholdList)
}

fun relativeUnsafety(valueThresholdList: List<Pair<Double, Double>>): Double?
        = valueThresholdList
    .mapNotNull { (value, threshold) ->
        println("Value: $value, Threshold: $threshold")
        relativeUnsafety(value, threshold) }
    .maxOrNull()
