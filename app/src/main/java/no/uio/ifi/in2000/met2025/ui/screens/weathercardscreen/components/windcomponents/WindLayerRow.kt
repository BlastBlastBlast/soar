package no.uio.ifi.in2000.met2025.ui.screens.weathercardscreen.components.windcomponents

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun WindLayerRow(
    altitudeText: String,
    windSpeedText: String,
    windDirectionText: String,
    style: androidx.compose.ui.text.TextStyle
) {
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
        Text(
            text = altitudeText,
            style = style,
            modifier = Modifier.weight(1f)
        )
        Text(
            text = windSpeedText,
            style = style,
            modifier = Modifier.weight(1f)
        )
        Text( //TODO: Add wind direction icon. Pointing downwards at 0° rotating clockwise
            text = windDirectionText,
            style = style,
            modifier = Modifier.weight(1f)
        )
    }
}