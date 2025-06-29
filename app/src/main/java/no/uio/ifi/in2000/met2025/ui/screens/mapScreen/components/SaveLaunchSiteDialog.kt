// SaveLaunchSiteDialog.kt
package no.uio.ifi.in2000.met2025.ui.screens.mapScreen.components

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import no.uio.ifi.in2000.met2025.ui.screens.mapScreen.MapScreenViewModel
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import no.uio.ifi.in2000.met2025.ui.common.AppOutlinedTextField
import no.uio.ifi.in2000.met2025.ui.theme.AppTypography

@Composable
fun SaveLaunchSiteDialog(
    launchSiteName: String,
    onNameChange: (String) -> Unit,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
    updateStatus: MapScreenViewModel.UpdateStatus
) {
    // only allow dismiss when there's no error
    //val canDismiss = updateStatus !is MapScreenViewModel.UpdateStatus.Error

    AlertDialog(
        onDismissRequest = { onDismiss() },
        containerColor   = MaterialTheme.colorScheme.primary,
        tonalElevation   = AlertDialogDefaults.TonalElevation,
        title = {
            CompositionLocalProvider(LocalContentColor provides MaterialTheme.colorScheme.onPrimary) {
                Text(
                    text = if (updateStatus is MapScreenViewModel.UpdateStatus.Error)
                        "Name Already Exists" else "Save Launch Site",
                    modifier = Modifier.semantics { heading() },
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
                        onValueChange = {onNameChange(it)},
                        labelText         = "Site Name",
                        modifier      = Modifier.fillMaxWidth()
                            .semantics { contentDescription = "Launch site name input field" }
                    )
                    if (updateStatus is MapScreenViewModel.UpdateStatus.Error) {
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
                enabled = launchSiteName.isNotBlank(),
                modifier = Modifier.semantics {
                    contentDescription = "Save launch site"
                    role = Role.Button
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.onPrimary,
                    contentColor   = MaterialTheme.colorScheme.primary
                )
            ) {
                Text("Save")
            }
        },

        dismissButton = {
            TextButton(
                onClick = { onDismiss() },
                modifier = Modifier.semantics {
                    contentDescription = "Cancel saving launch site"
                    role = Role.Button
                },
                colors = ButtonDefaults.textButtonColors(
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
            ) {
                Text("Cancel")
            }
        }
    )
}
