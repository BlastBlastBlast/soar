package no.uio.ifi.in2000.met2025.data.models

data class Vector3D(val x: Double, val y: Double, val z: Double) {
    operator fun plus(other: Vector3D) = Vector3D(x + other.x, y + other.y, z + other.z)
    operator fun minus(other: Vector3D) = Vector3D(x - other.x, y - other.y, z - other.z)
    operator fun times(scalar: Double) = Vector3D(x * scalar, y * scalar, z * scalar)
    operator fun times(other: Vector3D) = Vector3D(x * other.x, y * other.y, z * other.z)
    operator fun div(scalar: Double) = Vector3D(x / scalar, y / scalar, z / scalar)
    operator fun unaryMinus() = Vector3D(-x, -y, -z)

    fun dot(other: Vector3D) = x * other.x + y * other.y + z * other.z
    fun norm() = kotlin.math.sqrt(x * x + y * y + z * z)
    fun normalize() = this / norm()
}

operator fun Double.times(vector: Vector3D) = Vector3D(this * vector.x, this * vector.y, this * vector.z)
// tfw no linear algebra library :(
