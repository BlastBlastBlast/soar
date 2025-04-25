package no.uio.ifi.in2000.met2025.data.models.grib

data class GribDataMap(
    val time : String,
    val map : Map<Pair<Double, Double>, Map<Int, GribVectors>>
)

data class GribVectors(
    val temperature: Float,
    val uComponentWind: Float,
    val vComponentWind: Float
)
