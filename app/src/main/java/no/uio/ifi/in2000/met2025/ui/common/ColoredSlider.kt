package no.uio.ifi.in2000.met2025.ui.common

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import no.uio.ifi.in2000.met2025.ui.theme.WarmOrange
import no.uio.ifi.in2000.met2025.ui.theme.White

@Composable
fun ColoredSlider(
    value: Float, onValueChange: (Float)->Unit, modifier: Modifier = Modifier
) {
    Slider(
        value           = value,
        onValueChange   = onValueChange,
        valueRange      = 4f..72f,
        modifier        = modifier,
        colors          = SliderDefaults.colors(
            thumbColor        = White,
            activeTrackColor  = WarmOrange,
            inactiveTrackColor= MaterialTheme.colorScheme.onPrimary
        )
    )
}