/*
 * Bottom-sheet dialog for configuring and launching a rocket trajectory.
 *
 * Main functionality:
 *  - Displays last-visited location and lets you pick a RocketConfig
 *  - Provides buttons to start or clear the trajectory simulation
 *  - Supports swipe-down to dismiss with animated enter/exit
 *
 * Special notes:
 *  - Uses detectVerticalDragGestures to allow swipe-to-dismiss
 *  - AnimatedVisibility wraps the content for smooth transitions
 */
package no.uio.ifi.in2000.met2025.ui.screens.mapScreen.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.RocketLaunch
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.CustomAccessibilityAction
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.customActions
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import no.uio.ifi.in2000.met2025.data.local.database.LaunchSite
import no.uio.ifi.in2000.met2025.data.local.database.RocketConfig
import no.uio.ifi.in2000.met2025.data.models.locationforecast.ForecastDataItem
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import kotlin.math.roundToInt

/**
 * Shows a draggable bottom sheet with:
 *  - Last-visited coordinates display
 *  - RocketConfigCarousel for selecting a config
 *  - Start, Edit, and Clear buttons for trajectory control
 *
 * @param show Whether the popup is visible
 * @param lastVisited The last visited LaunchSite, or null
 * @param rocketConfigs Available rocket configurations
 * @param selectedConfig Currently selected RocketConfig, or null
 * @param onSelectConfig Callback when a config is tapped
 * @param onClose Callback to dismiss the popup
 * @param onStartTrajectory Callback to begin the simulation
 * @param onClearTrajectory Callback to clear existing trajectory
 * @param onEditConfigs Callback to open the configs editor
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TrajectoryPopup(
    show: Boolean,
    lastVisited: LaunchSite?,
    currentSite: LaunchSite?,
    rocketConfigs: List<RocketConfig>,
    selectedConfig: RocketConfig?,
    onSelectConfig: (RocketConfig) -> Unit,
    onClose: () -> Unit,
    onStartTrajectory: (Instant) -> Unit,
    onClearTrajectory: () -> Unit,
    onEditConfigs: () -> Unit,
    onSelectWindow: (Instant) -> Unit,
    availabilityInstant: Instant?,             // nullable Instant
    onRetryAvailability: () -> Unit,           // retry callback
    defaultLaunch: Instant,
    modifier: Modifier = Modifier
) {
    var offsetY by remember { mutableStateOf(0f) }
    val thresholdPx = with(LocalDensity.current) { 100.dp.toPx() }
    val oslo = ZoneId.of("Europe/Oslo")
    // truncate “now” to the top of the hour
    var pickedInstant by remember { mutableStateOf(defaultLaunch) }
    var showWindowPicker by remember { mutableStateOf(false) }

    AnimatedVisibility(
        visible = show,
        enter   = slideInVertically { it } + fadeIn(),
        exit    = slideOutVertically { it } + fadeOut(),
        modifier = modifier
            .fillMaxSize()
            .semantics {
                contentDescription = "Trajectory options dialog"
                customActions = listOf(
                    CustomAccessibilityAction("Close dialog") {
                        onClose(); true
                    }
                )
            }
    ) {
        Box(
            Modifier.fillMaxSize(),
            contentAlignment = Alignment.BottomCenter
        ) {
            Surface(
                shape           = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
                color           = Color.Black.copy(alpha = 0.6f),
                shadowElevation = 8.dp,
                modifier        = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.40f)
                    .offset { IntOffset(0, offsetY.roundToInt()) }
                    .pointerInput(show) {
                        detectVerticalDragGestures { change, dy ->
                            change.consume()
                            val new = (offsetY + dy).coerceAtLeast(0f)
                            offsetY = new
                            if (new > thresholdPx) {
                                offsetY = 0f
                                onClose()
                            }
                        }
                    }
            ) {
                Column(
                    Modifier
                        .fillMaxSize()
                        .padding(8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Drag handle
                    Box(
                        Modifier
                            .size(40.dp, 4.dp)
                            .background(Color.White, RoundedCornerShape(2.dp))
                    )

                    // Location label
                    val label = currentSite?.name?.let { "$it: " } ?: "Location: "
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            label,
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = Color.White
                        )
                        Text(
                            lastVisited
                                ?.let { "%.4f, %.4f".format(it.latitude, it.longitude) }
                                ?: "No location yet",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = Color.White
                        )
                    }

                    // Rocket carousel
                    RocketConfigCarousel(
                        rocketConfigs  = rocketConfigs,
                        selectedConfig = selectedConfig,
                        onSelectConfig = onSelectConfig,
                        modifier       = Modifier
                            .fillMaxWidth()
                            .height(65.dp)
                    )

                    // Start & Edit
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        IconTextButton(
                            modifier           = Modifier.weight(1f),
                            icon               = Icons.Default.RocketLaunch,
                            contentDescription = "Start trajectory",
                            line1              = "Start",
                            line2              = "Trajectory",
                            onClick            = { onStartTrajectory(pickedInstant) }
                        )
                        IconTextButton(
                            modifier           = Modifier.weight(1f),
                            icon               = Icons.Default.Settings,
                            contentDescription = "Edit configs",
                            line1              = "Rocket",
                            line2              = "Configs",
                            onClick            = onEditConfigs
                        )
                    }

                    // bottom row: Pick-time & Clear
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        val displayZdt = pickedInstant.atZone(oslo)
                        IconTextButton(
                            modifier           = Modifier.weight(1f),
                            icon               = Icons.Default.AccessTime,  // clock icon!
                            contentDescription = "Pick time",
                            line1              = "Launch Window:",
                            line2              = displayZdt.format(DateTimeFormatter.ofPattern("dd.MM HH:00")),
                            onClick            = { showWindowPicker = true }
                        )
                        IconTextButton(
                            modifier           = Modifier.weight(1f),
                            icon               = Icons.Default.Delete,
                            contentDescription = "Clear trajectory",
                            line1              = "Clear",
                            line2              = "Trajectory",
                            onClick            = onClearTrajectory
                        )
                    }

                    // The scrolling hour-picker, which hands back an Instant
                    LaunchWindowPickerDialog(
                        showDialog           = showWindowPicker,
                        availabilityInstant  = availabilityInstant,
                        onDismiss            = { showWindowPicker = false },
                        onRetry              = { onRetryAvailability() },
                        onConfirm           = {
                            pickedInstant = it
                            showWindowPicker = false
                            onSelectWindow(it)
                        }
                    )
                }
            }
        }
    }
}