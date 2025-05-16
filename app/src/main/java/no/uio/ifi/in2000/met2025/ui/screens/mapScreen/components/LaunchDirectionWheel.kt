package no.uio.ifi.in2000.met2025.ui.screens.mapScreen.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import no.uio.ifi.in2000.met2025.R
import no.uio.ifi.in2000.met2025.data.local.database.RocketConfig
import no.uio.ifi.in2000.met2025.ui.screens.mapScreen.MapScreenViewModel
import no.uio.ifi.in2000.met2025.ui.screens.weatherScreen.components.WindDirectionIcon
import java.time.Instant
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun WindDirectionIcon2(windDirection: Double?) {
    if (windDirection == null) {
        return
    }
    val arrowPainter = painterResource(id = R.drawable.windicator)

    Image(
        painter = arrowPainter,
        contentDescription = "Wind Direction",
        modifier = Modifier
            .size(64.dp)
            .graphicsLayer(rotationZ = windDirection.toFloat())
            .semantics { role = Role.Image }
        ,
    )
}

/**
 * A launch direction wheel overlaying a compass dial outline, drawing a red launch
 * indicator line, showing the wind direction icon, and displaying numeric readouts.
 */
@Composable
fun LaunchDirectionWheel(
    onAngleChange: (Double) -> Unit = {},
    forecastUiState: MapScreenViewModel.ForecastDataUiState,
    selectedConfig: RocketConfig?
) {
    // initial launch azimuth
    val defaultAngle = selectedConfig?.launchAzimuth ?: 0.0
    var rotationAngle by remember { mutableStateOf(defaultAngle) }

    // wind-from direction when available
    val windDirection = when (forecastUiState) {
        is MapScreenViewModel.ForecastDataUiState.Success ->
            forecastUiState.forecastData.values.windFromDirection
        else -> defaultAngle
    }

    // theme colors
    val surfaceColor = MaterialTheme.colorScheme.surface
    val onSurfaceColor = MaterialTheme.colorScheme.onSurface

    // dial dimensions
    val dialSize = 180.dp
    val dialSizePx = with(LocalDensity.current) { dialSize.toPx() }
    val center = Offset(dialSizePx / 2f, dialSizePx / 2f)

    Box(
        modifier = Modifier.size(250.dp),
        contentAlignment = Alignment.Center
    ) {
        // interactive container for dial + indicator
        Box(
            modifier = Modifier
                .size(dialSize)
                .clip(CircleShape)            // <-- force a circular touch area
                .pointerInput(Unit) {
                    detectDragGestures { change, _ ->
                        val dx = (change.position.x - center.x).toDouble()
                        val dy = (change.position.y - center.y).toDouble()
                        val touchAngle = Math.toDegrees(atan2(dy, dx))
                        val newAngle = ((touchAngle + 360) % 360 + 90) % 360
                        rotationAngle = newAngle
                        onAngleChange(newAngle)
                    }
                }
                .clickable {
                    rotationAngle = windDirection
                    onAngleChange(windDirection)
                }
        ) {
            // background compass dial (outline only)
            CompassDial(modifier = Modifier.matchParentSize())
            // draw red launch indicator line
            Canvas(modifier = Modifier.matchParentSize()) {
                val r = size.minDimension / 2f
                val angRad = Math.toRadians(rotationAngle - 90.0)
                val endX = center.x + cos(angRad) * r
                val endY = center.y + sin(angRad) * r
                drawLine(
                    color = Color.Red,
                    start = center,
                    end = Offset(endX.toFloat(), endY.toFloat()),
                    strokeWidth = 8f
                )
            }
        }

        // wind direction overlay or loading spinner
        when (forecastUiState) {
            is MapScreenViewModel.ForecastDataUiState.Success ->
                WindDirectionIcon2(windDirection)
            is MapScreenViewModel.ForecastDataUiState.Loading ->
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = onSurfaceColor
                )
            else -> { }
        }

        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .background(surfaceColor, RoundedCornerShape(4.dp))
                .padding(horizontal = 8.dp, vertical = 4.dp)// make row as wide as the dial
        ) {
            Text(
                text = "Wind dir: ${windDirection.toInt()}°",
                fontSize = 14.sp,
                color = onSurfaceColor
            )
            Text(
                text = "Launch dir: ${rotationAngle.toInt()}°",
                fontSize = 14.sp,
                color = onSurfaceColor
            )
        }
    }
}
