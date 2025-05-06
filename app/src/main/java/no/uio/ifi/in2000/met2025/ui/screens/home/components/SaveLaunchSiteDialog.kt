// SaveLaunchSiteDialog.kt
package no.uio.ifi.in2000.met2025.ui.screens.home.components

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import no.uio.ifi.in2000.met2025.ui.screens.home.HomeScreenViewModel
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import no.uio.ifi.in2000.met2025.ui.common.AppOutlinedTextField
import no.uio.ifi.in2000.met2025.ui.theme.AppTypography

@Composable
fun SaveLaunchSiteDialog(
    launchSiteName: String,
    onNameChange: (String) -> Unit,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
    updateStatus: HomeScreenViewModel.UpdateStatus
) {
    // only allow dismiss when there's no error
    val canDismiss = updateStatus !is HomeScreenViewModel.UpdateStatus.Error

    AlertDialog(
        onDismissRequest = {
            if (canDismiss) {
                onDismiss()
            }
        },
        containerColor   = MaterialTheme.colorScheme.primary,
        tonalElevation   = AlertDialogDefaults.TonalElevation,

        title = {
            CompositionLocalProvider(LocalContentColor provides MaterialTheme.colorScheme.onPrimary) {
                Text(
                    text  = if (updateStatus is HomeScreenViewModel.UpdateStatus.Error)
                        "Name Already Exists"
                    else
                        "Save Launch Site",
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
                    if (updateStatus is HomeScreenViewModel.UpdateStatus.Error) {
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text  = updateStatus.message,
                            color = MaterialTheme.colorScheme.error,
                            style = AppTypography.bodySmall
                        )
                    }
                }
            }
        },

        confirmButton = {
            Button(
                onClick = onConfirm,
                enabled = launchSiteName.isNotBlank(), // prevent empty name
                colors  = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.onPrimary,
                    contentColor   = MaterialTheme.colorScheme.primary
                )
            ) {
                Text("Save")
            }
        },

        dismissButton = {
            TextButton(
                onClick = {
                    if (canDismiss) onDismiss()
                },
                enabled = canDismiss,
                colors  = ButtonDefaults.textButtonColors(
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
            ) {
                Text("Cancel")
            }
        }
    )
}
