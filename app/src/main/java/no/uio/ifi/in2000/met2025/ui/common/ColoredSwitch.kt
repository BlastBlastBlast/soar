package no.uio.ifi.in2000.met2025.ui.common

import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import no.uio.ifi.in2000.met2025.ui.theme.WarmOrange
import androidx.compose.material3.MaterialTheme

@Composable
fun ColoredSwitch(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Switch(
        checked = checked,
        onCheckedChange = onCheckedChange,
        colors = SwitchDefaults.colors(
            checkedThumbColor   = Color.White,
            uncheckedThumbColor = Color.LightGray,
            checkedTrackColor   = WarmOrange,
            uncheckedTrackColor = Color.Black,
        )
    )
}