package no.uio.ifi.in2000.met2025.ui.screens.weatherScreen.components.windcomponents

import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection

class CustomRoundedCornerShape(private val cornerSize: Dp) : Shape {
    override fun createOutline(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density
    ): Outline {
        val cornerRadius = with(density) { cornerSize.toPx() }
        return Outline.Generic(Path().apply {
            // Top-left corner
            moveTo(0f, cornerRadius)
            arcTo(
                rect = androidx.compose.ui.geometry.Rect(0f, 0f, cornerRadius * 2, cornerRadius * 2),
                startAngleDegrees = 180f,
                sweepAngleDegrees = 90f,
                forceMoveTo = false
            )
            // Top horizontal line
            lineTo(size.width - cornerRadius, 0f)
            // Top-right corner
            arcTo(
                rect = androidx.compose.ui.geometry.Rect(size.width - cornerRadius * 2, 0f, size.width, cornerRadius * 2),
                startAngleDegrees = 270f,
                sweepAngleDegrees = 90f,
                forceMoveTo = false
            )
//            // Bottom-right corner
//            moveTo(size.width, size.height - cornerRadius)
//            arcTo(
//                rect = androidx.compose.ui.geometry.Rect(size.width - cornerRadius * 2, size.height - cornerRadius * 2, size.width, size.height),
//                startAngleDegrees = 0f,
//                sweepAngleDegrees = 90f,
//                forceMoveTo = false
//            )
//            // Bottom horizontal line
//            lineTo(cornerRadius, size.height)
//            // Bottom-left corner
//            arcTo(
//                rect = androidx.compose.ui.geometry.Rect(0f, size.height - cornerRadius * 2, cornerRadius * 2, size.height),
//                startAngleDegrees = 90f,
//                sweepAngleDegrees = 90f,
//                forceMoveTo = false
//            )
        })
    }
}