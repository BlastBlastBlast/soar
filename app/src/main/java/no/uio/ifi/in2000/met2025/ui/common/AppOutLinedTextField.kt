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
import androidx.compose.material3.Text
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.input.ImeAction


@Composable
fun AppOutlinedTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    labelText: String,
    singleLine: Boolean = true,
    enabled: Boolean = true,
    readOnly: Boolean = false,
    filterRegex: Regex = Regex(".*")
) {
    val selectionColors = TextSelectionColors(
        handleColor     = WarmOrange,
        backgroundColor = WarmOrange.copy(alpha = 0.4f)
    )
    val keyboardController = LocalSoftwareKeyboardController.current

    OutlinedTextField(
        value         = value,
        onValueChange = { new ->
            if (filterRegex.matches(new)) {
                onValueChange(new)
            }
        },
        modifier      = modifier
            .semantics {
                contentDescription = labelText
                stateDescription = if (value.isBlank()) "Empty" else value
            },
        label         = { Text(labelText) },
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
            onDone = { keyboardController?.hide() }
        )
    )
}

