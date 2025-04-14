package no.uio.ifi.in2000.met2025.ui.screens.weathercardscreen.components.windcomponents

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun WindLayerHeader(
    altitudeText: String,
    windSpeedText: String,
    windDirectionText: String,
    modifier: Modifier,
    style: androidx.compose.ui.text.TextStyle
) {
    Row(modifier = modifier) {
        Text(
            text = altitudeText,
            style = style,
            modifier = Modifier.weight(1f)
        )

        Text(
            text = windDirectionText,
            style = style,
            modifier = Modifier.weight(1f)
        )

        Text(
                text = windSpeedText,
        style = style,
        modifier = Modifier.weight(1f)
        )
    }
}