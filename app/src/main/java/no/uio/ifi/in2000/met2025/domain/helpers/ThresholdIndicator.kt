package no.uio.ifi.in2000.met2025.domain.helpers

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Column
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
import androidx.compose.ui.unit.Dp
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.math.sqrt

@Composable
fun ThresholdIndicator(measuredValue: Float, threshold: Float, size: Dp = 48.dp) {

    val cappedRatio = if (threshold == 0f)
                if (measuredValue > 0f)
                    1f
                else
                    0f
    else
        min(1f, measuredValue / threshold)

    val safeColor = Color.Green
    val unsafeColor = Color.Red

    val backgroundColor = safeColor.mixedWith(unsafeColor, cappedRatio)

    // Calculate needle angle or display exceeded icon
    val needleAngle = 360f * cappedRatio + 90f

    Canvas(modifier = Modifier.size(size)) {
        // Draw circular background
        drawCircle(color = Color.DarkGray, radius = size.toPx() / 2f)
        drawCircle(color = backgroundColor, radius = size.toPx() / 2f * 0.95f)

        if (measuredValue <= threshold) {
            drawNeedle(needleAngle)
        } else {
            drawCross()
        }
    }
}

fun DrawScope.drawNeedle(angle: Float) {
    // Needle drawing logic: rotates based on angle
    val needleLength = size.minDimension / 2f * 0.85f
    val xOffset = center.x - needleLength * cos(Math.toRadians(angle.toDouble())).toFloat()
    val yOffset = center.y - needleLength * sin(Math.toRadians(angle.toDouble())).toFloat()
    drawLine(
        color = Color.DarkGray,
        start = center,
        end = Offset(
            x = xOffset,
            y = yOffset
        ),
        strokeWidth = 5f,
        cap = StrokeCap.Round
    )
    drawLine(
        color = Color.White,
        start = center,
        end = Offset(
            x = xOffset,
            y = yOffset
        ),
        strokeWidth = 4f,
        cap = StrokeCap.Round
    )
}

fun DrawScope.drawCross() {
    val length = size.minDimension * 0.25f
    val cartesianComponent = length / sqrt(2f)
    val xNeg = center.x - cartesianComponent
    val yNeg = center.y - cartesianComponent
    val xPos = center.x + cartesianComponent
    val yPos = center.y + cartesianComponent

    val start1 = Offset(xNeg, yNeg)
    val end1 = Offset(xPos, yPos)
    val start2 = Offset(xPos, yNeg)
    val end2 = Offset(xNeg, yPos)

    drawLine(
        color = Color.DarkGray,
        start = start1,
        end = end1,
        strokeWidth = 5f,
        cap = StrokeCap.Round
    )
    drawLine(
        color = Color.DarkGray,
        start = start2,
        end = end2,
        strokeWidth = 5f,
        cap = StrokeCap.Round
    )
    drawLine(
        color = Color.White,
        start = start1,
        end = end1,
        strokeWidth = 4f,
        cap = StrokeCap.Round
    )
    drawLine(
        color = Color.White,
        start = start2,
        end = end2,
        strokeWidth = 4f,
        cap = StrokeCap.Round
    )
}

fun Color.mixedWith(other: Color, ratio: Float = 0.5f): Color {
    val hsv1 = FloatArray(3)
    val hsv2 = FloatArray(3)

    // Convert RGB to HSV
    android.graphics.Color.RGBToHSV(
        (this.red * 255).roundToInt(),
        (this.green * 255).roundToInt(),
        (this.blue * 255).roundToInt(),
        hsv1
    )
    android.graphics.Color.RGBToHSV(
        (other.red * 255).roundToInt(),
        (other.green * 255).roundToInt(),
        (other.blue * 255).roundToInt(),
        hsv2
    )

    // Interpolate between the two hues
    val hue = hsv1[0] + ratio * (hsv2[0] - hsv1[0])
    val saturation = hsv1[1] + ratio * (hsv2[1] - hsv1[1])
    val value = hsv1[2] + ratio * (hsv2[2] - hsv1[2])

    // Convert back to RGB
    val mixedColor = android.graphics.Color.HSVToColor(floatArrayOf(hue, saturation, value))
    return Color(
        red = android.graphics.Color.red(mixedColor) / 255f,
        green = android.graphics.Color.green(mixedColor) / 255f,
        blue = android.graphics.Color.blue(mixedColor) / 255f
    )
}

@Preview
@Composable
fun ThresholdIndicatorPreview() {
    Column {
        (0..9).forEach { i ->
            ThresholdIndicator(i.toFloat(), 8f)
        }
        ThresholdIndicator(0f, 0f)
        ThresholdIndicator(0.00000001f, 0f)
    }
}

// This function assumes basically only takes shades of green and red as input
//fun mixColors(c1: Color, c2: Color, ratio: Float): Color {
//    val diffR = c2.red - c1.red
//    val diffG = c2.green - c1.green
//    val diffB = c2.blue - c1.blue
//
//    val distR = abs(diffR)
//    val distG = abs(diffG)
//    val distB = abs(diffB)
//
//    val distSum = distR + distG + distB
//
//    val nDistR = distR / distSum
//    val nDistG = distG / distSum
//    val nDistB = distB / distSum
//
//    var ratioRem: Float = ratio
//
//    val nTravelR = min(nDistR, ratioRem)
//    ratioRem -= nTravelR
//
//    val nTravelG = min(nDistG, ratioRem)
//    ratioRem -= nTravelG
//
//    val nTravelB = min(nDistB, ratioRem)
//
//    val newR = c1.red + diffR * nTravelR / nDistR
//    val newG = c1.green + diffG * nTravelG /nDistG
//    val newB = c1.blue + diffB * nTravelB /nDistB
//
//    return Color(newR, newG, newB)
//}

//fun mixColors2(c1: Color, c2: Color, ratio: Float): Color {
//    val r = (c1.red * (1 - ratio) + c2.red * ratio).coerceIn(0f, 1f)
//    val g = (c1.green * (1 - ratio) + c2.green * ratio).coerceIn(0f, 1f)
//    val b = (c1.blue * (1 - ratio) + c2.blue * ratio).coerceIn(0f, 1f)
//    return Color(r, g, b)
//}

// This function assumes both input colors have one component = 0f and one component = 1f
//fun mixColors3(c1: Color, c2: Color, ratio: Float): Color {
//    val c1List = listOf(c1.red, c1.green, c1.blue)
//    val c2List = listOf(c2.red, c2.green, c2.blue)
//    val c1Vals = RGB.entries.associateWith { c1List[it.ordinal] }
//    val c1Order = RGB.entries.sortedBy { c1Vals[it] }
//
//    val visitOrder = RGB.entries.reversed()
//    var dir = -(-1f).pow(c1Order.isRotationOf(RGB.entries).toInt())
//
//    fun totalDistance(dir: Int, currentComponent: RGB, color: Color, res: Float): Float {
//        if (color == c2) {
//            return res
//        }
//        if (currentComponent)
//        val nextComponent = visitOrder[(currentComponent.ordinal + 1) % 3]
//        val newColor =
//    }
//
//
//    return Color(0,0,0)
//}
//
//enum class RGB {
//    RED, GREEN, BLUE
//}
//
//fun <T> List<T>.isRotationOf(other: List<T>): Boolean {
//    if (this.size != other.size) {
//        return false
//    }
//
//    val concatenated = this + this
//
//    return concatenated.windowed(other.size).any { window ->
//        window == other
//    }
//}
//
//fun Boolean.toInt() = if (this) 1 else 0