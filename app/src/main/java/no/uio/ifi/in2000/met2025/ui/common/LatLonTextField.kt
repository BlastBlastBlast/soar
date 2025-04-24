package no.uio.ifi.in2000.met2025.ui.common

// ui.common/LatLonInputField.kt
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.input.ImeAction
import no.uio.ifi.in2000.met2025.ui.theme.LocalAppCursorColor
import no.uio.ifi.in2000.met2025.ui.theme.WarmOrange

/**
 * A single‐line input for latitude/longitude pairs.
 *
 * @param text          The current contents (e.g. `"59.9452, 10.7153"`).
 * @param onTextChange  Called whenever the user edits.
 * @param onDone        Called when the user taps the check-icon or the IME “Done” key.
 * @param modifier      Any layout modifiers.
 */
@Composable
fun LatLonInputField(
    text: String,
    onTextChange: (String) -> Unit,
    onDone: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        tonalElevation  = 2.dp,
        shadowElevation = 4.dp,
        shape           = RoundedCornerShape(8.dp),
        modifier        = modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        OutlinedTextField(
            value               = text,
            onValueChange       = onTextChange,
            modifier            = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            label               = { Text("Coordinates", fontSize = 12.sp) },
            singleLine          = true,
            trailingIcon        = {
                IconButton(onClick = onDone) {
                    Icon(Icons.Default.Check, contentDescription = "Done")
                }
            },
            keyboardOptions     = KeyboardOptions.Default.copy(imeAction = ImeAction.Done),
            keyboardActions     = KeyboardActions(onDone = { onDone() }),
            colors        = OutlinedTextFieldDefaults.colors(
                // borders & caret
                focusedBorderColor    = WarmOrange,
                unfocusedBorderColor  = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.5f),
                cursorColor           = LocalAppCursorColor.current,

                // make the floating label always onSurface (full alpha when focused)
                focusedLabelColor     = MaterialTheme.colorScheme.onPrimary,
                unfocusedLabelColor   = MaterialTheme.colorScheme.onSurface,

                // make the placeholder / hint text onSurface too
                //placeholderColor      = MaterialTheme.colorScheme.onSurface
            )
        )
    }
}
