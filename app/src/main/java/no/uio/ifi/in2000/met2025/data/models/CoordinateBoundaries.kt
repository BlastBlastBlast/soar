package no.uio.ifi.in2000.met2025.data.models

object CoordinateBoundaries {
    private const val MIN_LATITUDE = 55.35
    private const val MAX_LATITUDE = 64.25
    private const val MIN_LONGITUDE = -1.45
    private const val MAX_LONGITUDE = 14.51

    fun isWithinBounds(lat: Double, lon: Double): Boolean {
        return lat in MIN_LATITUDE..MAX_LATITUDE && lon in MIN_LONGITUDE..MAX_LONGITUDE
    }
}