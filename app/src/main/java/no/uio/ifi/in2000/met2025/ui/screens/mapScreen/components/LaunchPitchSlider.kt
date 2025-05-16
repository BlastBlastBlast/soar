package no.uio.ifi.in2000.met2025.ui.screens.mapScreen.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.dp
import no.uio.ifi.in2000.met2025.ui.theme.WarmOrange

/**
 * A slider for adjusting the pitch angle of a launch.
 *
 * @param initialAngle The initial angle of the slider.
 * @param onAngleChange Callback function to handle angle changes.
 */
@Composable
fun LaunchPitchSlider(
    initialAngle: Float = 80f,
    onAngleChange: (Float) -> Unit = {}
) {
    var pitchAngle by remember { mutableFloatStateOf(initialAngle) }

    // Theme colors
    val surfaceColor = MaterialTheme.colorScheme.surface
    val onSurfaceColor = MaterialTheme.colorScheme.onSurface
    val screenWidth = LocalConfiguration.current.screenWidthDp.dp

    Column(
//        modifier = Modifier
//            .padding(16.dp),
        horizontalAlignment = Alignment.Start
    ) {
        Text(
            text = "Pitch: ${pitchAngle.toInt()}°",
            style = MaterialTheme.typography.bodyMedium,
            color = onSurfaceColor,
            modifier = Modifier
                .background(surfaceColor, RoundedCornerShape(4.dp))
                .padding(horizontal = 8.dp, vertical = 4.dp)
        )

        Slider(
            value = pitchAngle,
            onValueChange = {
                pitchAngle = it
                onAngleChange(it)
            },
            valueRange = 80f..90f,
            colors = SliderDefaults.colors(
                thumbColor = WarmOrange,
                activeTrackColor = WarmOrange,
                inactiveTrackColor = Color(0xFFBDBDBD),
            ),
            modifier = Modifier
                .width(screenWidth / 2) // Set slider width to half the screen
                .padding(8.dp) // Inner padding for the slider
        )
    }
}