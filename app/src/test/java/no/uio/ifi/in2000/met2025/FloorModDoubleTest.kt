package no.uio.ifi.in2000.met2025



import org.junit.Assert.assertEquals
import org.junit.Test
import no.uio.ifi.in2000.met2025.domain.helpers.floorModDouble


class FloorModDoubleTest {

    @Test
    fun testFloorModDoubleWithDoubleModulus() {
        // Eksempel: 10.5 mod 3.0 = 1.5
        val result = 10.5.floorModDouble(3.0)
        assertEquals(1.5, result, 0.0001)
    }

    @Test
    fun testFloorModDoubleWithNegativeInputDoubleModulus() {
        // Eksempel: -2.5 mod 3.0 = 0.5
        val result = (-2.5).floorModDouble(3.0)
        assertEquals(0.5, result, 0.0001)
    }

    @Test
    fun testFloorModDoubleWithIntModulus() {
        // Eksempel: 10.5 mod 4 = 2.5
        val result = 10.5.floorModDouble(4)
        assertEquals(2.5, result, 0.0001)
    }

    @Test
    fun testFloorModDoubleWithNegativeInputIntModulus() {
        // Eksempel: -2.5 mod 4 = 1.5
        val result = (-2.5).floorModDouble(4)
        assertEquals(1.5, result, 0.0001)
    }
}
