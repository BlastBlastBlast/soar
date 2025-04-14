package no.uio.ifi.in2000.met2025.ui.screens.weathercardscreen.components.filter

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Text

@Composable
fun FilterSliderHours(
    hoursToShow: Float,
    onHoursChanged: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(
            text = "Show forecast for ${hoursToShow.toInt()} hours",
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(vertical = 8.dp)
        )
        Slider(
            value = hoursToShow,
            onValueChange = onHoursChanged,
            valueRange = 4f..72f,
            steps = (72 - 4 - 1),
            modifier = Modifier.fillMaxWidth()
        )
    }
}