package no.uio.ifi.in2000.met2025.domain.helpers

import java.time.Instant
import java.time.ZoneId
import java.time.temporal.ChronoUnit

/**
 * Finds the closest isobaric data window before the current time.
 * The start of the isobaric data window is the the closest whole
 * hour after the current time that is a multiple of 3 hours
 */
fun Instant.closestIsobaricDataWindowBefore(): Instant {
    val zonedDateTime = this.atZone(ZoneId.of("Z")).truncatedTo(ChronoUnit.HOURS)
    val hoursMinusTwo = zonedDateTime.minusHours(2)
    val lastDivisibleHour = generateSequence(hoursMinusTwo) { it.plusHours(1) }
        .first { it.hour % 3 == 0 }
    return lastDivisibleHour.toInstant()
}