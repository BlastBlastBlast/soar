package no.uio.ifi.in2000.met2025.ui.screens.config.weatherConfig.common

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import no.uio.ifi.in2000.met2025.ui.common.AppOutlinedNumberField
import no.uio.ifi.in2000.met2025.ui.common.ColoredSwitch

@Composable
fun SettingRow(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    enabled: Boolean,
    onEnabledChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier          = modifier
            .semantics {
                contentDescription =
                    "$label value $value, ${if (enabled) "enabled" else "disabled"}"
            },
        verticalAlignment = Alignment.CenterVertically
    ) {
        AppOutlinedNumberField(
            value         = value,
            onValueChange = onValueChange,
            labelText     = label,
            modifier      = Modifier
                .weight(1f)
                .semantics {
                    contentDescription = "$label input field, current value $value"
                }
        )
        Spacer(Modifier.width(8.dp))
        ColoredSwitch(
            checked        = enabled,
            onCheckedChange= onEnabledChange
        )
    }
}

