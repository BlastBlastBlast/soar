package no.uio.ifi.in2000.met2025.domain.helpers

fun Double.floorModDouble(modulus: Double): Double {
    val division = this / modulus
    val floorDivision = kotlin.math.floor(division)
    return this - (floorDivision * modulus)
}

fun Double.floorModDouble(modulus: Int): Double {
    val division = this / modulus
    val floorDivision = kotlin.math.floor(division)
    return this - (floorDivision * modulus)
}