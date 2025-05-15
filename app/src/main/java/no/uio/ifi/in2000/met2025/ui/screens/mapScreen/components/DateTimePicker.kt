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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
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
    availabilityInstant: Instant?,         // ← now nullable
    onDismiss: () -> Unit,
    onRetry: () -> Unit,                  // ← retry callback
    onConfirm: (Instant) -> Unit
) {
    if (!showDialog) return

    val osloZone = ZoneId.of("Europe/Oslo")
    val now = remember {
        ZonedDateTime.now(osloZone)
            .withMinute(0)
            .withSecond(0)
            .withNano(0)
    }

    if (availabilityInstant == null) {
        // ─── ERROR STATE ───────────────────────────────────────────
        AlertDialog(
            onDismissRequest = onDismiss,
            title = { Text("Couldn’t load GRIB data") },
            text = {
                Column(
                    Modifier
                        .fillMaxWidth()
                        .padding(vertical = 24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.CloudOff,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.error
                    )
                    Spacer(Modifier.height(12.dp))
                    Text(
                        "We hit a snag fetching the forecasts.\nCheck your connection and try again.",
                        color = MaterialTheme.colorScheme.error,
                        textAlign = TextAlign.Center
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = onRetry) {
                    Text("Retry")
                }
            },
            dismissButton = {
                TextButton(onClick = onDismiss) {
                    Text("Cancel")
                }
            }
        )
    } else {
        // ─── NORMAL PICKER STATE ───────────────────────────────────
        // 1) compute “latest + 2h” in Oslo
        val latest = remember(availabilityInstant) {
            availabilityInstant
                .atZone(osloZone)
                .withMinute(0).withSecond(0).withNano(0)
                .plusHours(2)
        }
        // 2) build each hour slot
        val hours = generateSequence(now) { it.plusHours(1) }
            .takeWhile { !it.isAfter(latest) }
            .toList()
        // 3) group by day
        val grouped = hours.groupBy { it.toLocalDate() }
        // 4) scroll state
        val scroll = rememberScrollState()

        AlertDialog(
            onDismissRequest = onDismiss,
            title = { Text("Select Launch Time") },
            text = {
                Column(
                    Modifier
                        .fillMaxWidth()
                        .verticalScroll(scroll)
                        .padding(vertical = 8.dp)
                ) {
                    grouped.forEach { (day, slots) ->
                        Text(
                            day.format(DateTimeFormatter.ofPattern("EEEE dd.MM")),
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(8.dp)
                        )
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            contentPadding = PaddingValues(horizontal = 8.dp)
                        ) {
                            items(slots) { slotZdt ->
                                OutlinedButton(
                                    onClick = { onConfirm(slotZdt.toInstant()) },
                                    shape = CircleShape,
                                    contentPadding = PaddingValues(
                                        horizontal = 12.dp, vertical = 4.dp
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
            confirmButton = { /*no-op; selections happen on click*/ },
            dismissButton = {
                TextButton(onClick = onDismiss) {
                    Text("Cancel")
                }
            }
        )
    }
}
