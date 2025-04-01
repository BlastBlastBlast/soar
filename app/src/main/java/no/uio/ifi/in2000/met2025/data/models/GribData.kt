package no.uio.ifi.in2000.met2025.data.models

data class GribVectors(
    //val temperature: Float,
    val uComponentWind: Float,
    val vComponentWind: Float
)

typealias GribDataMap = Map<Pair<Double, Double>, Map<Int, GribVectors>>