package no.uio.ifi.in2000.met2025.ui.screens.home.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.CustomAccessibilityAction
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.customActions
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import no.uio.ifi.in2000.met2025.data.local.database.LaunchSite
import no.uio.ifi.in2000.met2025.data.local.database.RocketConfig
import no.uio.ifi.in2000.met2025.ui.theme.Black
import no.uio.ifi.in2000.met2025.ui.theme.WarmOrange
import kotlin.math.roundToInt

@Composable
fun TrajectoryPopup(
    show: Boolean,
    lastVisited: LaunchSite?,
    configs: List<RocketConfig>,              // ← all configs
    selectedConfig: RocketConfig?,            // ← current default
    onSelectConfig: (RocketConfig) -> Unit,   // ← tap a new default
    onClose: () -> Unit,
    onStartTrajectory: () -> Unit,
    onClearTrajectory: () -> Unit,
    onEditConfigs: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Track vertical drag offset
    var offsetY by remember { mutableStateOf(0f) }
    // Threshold to trigger dismiss (e.g. 100dp)
    val thresholdPx = with(LocalDensity.current) { 100.dp.toPx() }

    AnimatedVisibility(
        visible = show,
        enter   = slideInVertically { it } + fadeIn(),
        exit    = slideOutVertically { it } + fadeOut(),
        modifier = modifier.semantics {
            contentDescription = "Trajectory options dialog"
            customActions = listOf(
                CustomAccessibilityAction(label = "Close dialog") { onClose(); true }
            ) }
    ) {
        Surface(
            shape          = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
            color          = WarmOrange,
            tonalElevation = 0.dp,
            modifier       = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.3f)
                .offset { IntOffset(x = 0, y = offsetY.roundToInt()) }
                .pointerInput(show) {
                    detectVerticalDragGestures { change, dragAmount ->
                        change.consume()
                        val newOffset = (offsetY + dragAmount).coerceAtLeast(0f)
                        offsetY = newOffset
                        if (newOffset > thresholdPx) {
                            offsetY = 0f
                            onClose()
                        }
                    }
                }
        ) {
            Column(
                Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Small drag handle
                Box(
                    modifier = Modifier
                        .width(40.dp)
                        .height(4.dp)
                        .background(
                            color = Color.Black,
                            shape = RoundedCornerShape(2.dp)
                        )
                )

                Text(
                    text = lastVisited
                        ?.let { "Current: %.4f, %.4f".format(it.latitude, it.longitude) }
                        ?: "No location yet",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.semantics {
                        contentDescription =
                            lastVisited
                                ?.let { "Last visited at %.4f latitude, %.4f longitude".format(it.latitude, it.longitude) }
                                ?: "No location yet"
                    }
                )

                // Grid of buttons (2 columns)
                // 2) Carousel right here:
                RocketConfigCarousel(
                    configs        = configs,
                    selectedConfig = selectedConfig,
                    onSelectConfig = onSelectConfig,
                    modifier       = Modifier
                        .fillMaxWidth()
                        .height(70.dp)
                )

                Column(
                    Modifier
                        .fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick  = onStartTrajectory,
                            modifier = Modifier
                                .weight(1f)
                                .semantics {
                                    contentDescription = "Start trajectory"
                                    role = Role.Button
                                }
                        ) {
                            Text("▶️ Start Trajectory")
                        }
                        Button(
                            onClick  = onEditConfigs,
                            modifier = Modifier
                                .weight(1f)
                                .semantics {
                                    contentDescription = "Edit rocket configurations"
                                    role = Role.Button
                                }
                        ) {
                            Text("⚙️ Edit Configs")
                        }
                    }
                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick  = onClearTrajectory,
                            modifier = Modifier
                                .weight(1f)
                                .semantics {
                                    contentDescription = "Clear trajectory"
                                    role = Role.Button
                                }
                        ) {
                            Text("🗑️ Clear Trajectory")
                        }
                    }
                }
            }
        }
    }
}