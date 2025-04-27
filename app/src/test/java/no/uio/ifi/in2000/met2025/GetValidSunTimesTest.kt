package no.uio.ifi.in2000.met2025

import junit.framework.Assert.assertEquals
import kotlinx.coroutines.runBlocking
import no.uio.ifi.in2000.met2025.data.remote.sunrise.SunriseDataSource
import no.uio.ifi.in2000.met2025.data.remote.sunrise.SunriseRepository
import no.uio.ifi.in2000.met2025.domain.helpers.formatter
import org.junit.Test
import java.time.OffsetDateTime

class GetValidSunTimesTest {

    @Test
    fun getValidSunTimesTest() = runBlocking {
        val mockClient = no.uio.ifi.in2000.met2025.createSunriseMockClientWithJson()
        val sunriseDataSource = SunriseDataSource(mockClient)
        val sunriseRepository = SunriseRepository(sunriseDataSource)

        val res = sunriseRepository.getValidSunTimes(59.9, 10.7, "2025-04-25")

        val expectedString = "2025-04-25T05:31+01:00"
        val offsetExpected = OffsetDateTime.parse(expectedString, formatter)
        val expectedInstant = offsetExpected.toInstant()
        assertEquals(expectedInstant, res.earliestRocket)
    }
}