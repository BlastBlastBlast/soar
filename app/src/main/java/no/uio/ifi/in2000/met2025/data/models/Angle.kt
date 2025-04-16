package no.uio.ifi.in2000.met2025.data.models

class Angle(val degrees: Double) {
    val radians: Double = degrees * Math.PI / 180

    fun Angle.plus(angle: Angle): Angle = Angle(degrees + angle.degrees)

    fun Angle.minus(angle: Angle): Angle = Angle(degrees - angle.degrees)

    fun Angle.times(factor: Double) : Angle = Angle(degrees * factor)
}