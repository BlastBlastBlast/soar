package no.uio.ifi.in2000.met2025.data.models.locationforecast.apiResponse

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Metadata about this forecast feed.
 *
 * @property updatedAt ISO-8601 UTC timestamp when the feed was last refreshed
 * @property units     mapping from parameter names to their unit symbols
 */
@Serializable
data class Meta(
    @SerialName("updated_at")               val updatedAt: String,  //f.eks "2025-03-15T16:26:43Z"
    val units: Units
)