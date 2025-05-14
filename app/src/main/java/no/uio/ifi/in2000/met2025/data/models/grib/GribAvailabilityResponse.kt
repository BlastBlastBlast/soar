package no.uio.ifi.in2000.met2025.data.models.grib

import kotlinx.serialization.Serializable

/**
 * Response wrapper for available GRIB datasets.
 *
 * Special notes:
 * - All raw timestamp strings (e.g. `updated`, `time`) are ISO-8601 in UTC.
 *
 * @property entries each describes one endpoint with its parameters and last update.
 */
@Serializable
data class GribAvailabilityResponse(
    val entries: List<DataEntry>
)

/**
 * One entry in the availability list.
 *
 * @property endpoint specifies endpoint data format (e.g. "grib2")
 * @property params  query parameters required by this endpoint
 * @property updated ISO-8601 timestamp when this data was last refreshed
 * @property uri     full URI to fetch the dataset
 */
@Serializable
data class DataEntry(
    val endpoint: String,
    val params: Params,
    val updated: String,
    val uri: String
)

/**
 * Query parameters for a GRIB availability endpoint.
 *
 * @property area spatial area code (e.g. "NOR")
 * @property time time window for which data is valid, in ISO-8601
 */
@Serializable
data class Params(
    val area: String,
    val time: String
)

/**
 * Parsed availability data with Instant timestamps.
 *
 * @property updated when the source was last updated
 * @property availData list of areas/times and their URIs
 */
data class StructuredAvailability(
    val updated: java.time.Instant,
    val availData: List<AvailabilityData>
)

/**
 * One availability record with proper Instant type.
 *
 * @property area region code
 * @property time exact timestamp
 * @property uri  link to the GRIB file
 */
data class AvailabilityData(
    val area: String,
    val time: java.time.Instant,
    val uri: String
)
