package no.uio.ifi.in2000.met2025.domain.helpers

import kotlin.math.round
import kotlin.math.roundToInt

fun Double.roundToPointXFive(): Double {
    return ((this * 10).toInt() / 10) +0.05
}