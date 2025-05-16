package no.uio.ifi.in2000.met2025.domain.helpers

import java.math.BigDecimal
import java.math.RoundingMode

fun roundDoubleToXDecimals(value: Double, x: Int) : Double {
    return BigDecimal(value).setScale(x, RoundingMode.HALF_UP).toDouble()
}

fun roundFloatToXDecimalsDouble(value: Float, x: Int) : Double {
    return BigDecimal(value.toDouble()).setScale(x, RoundingMode.HALF_UP).toDouble()
}

fun Double.roundToDecimals(n: Int) : Double {
    return this.toBigDecimal().setScale(n, RoundingMode.HALF_UP).toDouble()
}