package no.uio.ifi.in2000.met2025

import no.uio.ifi.in2000.met2025.domain.helpers.formatZuluTimeToLocalDate
import org.junit.Test
import kotlin.test.assertEquals

/**
 * Unit test for the formatZuluTimeToLocalDate helper function.
 * This test ensures that a UTC timestamp (Zulu time) is correctly
 * formatted
 */

class TimeFormatTest {
    @Test
    fun testFormatZuluTimeToLocalDate() {
        val input = "2025-05-15T08:00:00Z"
        val result = formatZuluTimeToLocalDate(input)

        assertEquals("Thu", result)

    }
}