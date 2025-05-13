package no.uio.ifi.in2000.met2025.ui.screens.mapScreen.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.selection.TextSelectionColors
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import no.uio.ifi.in2000.met2025.domain.helpers.parseLatLon
import no.uio.ifi.in2000.met2025.ui.theme.WarmOrange

@Composable
fun LatLonDisplay(
    coordinates: String,
    onCoordinatesChange: (String) -> Unit,
    onDone: () -> Unit,
    modifier: Modifier = Modifier
) {
    var internalText by remember { mutableStateOf(coordinates) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // Keep internal mirror in sync with external coordinates
    LaunchedEffect(coordinates) {
        internalText = coordinates
        errorMessage = null
    }

    // Strict lat,lon regex: 1–3 digits, “.”, 1–8 digits, “, ”, 1–3 digits, “.”, 1–8 digits
    val coordinateRegex = Regex("^[0-9]{1,3}\\.[0-9]{1,8}, [0-9]{1,3}\\.[0-9]{1,8}$")

    Surface(
        shape           = RoundedCornerShape(8.dp),
        tonalElevation  = 2.dp,
        shadowElevation = 4.dp,
        color           = MaterialTheme.colorScheme.surfaceVariant,
        modifier        = modifier
            .fillMaxWidth()
            .padding(8.dp)
    ) {
        Column {
            val selectionColors = TextSelectionColors(
                handleColor = WarmOrange,
                backgroundColor = WarmOrange.copy(alpha = 0.4f)
            )

            OutlinedTextField(
                value = internalText,
                onValueChange = { new ->
                    // always accept the new text
                    internalText = new
                    errorMessage = null
                    onCoordinatesChange(new)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                label = { Text(text = "Coordinates", fontSize = 12.sp) },
                singleLine = true,
                trailingIcon = {
                    // only enabled when it fully matches
                    IconButton(
                        onClick = {
                            if (coordinateRegex.matches(internalText)) {
                                // re-parse and format, or show error
                                parseLatLon(internalText)?.let { (lat, lon) ->
                                    val formatted = "%.4f, %.4f".format(lat, lon)
                                    internalText = formatted
                                    onCoordinatesChange(formatted)
                                    errorMessage = null
                                    onDone()
                                } ?: run {
                                    errorMessage = "Invalid coordinates"
                                }
                            } else {
                                errorMessage = "Use format: 12.34567890, 98.76543210"
                            }
                        }
                    ) {
                        Icon(Icons.Default.Check, contentDescription = "Done")
                    }
                },
                keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = {
                    if (coordinateRegex.matches(internalText)) {
                        // re-parse and format, or show error
                        parseLatLon(internalText)?.let { (lat, lon) ->
                            val formatted = "%.4f, %.4f".format(lat, lon)
                            internalText = formatted
                            onCoordinatesChange(formatted)
                            errorMessage = null
                            onDone()
                        } ?: run {
                            errorMessage = "Invalid coordinates"
                        }
                    } else {
                        errorMessage = "Use format: 12.34567890, 98.76543210"
                    }
                }),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = WarmOrange,
                    unfocusedBorderColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.5f),
                    cursorColor = WarmOrange,
                    selectionColors = selectionColors,
                    focusedLabelColor = MaterialTheme.colorScheme.onPrimary,
                    unfocusedLabelColor = MaterialTheme.colorScheme.onSurface,
                )
            )
            errorMessage?.let { err ->
                Text(
                    text = err,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(start = 16.dp, top = 4.dp)
                )
            }
        }
    }
}
