package no.uio.ifi.in2000.met2025.data.models

data class CartesianIsobaricValues(
    val pressure: Double,
    val altitude: Double,
    val temperature: Double,
    val windXComponent: Double,
    val windYComponent: Double,
)