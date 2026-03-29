package no.uio.ifi.in2000.met2025.data.models

import java.time.Instant

data class Timestamped<T>(
    val data: T,
    val time: Instant
)
