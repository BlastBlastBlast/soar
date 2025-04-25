package no.uio.ifi.in2000.met2025

import kotlinx.coroutines.runBlocking
import no.uio.ifi.in2000.met2025.data.remote.sunrise.SunriseDataSource
import no.uio.ifi.in2000.met2025.data.remote.sunrise.SunriseRepository
import org.junit.Test
import java.time.Instant
import kotlin.test.assertEquals

class GetValidSunTimesTest {

    @Test
    fun `test getValidSunTimes`() = runBlocking {
        val mockClient = createMockClientWithJson()
        val sunriseDataSource = SunriseDataSource(mockClient)
        val sunriseRepository = SunriseRepository(sunriseDataSource)

        val res = sunriseRepository.getValidSunTimes(59.9, 10.7, "2025-04-25")

        val expectedString = "2025-04-25T05:31+01:00"
        val expectedInstant = Instant.parse(expectedString)
        assertEquals(expectedInstant, res.earliestRocket)
    }
}