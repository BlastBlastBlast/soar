package no.uio.ifi.in2000.met2025.data.models

class Angle(val degrees: Double) {
    val radians: Double = degrees * Math.PI / 180

    operator fun plus(angle: Angle): Angle = Angle(degrees + angle.degrees)

    operator fun minus(angle: Angle): Angle = Angle(degrees - angle.degrees)

    operator fun times(factor: Double) : Angle = Angle(degrees * factor)
}

fun cos(angle: Angle): Double = kotlin.math.cos(angle.radians)
fun sin(angle: Angle): Double = kotlin.math.sin(angle.radians)
fun tan(angle: Angle): Double = kotlin.math.tan(angle.radians)