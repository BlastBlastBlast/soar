package no.uio.ifi.in2000.met2025.ui.screens.weathercardscreen.components.filter

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.platform.LocalConfiguration

@Composable
fun FilterSliderHours(
    hoursToShow: Float,
    onHoursChanged: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp.dp
    val minWidth = screenWidth * 0.4f
    val maxWidth = screenWidth * 0.8f

    Column(
        modifier = modifier
            .shadow(elevation = 8.dp, shape = RoundedCornerShape(8.dp))
            .background(
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
                shape = RoundedCornerShape(8.dp)
            )
            .padding(8.dp)
            .widthIn(min = minWidth, max = maxWidth)
    ) {
        Text(
            text = "Show forecast for ${hoursToShow.toInt()} hours",
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(bottom = 4.dp)
        )
        Slider(
            value = hoursToShow,
            onValueChange = onHoursChanged,
            valueRange = 4f..72f,
            // Removed the steps property for a continuous slider.
            modifier = Modifier.fillMaxWidth()
        )
    }
}