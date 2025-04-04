package no.uio.ifi.in2000.met2025.domain.helpers

import java.time.Instant
import java.time.ZoneId
import java.time.temporal.ChronoUnit

fun Instant.startOfIsobaricDataWindow(): Instant {
    // The start of the isobaric data window is the the closest whole hour before the current time that is a multiple of 3 hours
    val zonedDateTime = this.atZone(ZoneId.of("Z")).truncatedTo(ChronoUnit.HOURS)
    val lastDivisibleHour = generateSequence(zonedDateTime) { it.minusHours(1) }
        .first { it.hour % 3 == 0 }
    return lastDivisibleHour.toInstant()
}