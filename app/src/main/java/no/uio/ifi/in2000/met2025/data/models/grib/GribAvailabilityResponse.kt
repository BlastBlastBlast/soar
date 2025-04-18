package no.uio.ifi.in2000.met2025.data.models.grib

import kotlinx.serialization.Serializable

@Serializable
data class GribAvailabilityResponse (
    val entries: List<DataEntry>
)

@Serializable
data class DataEntry (
    val endpoint: String,
    val params: Params,
    val updated: String,
    val uri: String
)

@Serializable
data class Params (
    val area: String,
    val time: String
)

data class StructuredAvailability(
    val updated: java.time.Instant,
    val availData: List<AvailabilityData>
)

data class AvailabilityData(
    val area: String,
    val time: java.time.Instant,
    val uri: String
)