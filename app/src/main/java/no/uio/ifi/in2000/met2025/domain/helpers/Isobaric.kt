package no.uio.ifi.in2000.met2025.domain.helpers

import no.uio.ifi.in2000.met2025.data.local.database.WeatherConfig
import no.uio.ifi.in2000.met2025.data.models.isobaric.IsobaricData

/**
 * Converts raw isobaric measurements into a list of threshold-check triples.
 *
 * For each layer up to the configured altitude upper bound, produces:
 * - a Triple(windSpeed, airWindThreshold, isEnabledAirWind)
 * - a Triple(windShearBetweenThisAndNextLayer, windShearSpeedThreshold, isEnabledWindShear)
 *
 * The resulting list alternates wind and shear entries in order of increasing altitude.
 */
fun IsobaricData.toConfigList(config: WeatherConfig): List<Triple<Double, Double, Boolean>> {
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