package no.uio.ifi.in2000.met2025.ui.screens.mapScreen.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
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
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import no.uio.ifi.in2000.met2025.ui.theme.WarmOrange
import java.time.Instant
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun LaunchWindowPickerDialog(
    showDialog: Boolean,
    availabilityInstant: Instant?,
    onDismiss: () -> Unit,
    onRetry: () -> Unit,
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
        AlertDialog(
            onDismissRequest = onDismiss,
            containerColor = MaterialTheme.colorScheme.surface,
            title = { Text("Couldn’t load GRIB data", color = MaterialTheme.colorScheme.onSurface) },
            text = {
                Surface(
                    color = MaterialTheme.colorScheme.surface,
                    modifier = Modifier.fillMaxWidth()
                ) {
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
                }
            },
            confirmButton = {
                TextButton(
                    onClick = onRetry,
                    colors = ButtonDefaults.textButtonColors(contentColor = WarmOrange)
                ) {
                    Text("Retry")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = onDismiss,
                    colors = ButtonDefaults.textButtonColors(contentColor = WarmOrange)
                ) {
                    Text("Cancel")
                }
            }
        )
    } else {
        val latest = remember(availabilityInstant) {
            availabilityInstant
                .atZone(osloZone)
                .withMinute(0).withSecond(0).withNano(0)
                .plusHours(2)
        }
        val hours = generateSequence(now) { it.plusHours(1) }
            .takeWhile { !it.isAfter(latest) }
            .toList()
        val grouped = hours.groupBy { it.toLocalDate() }
        val scroll = rememberScrollState()

        AlertDialog(
            onDismissRequest = onDismiss,
            containerColor = MaterialTheme.colorScheme.surface,
            title = { Text("Select Launch Time", color = MaterialTheme.colorScheme.onSurface) },
            text = {
                Surface(
                    color = MaterialTheme.colorScheme.surface,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        Modifier
                            .fillMaxWidth()
                            .verticalScroll(scroll)
                            .padding(vertical = 8.dp),
                        horizontalAlignment = Alignment.Start
                    ) {
                        // Display availability range in Oslo local time
                        Text(
                            "GRIB-2 data availability:\n" +
                                    "${now.format(DateTimeFormatter.ofPattern("HH:mm"))} – " +
                                    "${latest.format(DateTimeFormatter.ofPattern("HH:mm"))} (Oslo time)",
                            color = MaterialTheme.colorScheme.onSurface,
                            fontSize = 16.sp,
                            textAlign = TextAlign.Center
                        )
                        HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f))
                        grouped.forEach { (day, slots) ->
                            Text(
                                day.format(DateTimeFormatter.ofPattern("EEEE dd.MM")),
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface,
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
                                        border = BorderStroke(1.dp, WarmOrange),
                                        colors = ButtonDefaults.outlinedButtonColors(contentColor = WarmOrange),
                                        contentPadding = PaddingValues(
                                            horizontal = 12.dp, vertical = 4.dp
                                        )
                                    ) {
                                        Text(
                                            "${slotZdt.hour.toString().padStart(2, '0')}:00",
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                }
                            }
                            Spacer(Modifier.height(12.dp))
                        }
                    }
                }
            },
            confirmButton = { /* no-op */ },
            dismissButton = {
                TextButton(
                    onClick = onDismiss,
                    colors = ButtonDefaults.textButtonColors(contentColor = WarmOrange)
                ) {
                    Text("Cancel")
                }
            }
        )
    }
}
