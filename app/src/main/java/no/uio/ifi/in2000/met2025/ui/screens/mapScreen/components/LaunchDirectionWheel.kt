package no.uio.ifi.in2000.met2025.ui.screens.mapScreen.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
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
            .size(60.dp)
            .graphicsLayer(rotationZ = windDirection.toFloat())
            .semantics { role = Role.Image }
        ,
    )
}

@Composable
fun LaunchDirectionWheel(
    onAngleChange: (Double) -> Unit = {}, // Callback for angle updates
    forecastUiState: MapScreenViewModel.ForecastDataUiState,
    selectedConfig: RocketConfig?,
) {
    val defaultAngle = selectedConfig?.launchAzimuth ?: 0.0
    var rotationAngle by remember { mutableDoubleStateOf(0.0) }

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .size(250.dp)
    ) {
        var windDirection = defaultAngle

        Canvas(modifier = Modifier.size(200.dp)) {
            // Draw the circle
            drawCircle(color = Color.Gray)
        }

        when (forecastUiState) {
            is MapScreenViewModel.ForecastDataUiState.Success -> {
                val windFromDirection = forecastUiState.forecastData.values.windFromDirection
                windDirection = windFromDirection
                WindDirectionIcon2(
                    windDirection = windFromDirection,
                )
            }
            is MapScreenViewModel.ForecastDataUiState.Loading -> {
                CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.Black)
            }
            else -> {}
        }

        // Draw the circle and indicator
        Canvas(modifier = Modifier.size(200.dp)) {
            val radius = size.minDimension / 2
            val indicatorX = center.x + radius * cos(Math.toRadians(rotationAngle) - Math.PI / 2.0).toFloat()
            val indicatorY = center.y + radius * sin(Math.toRadians(rotationAngle) - Math.PI / 2.0).toFloat()

            drawLine(
                color = Color.Red,
                start = center,
                end = Offset(indicatorX, indicatorY),
                strokeWidth = 4f
            )
        }

        // Wind direction angle box at top
        Box(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .background(
                    color = MaterialTheme.colorScheme.surface,
                    shape = RoundedCornerShape(4.dp)
                )
                .padding(horizontal = 8.dp, vertical = 4.dp)
        ) {
            Text(
                text = "Wind: ${windDirection.toInt()}°",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        // Launch angle box at bottom
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .background(
                    color = MaterialTheme.colorScheme.surface,
                    shape = RoundedCornerShape(4.dp)
                )
                .padding(horizontal = 8.dp, vertical = 4.dp)
        ) {
            Text(
                text = "Launch: ${rotationAngle.toInt()}°",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        // Handle rotation gestures
        Box(
            modifier = Modifier
                .size(200.dp)
                .pointerInput(Unit) {
                    detectDragGestures { change, _ ->
                        val center = Offset((size.width / 2).toFloat(), (size.height / 2).toFloat())
                        val touchAngle = Math.toDegrees(
                            atan2(
                                change.position.y - center.y,
                                change.position.x - center.x
                            ).toDouble()
                        )
                        // Normalize the angle to 0° - 360°
                        rotationAngle = ((touchAngle + 360) % 360 + 90) % 360
                        onAngleChange(rotationAngle)
                    }
                }
                .clickable {
                    rotationAngle = windDirection
                    onAngleChange(rotationAngle)
                }
        )
    }
}