package no.uio.ifi.in2000.met2025.data.models.locationforecast.formattedData

import java.time.Duration
import java.time.Instant

data class ForecastData(
    val updatedAt: String,
    val altitude: Double,
    val timeSeries: List<ForecastDataItem>
) {
    companion object {
        const val EFFECTIVE_WINDOW_DURATION_HOURS = 1
    }

    /**
     * Checks if data for the given time is the latest.
     * Returns true if newer data is not available yet, false otherwise
     */
    fun isLatest() = (Duration.between(Instant.parse(updatedAt), Instant.now()).toHours() <= EFFECTIVE_WINDOW_DURATION_HOURS)
}

