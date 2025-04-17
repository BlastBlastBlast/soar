// SaveLaunchSiteDialog.kt
package no.uio.ifi.in2000.met2025.ui.screens.home.components

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import no.uio.ifi.in2000.met2025.ui.screens.home.HomeScreenViewModel

@Composable
fun SaveLaunchSiteDialog(
    launchSiteName: String,
    onNameChange: (String) -> Unit,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
    updateStatus: HomeScreenViewModel.UpdateStatus
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Save Launch Site") },
        text = {
            Column {
                Text("Enter a name for this launch site:")
                OutlinedTextField(
                    value = launchSiteName,
                    onValueChange = onNameChange,
                    label = { Text("Site Name") }
                )
                if (updateStatus is HomeScreenViewModel.UpdateStatus.Error) {
                    Text(
                        text = updateStatus.message,
                        color = Color.Red
                    )
                }
            }
        },
        confirmButton = {
            Button(onClick = onConfirm) {
                Text("Save")
            }
        },
        dismissButton = {
            Button(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
