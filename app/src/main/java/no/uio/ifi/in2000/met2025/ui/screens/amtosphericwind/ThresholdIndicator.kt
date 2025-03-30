package no.uio.ifi.in2000.met2025.ui.screens.amtosphericwind

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.drawscope.DrawScope
import kotlin.math.min
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.tooling.preview.Preview
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun ThresholdIndicator(measuredValue: Float, threshold: Float) {
    val circleSize = 48.dp

    val proximityRatio = measuredValue / threshold
    val cappedRatio = min(1f, proximityRatio)

    val safeColor = Color.Green
    val unsafeColor = Color.Red

    //val backgroundColor = Color.Green.copy(alpha = 1f - cappedRatio).compositeOver(Color.Red.copy(alpha = cappedRatio))
    val backgroundColor = mixColors(safeColor, unsafeColor, cappedRatio)

    // Calculate needle angle or display exceeded icon
    val needleAngle = 360f * cappedRatio + 90f

    Box {
        Canvas(modifier = Modifier.size(circleSize)) {
            // Draw circular background
            drawCircle(color = backgroundColor)

            if (measuredValue <= threshold) {
                drawNeedle(needleAngle)
            }
        }
        if (measuredValue > threshold) {
            // Display "X" icon if threshold exceeded
            Image(
                imageVector = Icons.Default.Close,
                contentDescription = "Exceeded Threshold",
                modifier = Modifier.size(circleSize).padding((circleSize.value/4).dp),
                colorFilter = androidx.compose.ui.graphics.ColorFilter.tint(Color.White)
            )
        }
    }

}

fun DrawScope.drawNeedle(angle: Float) {
    // Needle drawing logic: rotates based on angle
    val needleLength = size.minDimension / 2f
    drawLine(
        color = Color.White,
        start = center,
        end = Offset(
            x = center.x - needleLength * cos(Math.toRadians(angle.toDouble())).toFloat() * 0.8f,
            y = center.y - needleLength * sin(Math.toRadians(angle.toDouble())).toFloat() * 0.8f
        ),
        strokeWidth = 4f,
        cap = StrokeCap.Round
    )
}

fun mixColors(c1: Color, c2: Color, ratio: Float): Color {
    val diffR = c2.red - c1.red
    val diffG = c2.green - c1.green
    val diffB = c2.blue - c1.blue

    val distR = abs(diffR)
    val distG = abs(diffG)
    val distB = abs(diffB)

    val distSum = distR + distG + distB

    val nDistR = distR / distSum
    val nDistG = distG / distSum
    val nDistB = distB / distSum

    var ratioRem: Float = ratio

    val nTravelR = min(nDistR, ratioRem)
    ratioRem -= nTravelR

    val nTravelG = min(nDistG, ratioRem)
    ratioRem -= nTravelG

    val nTravelB = min(nDistB, ratioRem)

    val newR = c1.red + diffR * nTravelR / nDistR
    val newG = c1.green + diffG * nTravelG /nDistG
    val newB = c1.blue + diffB * nTravelB /nDistB

    return Color(newR, newG, newB)
}

@Preview
@Composable
fun ThresholdIndicatorPreview() {
    Column {
        ThresholdIndicator(0f, 4f)
        ThresholdIndicator(1.3f, 4f)
        ThresholdIndicator(2.3f, 4f)
        ThresholdIndicator(3.0f, 4f)
        ThresholdIndicator(3.9f, 4f)
        ThresholdIndicator(4.2f, 4f)
    }
}