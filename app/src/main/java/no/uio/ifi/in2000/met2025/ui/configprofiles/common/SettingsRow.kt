package no.uio.ifi.in2000.met2025.ui.configprofiles.common

import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import no.uio.ifi.in2000.met2025.ui.AppOutlinedTextField
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
        modifier          = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        AppOutlinedTextField(
            value         = value,
            onValueChange = onValueChange,
            label         = { Text(label, color = MaterialTheme.colorScheme.onPrimary) }, // label in onPrimary
            modifier      = Modifier.weight(1f)
        )
        Spacer(Modifier.width(8.dp))
        ColoredSwitch(
            checked        = enabled,
            onCheckedChange= onEnabledChange
        )
    }
}

