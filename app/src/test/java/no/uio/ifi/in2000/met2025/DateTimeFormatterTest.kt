package no.uio.ifi.in2000.met2025

import org.junit.Test
import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatterBuilder
import java.time.temporal.ChronoField
import java.time.format.DateTimeFormatter
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class DateTimeFormatterTest {

    private val formatter: DateTimeFormatter = DateTimeFormatterBuilder()
        .appendPattern("yyyy-MM-dd'T'HH:mm")
        .appendPattern("XXX")
        .parseDefaulting(ChronoField.SECOND_OF_MINUTE, 0)
        .toFormatter()

    @Test
    fun parse_validDateTime_returnsCorrectOffsetDateTime() {
        val input = "2025-05-14T12:30+01:00"
        val result = OffsetDateTime.parse(input, formatter)

        val expected = OffsetDateTime.of(
            LocalDateTime.of(2025, 5, 14, 12, 30, 0),
            ZoneOffset.of("+01:00")
        )

        assertEquals(expected, result)
    }

    @Test
    fun parse_missingSeconds_setsSecondsToZero() {
        val input = "2025-05-14T23:59+00:00"
        val result = OffsetDateTime.parse(input, formatter)

        assertEquals(0, result.second)
    }

    @Test
    fun parse_invalidFormat_throwsException() {
        val invalidInput = "14-05-2025 12:30"
        assertFailsWith<Exception> {
            OffsetDateTime.parse(invalidInput, formatter)
        }
    }
}