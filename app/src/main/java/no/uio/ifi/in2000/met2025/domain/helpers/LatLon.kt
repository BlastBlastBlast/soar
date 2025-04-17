package no.uio.ifi.in2000.met2025.domain.helpers

// Simple data holder
data class LatLon(val lat: Double, val lon: Double)

/** Tries to parse a variety of coordinate formats into a LatLon. */
fun parseLatLon(raw: String): LatLon? {
    val s = raw.trim()

    // 1) Google Maps URL segment: "@lat,lon,zoom"
    val afterAt = s.substringAfter('@', s)
    val gmParts = afterAt.split(',').map { it.trim() }
    if (gmParts.size >= 2) {
        val lat = gmParts[0].toDoubleOrNull()
        val lon = gmParts[1].toDoubleOrNull()
        if (lat != null && lon != null) {
            return LatLon(lat, lon)
        }
    }

    // 2) Simple decimal pair with various separators
    val decParts = s
        .split(Regex("[,;:/|]"))
        .map { it.trim() }
        .filter { it.isNotEmpty() }
    if (decParts.size >= 2) {
        val lat = decParts[0].toDoubleOrNull()
        val lon = decParts[1].toDoubleOrNull()
        if (lat != null && lon != null) {
            return LatLon(lat, lon)
        }
    }

    // 3) DMS (e.g. 59°55'03.1"N 10°37'35.4"E)
    val latDmsRegex = Regex("""(\d{1,3})°\s*([0-5]?\d)'(?:\s*([0-5]?\d(?:\.\d+)?))?"?\s*([NS])""", RegexOption.IGNORE_CASE)
    val lonDmsRegex = Regex("""(\d{1,3})°\s*([0-5]?\d)'(?:\s*([0-5]?\d(?:\.\d+)?))?"?\s*([EW])""", RegexOption.IGNORE_CASE)

    val latMatch = latDmsRegex.find(s)
    val lonMatch = lonDmsRegex.find(s)
    if (latMatch != null && lonMatch != null) {
        val (dDeg, dMin, dSec, dHem) = latMatch.destructured
        val (mDeg, mMin, mSec, mHem) = lonMatch.destructured

        fun toDecimal(deg: String, min: String, sec: String): Double =
            deg.toDouble() + min.toDouble() / 60.0 + sec.toDouble() / 3600.0

        val latVal = toDecimal(dDeg, dMin, if (dSec.isBlank()) "0" else dSec)
        val lonVal = toDecimal(mDeg, mMin, if (mSec.isBlank()) "0" else mSec)

        val finalLat = if (dHem.equals("S", true)) -latVal else latVal
        val finalLon = if (mHem.equals("W", true)) -lonVal else lonVal

        return LatLon(finalLat, finalLon)
    }

    return null
}