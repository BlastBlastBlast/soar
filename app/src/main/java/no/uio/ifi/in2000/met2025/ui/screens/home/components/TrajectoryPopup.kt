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
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import no.uio.ifi.in2000.met2025.data.local.database.LaunchSite
import no.uio.ifi.in2000.met2025.ui.theme.Black
import no.uio.ifi.in2000.met2025.ui.theme.WarmOrange
import kotlin.math.roundToInt

@Composable
fun TrajectoryPopup(
    show: Boolean,
    lastVisited: LaunchSite?,
    onClose: () -> Unit,
    onStartTrajectory: () -> Unit,
    onPickRocketConfig: () -> Unit,
    onShowCurrentLatLon: () -> Unit,
    onLaunchHere: () -> Unit,
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
        modifier = modifier
    ) {
        // Draggable surface
        Surface(
            shape          = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
            color          = WarmOrange,
            tonalElevation = 0.dp,
            modifier       = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.3f)
                // apply drag offset
                .offset { IntOffset(x = 0, y = offsetY.roundToInt()) }
                // detect vertical drag for dismiss
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
                    .padding(16.dp),
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
                    style = MaterialTheme.typography.bodyMedium
                )

                // Grid of buttons (2 columns)
                Column(
                    Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = onStartTrajectory,
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("▶️ Start Trajectory")
                        }
                        Button(
                            onClick = onPickRocketConfig,
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("⚙️ Rocket Configs")
                        }
                    }
                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = onShowCurrentLatLon,
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("📍 Show Current Lat/Lon")
                        }
                        Button(
                            onClick = onLaunchHere,
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("🚀 Launch From Center")
                        }
                    }
                }
            }
        }
    }
}