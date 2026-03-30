package no.uio.ifi.in2000.met2025.data.models.isobaric

import java.time.Duration
import java.time.Instant
import java.time.ZoneId
import java.time.temporal.ChronoUnit

/**
 * IsobaricData
 * A set of calculated IsobaricData values for a specific time.
 */
data class IsobaricData(
    val time: String,
    val valuesAtLayer: Map<Int, IsobaricDataValues>
) {
    companion object {
        const val EFFECTIVE_WINDOW_DURATION_HOURS = 3

        /**
         * Checks if data for the given time is the latest.
         * Returns true if newer data is not available yet, false otherwise
         */
        fun isLatest(time: Instant) = (Duration.between(time, Instant.now()).toHours() <= EFFECTIVE_WINDOW_DURATION_HOURS)

        /**
         * Finds the closest isobaric data window before the current time.
         * The start of the isobaric data window is the closest whole
         * hour after the current time that is a multiple of 3 hours
         */
        fun effectiveWindowFor(time: Instant): Instant {
            val zonedDateTime = time.atZone(ZoneId.of("Z")).truncatedTo(ChronoUnit.HOURS)
            val hoursMinusTwo = zonedDateTime.minusHours(2)
            val lastDivisibleHour = generateSequence(hoursMinusTwo) { it.plusHours(1) }
                .first { it.hour % 3 == 0 }
            return lastDivisibleHour.toInstant()
        }
    }
}
