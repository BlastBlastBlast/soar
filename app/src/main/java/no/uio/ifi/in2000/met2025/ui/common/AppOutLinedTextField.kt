package no.uio.ifi.in2000.met2025.ui.common

import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import no.uio.ifi.in2000.met2025.ui.theme.LocalAppCursorColor
import no.uio.ifi.in2000.met2025.ui.theme.WarmOrange
import androidx.compose.foundation.text.selection.TextSelectionColors
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction


@Composable
fun AppOutlinedTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    label: @Composable (() -> Unit)? = null,
    singleLine: Boolean = true,
    enabled: Boolean = true,
    readOnly: Boolean = false
) {
    val selectionColors = TextSelectionColors(
        handleColor     = WarmOrange,
        backgroundColor = WarmOrange.copy(alpha = 0.4f)
    )

    val keyboardController = LocalSoftwareKeyboardController.current

    OutlinedTextField(
        value         = value,
        onValueChange = onValueChange,
        modifier      = modifier,
        label         = label,
        singleLine    = singleLine,
        enabled       = enabled,
        readOnly      = readOnly,
        colors        = OutlinedTextFieldDefaults.colors(
            focusedBorderColor    = WarmOrange,
            unfocusedBorderColor  = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.5f),
            cursorColor           = LocalAppCursorColor.current,
            selectionColors       = selectionColors,
            focusedLabelColor     = MaterialTheme.colorScheme.onPrimary,
            unfocusedLabelColor   = MaterialTheme.colorScheme.onSurface,
        ),
        keyboardOptions = KeyboardOptions.Default.copy(
            imeAction = ImeAction.Done
        ),
        keyboardActions = KeyboardActions(
            onDone = {
                keyboardController?.hide()
            }
        )
    )
}
