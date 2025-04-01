package no.uio.ifi.in2000.met2025.domain.helpers

import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

fun formatZuluTimeToLocal(zuluTime: String): String {
    // Parse the ISO date‑time string (Zulu/UTC format)
    val zonedDateTime = ZonedDateTime.parse(zuluTime)
    // Convert the time to the system default timezone (or specify ZoneId.of("Europe/Oslo"))
    val localTime = zonedDateTime.withZoneSameInstant(ZoneId.systemDefault())
    // Format as 24‑h time
    return localTime.format(DateTimeFormatter.ofPattern("HH:mm"))
}