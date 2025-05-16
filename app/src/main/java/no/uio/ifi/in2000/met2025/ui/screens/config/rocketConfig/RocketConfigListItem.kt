package no.uio.ifi.in2000.met2025.ui.screens.config.rocketConfig

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import no.uio.ifi.in2000.met2025.data.local.database.RocketConfig
import no.uio.ifi.in2000.met2025.ui.common.ConfirmationDialog
import no.uio.ifi.in2000.met2025.ui.theme.IconGreen
import no.uio.ifi.in2000.met2025.ui.theme.IconRed
import no.uio.ifi.in2000.met2025.ui.theme.WarmOrange

/**
 * RocketConfigItem
 *
 * Displays a card for a RocketConfig with:
 * - The config name
 * - Tap anywhere on the card to select this config
 * - Edit and Delete buttons for non-default configs
 * - A confirmation dialog before deletion
 */
@Composable
fun RocketConfigItem(
    rocketConfig: RocketConfig,
    onClick: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val shape = RoundedCornerShape(8.dp)
    var showConfirmationDialog by rememberSaveable { mutableStateOf(false) }

    // Each item on pure white primary
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable(onClick = onClick)
            .semantics {
                role = Role.Button
                contentDescription = buildString {
                    append("Configuration ${rocketConfig.name}. ")
                    if (rocketConfig.isDefault) append("Default configuration.")
                }
            },
        color           = MaterialTheme.colorScheme.surfaceVariant,
        tonalElevation  = 8.dp,
        shadowElevation = 10.dp,
        shape           = shape,
        border = BorderStroke(
            width = 1.dp,
            color = if (rocketConfig.isDefault) WarmOrange else Color.Black
        )
    ) {
        Row(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment     = Alignment.CenterVertically
        ) {
            Column(Modifier.weight(1f)) {
                Text(
                    text  = rocketConfig.name,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onPrimary
                )
                if (rocketConfig.isDefault) {
                    Text(
                        text  = "Default Configuration",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                }
            }
            Row {
                if (!rocketConfig.isDefault) {
                    IconButton(onClick = onEdit,
                        modifier = Modifier.semantics {
                        role = Role.Button
                        contentDescription = "Edit ${rocketConfig.name}"
                    }) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit",
                            tint = IconGreen
                        )
                    }
                    IconButton(onClick = { showConfirmationDialog = true },
                        modifier = Modifier.semantics {
                            role = Role.Button
                            contentDescription = "Delete ${rocketConfig.name}"
                        }) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete",
                            tint = IconRed
                        )
                    }
                }
            }
        }
        if (showConfirmationDialog) {
            ConfirmationDialog(
                title = "Delete Configuration",
                text = "Are you sure you want to delete ${rocketConfig.name}?",
                confirmText = "Delete",
                dismissText = "Cancel",
                onConfirm = onDelete,
                onDismiss = { showConfirmationDialog = false }
            )
        }
    }
}