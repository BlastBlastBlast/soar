package no.uio.ifi.in2000.met2025.ui

import androidx.compose.foundation.text.selection.LocalTextSelectionColors
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.TextFieldColors
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import no.uio.ifi.in2000.met2025.ui.theme.LocalAppCursorColor
import no.uio.ifi.in2000.met2025.ui.theme.WarmOrange
import androidx.compose.foundation.text.selection.TextSelectionColors


@Composable
fun AppOutlinedTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    label: @Composable (() -> Unit)? = null,
    singleLine: Boolean = false,
    enabled: Boolean = true,
    readOnly: Boolean = false
) {
    // 1) selection‐handle & highlight colors
    val selectionColors = TextSelectionColors(
        handleColor     = WarmOrange,
        backgroundColor = WarmOrange.copy(alpha = 0.4f)
    )

    OutlinedTextField(
        value         = value,
        onValueChange = onValueChange,
        modifier      = modifier,
        label         = label,
        singleLine    = singleLine,
        enabled       = enabled,
        readOnly      = readOnly,
        colors        = OutlinedTextFieldDefaults.colors(
            // borders & caret
            focusedBorderColor    = WarmOrange,
            unfocusedBorderColor  = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.5f),
            cursorColor           = LocalAppCursorColor.current,
            selectionColors       = selectionColors,

            // make the floating label always onSurface (full alpha when focused)
            focusedLabelColor     = MaterialTheme.colorScheme.onPrimary,
            unfocusedLabelColor   = MaterialTheme.colorScheme.onSurface,

            // make the placeholder / hint text onSurface too
            //placeholderColor      = MaterialTheme.colorScheme.onSurface
        )
    )
}
