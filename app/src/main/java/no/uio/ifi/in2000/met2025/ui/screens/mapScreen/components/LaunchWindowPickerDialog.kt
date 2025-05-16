package no.uio.ifi.in2000.met2025.ui.screens.mapScreen.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import no.uio.ifi.in2000.met2025.ui.theme.WarmOrange
import java.time.Instant
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

/**
 * Dialog component allowing the user to select a launch time based on GRIB-2 forecast availability.
 *
 * @param showDialog Controls whether the dialog is visible.
 * @param availabilityInstant Instant representing the start of available forecast data.
 * @param onDismiss Callback when the dialog is dismissed.
 * @param onRetry Callback to retry loading data on error.
 * @param onConfirm Callback with the selected launch Instant.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun LaunchWindowPickerDialog(
    showDialog: Boolean,
    availabilityInstant: Instant?,
    onDismiss: () -> Unit,
    onRetry: () -> Unit,
    onConfirm: (Instant) -> Unit
) {
    // Only show the dialog if requested
    if (!showDialog) return

    // Define Oslo time zone for localizing instants
    val oslo = ZoneId.of("Europe/Oslo")

    // Convert the availability instant to a ZonedDateTime truncated to the hour
    val fileStart = availabilityInstant
        ?.atZone(oslo)
        ?.truncatedTo(ChronoUnit.HOURS)

    // Get the current Oslo hour
    val nowHour = ZonedDateTime.now(oslo).truncatedTo(ChronoUnit.HOURS)
    // Choose the later of now and file start as initial slot
    //val startZdt = maxOf(nowHour, fileStart)

    // If data failed to load completely, show error dialog
    if (availabilityInstant == null) {
        AlertDialog(
            onDismissRequest = onDismiss,
            containerColor = MaterialTheme.colorScheme.surface,
            title = {
                Text(
                    text = "Couldn’t load GRIB data",
                    color = MaterialTheme.colorScheme.onSurface
                )
            },
            text = {
                // Error icon + message when forecasts can't be fetched
                Surface(
                    color = MaterialTheme.colorScheme.surface,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
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
                        Spacer(Modifier.size(12.dp))
                        Text(
                            text = "We hit a snag fetching the forecasts.\nCheck your connection and try again.",
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
                    Text("Retry") // Retry loading data
                }
            },
            dismissButton = {
                TextButton(
                    onClick = onDismiss,
                    colors = ButtonDefaults.textButtonColors(contentColor = WarmOrange)
                ) {
                    Text("Cancel") // Dismiss error dialog
                }
            }
        )
    } else {
        // Compute the latest available slot by adding 2 hours to the raw availability
        val latest = remember(availabilityInstant) {
            availabilityInstant
                .atZone(oslo)
                .withMinute(0)
                .withSecond(0)
                .withNano(0)
                .plusHours(2)
        }

        // Build a list of hourly instants from now until latest
        val hours = generateSequence(nowHour) { it.plusHours(1) }
            .takeWhile { !it.isAfter(latest) }
            .toList()

        // Group the hours by local date for section headers
        val grouped = hours.groupBy { it.toLocalDate() }

        AlertDialog(
            onDismissRequest = onDismiss,
            containerColor = MaterialTheme.colorScheme.surface,
            title = {
                Text(
                    text = "Select Launch Time",
                    color = MaterialTheme.colorScheme.onSurface
                )
            },
            text = {
                // Constrain dialog height and allow vertical scroll
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 0.dp, max = 350.dp)
                ) {
                    // LazyColumn efficiently renders an arbitrary number of days
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(vertical = 8.dp)
                    ) {
                        // Header showing overall availability range
                        item {
                            Text(
                                text = "GRIB-2 data availability:\n" +
                                        "${nowHour.format(DateTimeFormatter.ofPattern("HH:mm"))} – " +
                                        "${latest.format(DateTimeFormatter.ofPattern("HH:mm"))} (Oslo time)",
                                color = MaterialTheme.colorScheme.onSurface,
                                fontSize = 16.sp,
                                textAlign = TextAlign.Center,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                            HorizontalDivider(
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f)
                            )
                        }

                        // One section per date with a LazyRow of hour buttons
                        grouped.forEach { (day, slots) ->
                            item {
                                // Date header
                                Text(
                                    text = day.format(DateTimeFormatter.ofPattern("EEEE dd.MM")),
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.padding(8.dp)
                                )
                            }
                            item {
                                // Horizontal scrolling row of time slots
                                LazyRow(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    contentPadding = PaddingValues(horizontal = 8.dp)
                                ) {
                                    items(slots) { slotZdt ->
                                        OutlinedButton(
                                            onClick = { onConfirm(slotZdt.toInstant()) },
                                            shape = CircleShape,
                                            border = BorderStroke(1.dp, WarmOrange),
                                            colors = ButtonDefaults.outlinedButtonColors(
                                                contentColor = WarmOrange
                                            ),
                                            contentPadding = PaddingValues(
                                                horizontal = 12.dp,
                                                vertical = 4.dp
                                            )
                                        ) {
                                            // Button text shows hour in 24h format
                                            Text(
                                                text = slotZdt.hour.toString().padStart(2, '0') + ":00",
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                        }
                                    }
                                }
                            }
                            item {
                                Spacer(modifier = Modifier.height(12.dp))
                            }
                        }
                    }
                }
            },
            confirmButton = { /* No confirm button: selection happens instantly */ },
            dismissButton = {
                TextButton(
                    onClick = onDismiss,
                    colors = ButtonDefaults.textButtonColors(contentColor = WarmOrange)
                ) {
                    Text("Cancel") // Close without selecting
                }
            }
        )
    }
}
