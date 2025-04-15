package no.uio.ifi.in2000.met2025.domain.helpers

import no.uio.ifi.in2000.met2025.data.local.database.ConfigProfile
import no.uio.ifi.in2000.met2025.data.models.IsobaricData

fun IsobaricData.toConfigList(config: ConfigProfile): List<Pair<Double, Double>> {
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

    return (1..<(windThresholdsList.size + shearThresholdsList.size))
        .map { index ->
            if (index % 2 == 0) {
                windThresholdsList[index / 2]
            } else {
                shearThresholdsList[index / 2]
            }
        }
}