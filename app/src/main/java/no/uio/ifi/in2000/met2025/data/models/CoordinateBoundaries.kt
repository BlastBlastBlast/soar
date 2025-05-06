package no.uio.ifi.in2000.met2025.data.models

object CoordinateBoundaries {
    const val MIN_LATITUDE = 55.35
    const val MAX_LATITUDE = 64.25
    const val MIN_LONGITUDE = -1.45
    const val MAX_LONGITUDE = 14.51
    const val RESOLUTION = 10.0

    fun isWithinBounds(lat: Double, lon: Double): Boolean {
        return lat in MIN_LATITUDE..MAX_LATITUDE && lon in MIN_LONGITUDE..MAX_LONGITUDE
    }
}