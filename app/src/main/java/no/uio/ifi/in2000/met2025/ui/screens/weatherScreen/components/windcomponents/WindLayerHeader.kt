package no.uio.ifi.in2000.met2025.ui.screens.weatherScreen.components.windcomponents

import androidx.compose.foundation.layout.Row
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle

/**
 * Defines a reusable header row for wind data tables, used in the isobaric wind layer UI.
 */

@Composable
fun WindLayerHeader(
    altitudeText: String,
    windSpeedText: String,
    windDirectionText: String,
    modifier: Modifier,
    style: TextStyle
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