package no.uio.ifi.in2000.met2025.ui.screens.mapScreen.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.cos
import kotlin.math.sin

/**
 * A compass dial with tick marks and cardinal labels.
 *
 * @param modifier Modifier to be applied to the canvas.
 */
@Composable
fun CompassDial(
    modifier: Modifier = Modifier
        .size(200.dp)             // default size
        .padding(16.dp)
) {
    // Hoist all theme & density lookups here
    val primaryColor     = Color.White
    val onBackground     = Color.White

    // Precompute label text size in px
    val density         = LocalDensity.current
    val labelTextSizePx = with(density) { 20.sp.toPx() }

    Canvas(modifier = modifier) {
        val radius = size.minDimension / 2f
        val center = Offset(radius, radius)

        // Outer ring
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(primaryColor, Color.Transparent),
                center = center,
                radius = radius
            ),
            center = center,
            radius = radius,
            style = Stroke(width = radius * 0.07f)
        )

        // Tick marks every 10°
        for (i in 0 until 36) {
            val angle = (i * 10f - 90f).toRadians()
            val outer = Offset(
                x = center.x + cos(angle) * radius * 0.9f,
                y = center.y + sin(angle) * radius * 0.9f
            )
            val inner = Offset(
                x = center.x + cos(angle) * radius * if (i % 3 == 0) 0.75f else 0.80f,
                y = center.y + sin(angle) * radius * if (i % 3 == 0) 0.75f else 0.80f
            )
            drawLine(
                color = onBackground,
                start = inner,
                end   = outer,
                strokeWidth = if (i % 3 == 0) 4f else 2f
            )
        }

        // Cardinal labels
        val cardinal = listOf("N" to 0f, "E" to 90f, "S" to 180f, "W" to 270f)
        cardinal.forEach { (label, deg) ->
            val rad = (deg - 90f).toRadians()
            val textOffset = Offset(
                x = center.x + cos(rad) * radius * 0.6f,
                y = center.y + sin(rad) * radius * 0.6f
            )
            drawIntoCanvas { canvas ->
                val paint = android.graphics.Paint().apply {
                    color = onBackground.toArgb()
                    textAlign = android.graphics.Paint.Align.CENTER
                    textSize = labelTextSizePx
                    isFakeBoldText = true
                }
                canvas.nativeCanvas.drawText(
                    label,
                    textOffset.x,
                    textOffset.y + labelTextSizePx / 3,
                    paint
                )
            }
        }

        // Center hub
        drawCircle(
            color = onBackground,
            radius = radius * 0.05f,
            center = center
        )
    }
}

// Helper extension
private fun Float.toRadians() = Math.toRadians(this.toDouble()).toFloat()