package no.uio.ifi.in2000.met2025.domain.helpers

import no.uio.ifi.in2000.met2025.data.models.isobaric.IsobaricDataValues
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt

/**
 * Wind shear calculation helpers
 *
 * Provides functions to compute the magnitude and direction of wind shear
 * between two isobaric layers, based on their wind speed and wind-from-direction.
 *
 * Special notes:
 * - Angles are in degrees and converted to radians for trigonometric functions.
 */

/**
 * Computes the wind shear speed between two layers using the law of cosines.
 */
fun windShearSpeed(d1: IsobaricDataValues, d2: IsobaricDataValues): Double {
    val s1 = d1.windSpeed
    val s2 = d2.windSpeed
    return sqrt(s1.pow(2) + s2.pow(2) - 2 * s1 * s2 * cos((d2.windFromDirection - d1.windFromDirection) * Math.PI / 180))
}

/**
 * Computes the wind shear direction between two layers.
 *
 * Calculates the bearing of the vector difference (v2 - v1).
 */
fun windShearDirection(d1: IsobaricDataValues, d2: IsobaricDataValues): Double {
    val x1 = d1.windSpeed * cos(d1.windFromDirection)
    val y1 = d1.windSpeed * sin(d1.windFromDirection)
    val x2 = d2.windSpeed * cos(d2.windFromDirection)
    val y2 = d2.windSpeed * sin(d2.windFromDirection)
    return (atan2(y2 - y1, x2 - x1) * 180 / Math.PI)
}