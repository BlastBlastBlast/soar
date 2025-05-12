package no.uio.ifi.in2000.met2025.data.models.sunrise

import java.time.Instant

data class ValidSunTimes(
    val sunrise: Instant,
    val sunset: Instant,
    val earliestRocket: Instant,
    val latestRocket: Instant
)
