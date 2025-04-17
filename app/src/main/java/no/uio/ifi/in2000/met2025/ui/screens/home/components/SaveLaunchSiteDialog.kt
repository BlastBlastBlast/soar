// SaveLaunchSiteDialog.kt
package no.uio.ifi.in2000.met2025.ui.screens.home.components

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import no.uio.ifi.in2000.met2025.ui.AppOutlinedTextField
import no.uio.ifi.in2000.met2025.ui.theme.AppTypography

@Composable
fun SaveLaunchSiteDialog(
    launchSiteName: String,
    onNameChange: (String) -> Unit,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor   = MaterialTheme.colorScheme.primary,
        tonalElevation   = AlertDialogDefaults.TonalElevation,

        title = {
            CompositionLocalProvider(LocalContentColor provides MaterialTheme.colorScheme.onPrimary) {
                Text(
                    text  = "Save Launch Site",
                    style = AppTypography.headlineSmall
                )
            }
        },

        text = {
            CompositionLocalProvider(LocalContentColor provides MaterialTheme.colorScheme.onPrimary) {
                Column {
                    Text(
                        text  = "Enter a name for this launch site:",
                        style = AppTypography.bodyMedium
                    )
                    Spacer(Modifier.height(8.dp))
                    AppOutlinedTextField(
                        value         = launchSiteName,
                        onValueChange = onNameChange,
                        label         = { Text("Site Name") },
                        modifier      = Modifier.fillMaxWidth()
                    )
                }
            }
        },

        confirmButton = {
            Button(
                onClick      = onConfirm,
                colors       = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.onPrimary,
                    contentColor   = MaterialTheme.colorScheme.primary
                )
            ) {
                Text("Save")
            }
        },

        dismissButton = {
            TextButton(
                onClick      = onDismiss,
                colors       = ButtonDefaults.textButtonColors(
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
            ) {
                Text("Cancel")
            }
        }
    )
}

