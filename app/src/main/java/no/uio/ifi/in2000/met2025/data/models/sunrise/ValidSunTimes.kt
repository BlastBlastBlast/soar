package no.uio.ifi.in2000.met2025.data.models.sunrise

import java.time.Instant

/**
 * ValidSunTimes
 * Sunrise and sunset times for a given location.
 */
data class ValidSunTimes(
    val sunrise: Instant,
    val sunset: Instant,
    val earliestRocket: Instant,
    val latestRocket: Instant
)
