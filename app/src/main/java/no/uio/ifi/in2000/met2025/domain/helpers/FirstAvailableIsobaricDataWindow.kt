package no.uio.ifi.in2000.met2025.domain.helpers

import java.time.Instant
import java.time.ZoneId
import java.time.temporal.ChronoUnit

fun Instant.firstAvailableIsobaricDataWindowBefore(): Instant {
    // The start of the isobaric data window is the closest whole hour after the current time that is a multiple of 3 hours
    val zonedDateTime = this.atZone(ZoneId.of("Z")).truncatedTo(ChronoUnit.HOURS)
    val hoursMinusSix = zonedDateTime.minusHours(8)
    val firstDivisibleHour = generateSequence(hoursMinusSix) { it.plusHours(1) }
        .first { it.hour % 3 == 0 }
    return firstDivisibleHour.toInstant()
}