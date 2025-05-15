package no.uio.ifi.in2000.met2025.ui.screens.mapScreen.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.time.Instant
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun LaunchWindowPickerDialog(
    showDialog: Boolean,
    getAvailabilityLatestTime: () -> Instant,
    onDismiss: () -> Unit,
    onConfirm: (Instant) -> Unit
) {
    if (!showDialog) return

    val osloZone = ZoneId.of("Europe/Oslo")
    // Round "now" down to the top of the current hour in Oslo
    val now = remember {
        ZonedDateTime.now(osloZone)
            .withMinute(0)
            .withSecond(0)
            .withNano(0)
    }
    // ① take the Instant, convert to Oslo, then +2h
    val latest = remember {
        getAvailabilityLatestTime()
            .atZone(osloZone)
            .withMinute(0).withSecond(0).withNano(0)  // optional: snap to the hour
            .plusHours(2)
    }

    // ② build each full-hour slot
    val hours = generateSequence(now) { it.plusHours(1) }
        .takeWhile { !it.isAfter(latest) }
        .toList()

    // ③ group by calendar day
    val groupedByDate = hours.groupBy { it.toLocalDate() }

    // ④ remember a vertical scroll
    val scrollState = rememberScrollState()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Select Launch Time") },
        text = {
            Column(
                Modifier
                    .fillMaxWidth()
                    .verticalScroll(scrollState)
                    .padding(vertical = 8.dp)
            ) {
                groupedByDate.forEach { (day, slots) ->
                    Text(
                        text = day.format(DateTimeFormatter.ofPattern("EEEE dd.MM")),
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                    // ⑤ horizontal, scrollable row
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp)
                    ) {
                        items(slots) { slotZdt ->
                            OutlinedButton(
                                onClick = { onConfirm(slotZdt.toInstant()) },
                                shape = CircleShape,
                                contentPadding = PaddingValues(
                                    horizontal = 12.dp,
                                    vertical = 4.dp
                                )
                            ) {
                                Text("${slotZdt.hour.toString().padStart(2,'0')}:00")
                            }
                        }
                    }
                    Spacer(Modifier.height(12.dp))
                }
            }
        },
        confirmButton = { /* no-op; taps confirm */ },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
