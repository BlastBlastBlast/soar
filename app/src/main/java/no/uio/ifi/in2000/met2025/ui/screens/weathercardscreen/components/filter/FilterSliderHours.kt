package no.uio.ifi.in2000.met2025.ui.screens.weathercardscreen.components.filter

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Text
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import no.uio.ifi.in2000.met2025.ui.common.ColoredSlider

@Composable
fun FilterSliderHours(
    hoursToShow: Float,
    onHoursChanged: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    val screenWidth = LocalConfiguration.current.screenWidthDp.dp
    val minW = screenWidth * 0.4f
    val maxW = screenWidth * 0.8f

    ElevatedCard(
        modifier = modifier
            .widthIn(min = minW, max = maxW)
            .semantics {
                contentDescription = "Forecast hours slider, currently set to ${hoursToShow.toInt()} hours"
            },
        shape     = RoundedCornerShape(8.dp),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp, pressedElevation = 4.dp),
        colors    = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
            contentColor   = MaterialTheme.colorScheme.onSurface
        )
    ) {
        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                text  = "Show forecast for ${hoursToShow.toInt()} hours",
                style = MaterialTheme.typography.bodyMedium
            )
            ColoredSlider(
                value      = hoursToShow,
                onValueChange = onHoursChanged,
                modifier   = Modifier.fillMaxWidth()
            )
        }
    }
}
