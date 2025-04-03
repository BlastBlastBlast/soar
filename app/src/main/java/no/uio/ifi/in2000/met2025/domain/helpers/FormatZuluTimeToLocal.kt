package no.uio.ifi.in2000.met2025.domain.helpers

import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

fun formatZuluTimeToLocal(zuluTime: String): String {
    // Parse the ISO date‑time string (Zulu/UTC format)
    val zonedDateTime = ZonedDateTime.parse(zuluTime)
    // Convert the time to the system default timezone (or specify ZoneId.of("Europe/Oslo"))
    val localTime = zonedDateTime.withZoneSameInstant(ZoneId.systemDefault())
    // Format as 24‑h time
    return localTime.format(DateTimeFormatter.ofPattern("HH:mm"))
}

fun formatZuluTimeToLocalDate(zuluTime: String): String {
    val zonedDateTime = ZonedDateTime.parse(zuluTime)
    val localTime = zonedDateTime.withZoneSameInstant(ZoneId.of("Europe/Oslo"))
    return localTime.format(DateTimeFormatter.ofPattern("EEE", Locale.ENGLISH))
}

fun formatZuluTimeToLocalTime(zuluTime: String): String {
    val zonedDateTime = ZonedDateTime.parse(zuluTime)
    val localTime = zonedDateTime.withZoneSameInstant(ZoneId.of("Europe/Oslo"))
    return localTime.format(DateTimeFormatter.ofPattern("HH:mm"))
}