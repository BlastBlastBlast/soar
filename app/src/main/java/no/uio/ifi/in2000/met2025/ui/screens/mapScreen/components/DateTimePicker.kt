package no.uio.ifi.in2000.met2025.ui.screens.mapScreen.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ArrowDropUp
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

/**
 * Dialog-only composable for selecting a date (today/tomorrow/day after) and an hour (full hours).
 * The caller is responsible for showing the trigger button in TrajectoryPopup.
 *
 * @param showDialog Controls visibility of the dialog
 * @param initialDateTime Starting LocalDateTime
 * @param onDismiss Called when dialog is cancelled
 * @param onConfirm  Called with the selected LocalDateTime when OK pressed
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DateTimePickerDialog(
    showDialog: Boolean,
    initialDateTime: LocalDateTime,
    onDismiss: () -> Unit,
    onConfirm: (LocalDateTime) -> Unit
) {
    if (!showDialog) return

    val osloZone = ZoneId.of("Europe/Oslo")
    // Local mutable copy for selection
    var dateTime by remember { mutableStateOf(initialDateTime) }

    // Formatters
    val dateFormatter = DateTimeFormatter.ofPattern("dd.MM")

    AlertDialog(
        onDismissRequest = { onDismiss() },
        title = { Text("Select Date & Hour", style = MaterialTheme.typography.titleSmall) },
        text = {
            Column(
                Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            ) {
                // Day selector row
                val today = LocalDate.now(osloZone)
                val days = listOf(0L,1L,2L).map { today.plusDays(it)}
                Row(
                    Modifier
                        .fillMaxWidth()
                        .height(64.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    days.forEach { d ->
                        val selected = d == dateTime.toLocalDate()
                        OutlinedButton(
                            onClick = { dateTime = dateTime.with(d) },
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight(),
                            shape = RectangleShape,
                            contentPadding = PaddingValues(0.dp),
                            colors = ButtonDefaults.outlinedButtonColors(
                                containerColor = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface,
                                contentColor = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                            )
                        ) {
                            Text(
                                text = d.format(dateFormatter),
                                fontSize = 14.sp,
                                fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                                textAlign = TextAlign.Center,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp)
                            )
                        }
                    }
                }

                Spacer(Modifier.height(16.dp))

                // Hour display and controls
                Column(
                    Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Display HH:00
                    Text(
                        text = dateTime.hour.toString().padStart(2,'0') + ":00",
                        fontSize = 52.sp,
                        fontWeight = FontWeight.ExtraBold,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Control buttons: Now, Up, Down
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Button(
                            onClick = { dateTime = LocalDateTime.now(osloZone).withMinute(0) },
                            shape = CircleShape,
                            modifier = Modifier.size(56.dp),
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Text("Now", fontSize = 14.sp)
                        }
                        Button(
                            onClick = { dateTime = dateTime.withHour((dateTime.hour+23)%24) },
                            shape = CircleShape,
                            modifier = Modifier.size(56.dp),
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Icon(Icons.Default.ArrowDropUp, contentDescription="Up")
                        }
                        Button(
                            onClick = { dateTime = dateTime.withHour((dateTime.hour+1)%24) },
                            shape = CircleShape,
                            modifier = Modifier.size(56.dp),
                            contentPadding = PaddingValues(0.dp)
                        ) {
                            Icon(Icons.Default.ArrowDropDown, contentDescription="Down")
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(dateTime.withMinute(0)) },
                shape = CircleShape,
                modifier = Modifier.size(64.dp),
                contentPadding = PaddingValues(0.dp)
            ) { Text("OK") }
        },
        dismissButton = {
            Button(
                onClick = { onDismiss() },
                shape = CircleShape,
                modifier = Modifier.size(64.dp),
                contentPadding = PaddingValues(0.dp)
            ) { Text("Cancel", fontSize=14.sp) }
        }
    )
}
