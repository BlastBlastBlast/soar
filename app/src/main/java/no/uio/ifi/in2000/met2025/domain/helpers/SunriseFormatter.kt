package no.uio.ifi.in2000.met2025.domain.helpers

import java.time.format.DateTimeFormatter
import java.time.format.DateTimeFormatterBuilder
import java.time.temporal.ChronoField

val formatter: DateTimeFormatter = DateTimeFormatterBuilder()
    .appendPattern("yyyy-MM-dd'T'HH:mm")
    .appendPattern("XXX")
    .parseDefaulting(ChronoField.SECOND_OF_MINUTE, 0)
    .toFormatter()