package no.uio.ifi.in2000.met2025.domain.helpers

import kotlin.math.round

fun Double.roundToPointXFive(): Double {
    return (Math.round(this * 20) / 20.0).let {
        if (it % 0.10 == 0.0) it + 0.05 else it
    }
}