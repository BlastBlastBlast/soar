package no.uio.ifi.in2000.met2025.ui.common

import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import no.uio.ifi.in2000.met2025.ui.theme.WarmOrange
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription

@Composable
fun ColoredSwitch(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Switch(
        modifier = Modifier.semantics {
            role = Role.Switch
            contentDescription = "Toggle filter"
            stateDescription   = if (checked) "On" else "Off"
        },
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