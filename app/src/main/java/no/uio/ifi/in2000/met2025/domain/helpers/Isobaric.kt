package no.uio.ifi.in2000.met2025.domain.helpers

import no.uio.ifi.in2000.met2025.data.local.database.ConfigProfile
import no.uio.ifi.in2000.met2025.data.models.isobaric.IsobaricData


fun IsobaricData.toConfigList(config: ConfigProfile): List<Triple<Double, Double, Boolean>> {
    val relevantLayers = valuesAtLayer.keys.reversed()
        .takeLastWhile {
            valuesAtLayer[it]!!.altitude <= config.altitudeUpperBound
        }
        .map { valuesAtLayer[it]!! }

    val windThresholdsList = relevantLayers.map {
        Triple(it.windSpeed, config.airWindThreshold, config.isEnabledAirWind)
    }

    val shearThresholdsList = relevantLayers.zipWithNext { currentValues, nextValues ->
        Triple(windShearSpeed(currentValues, nextValues), config.windShearSpeedThreshold, config.isEnabledWindShear)
    }

    return (0..<(windThresholdsList.size + shearThresholdsList.size))
        .map { index ->
            if (index % 2 == 0) {
                windThresholdsList[index / 2]
            } else {
                shearThresholdsList[index / 2]
            }
        }
}