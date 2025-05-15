package no.uio.ifi.in2000.met2025.ui.screens.config.weatherConfig

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
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
import no.uio.ifi.in2000.met2025.data.local.database.WeatherConfig
import no.uio.ifi.in2000.met2025.ui.common.ConfirmationDialog
import no.uio.ifi.in2000.met2025.ui.theme.IconGreen
import no.uio.ifi.in2000.met2025.ui.theme.IconRed
import no.uio.ifi.in2000.met2025.ui.theme.WarmOrange

/**
 * WeatherConfigListItem
 *
 * Displays a card for a WeatherConfig with:
 * - The config name
 * - Tap anywhere on the card to select this config
 * - Edit and Delete buttons for non-default configs
 * - A confirmation dialog before deletion
 */
@Composable
fun WeatherConfigListItem(
    weatherConfig: WeatherConfig,
    onClick: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val shape = RoundedCornerShape(8.dp)
    var showConfirmationDialog by rememberSaveable { mutableStateOf(false) }

    Surface(
        modifier        = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable(onClick = onClick)
            .semantics {
                role = Role.Button
                contentDescription = buildString {
                    append("${weatherConfig.name} configuration. ")
                    if (weatherConfig.isDefault) append("Default configuration. ")
                    append("Tap to select.")
                }
            },
        color           = MaterialTheme.colorScheme.surfaceVariant,
        tonalElevation  = 8.dp,
        shadowElevation = 10.dp,
        shape           = shape,
        border = BorderStroke(
            width = 1.dp,
            color = if (weatherConfig.isDefault) WarmOrange else Color.Black
        )
    ) {
        Row(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text  = weatherConfig.name,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onPrimary
            )
            Row {
                if (!weatherConfig.isDefault) {
                    IconButton(onClick = onEdit,
                        modifier = Modifier.semantics {
                            role = Role.Button
                            contentDescription = "Edit ${weatherConfig.name}"
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
                            contentDescription = "Delete ${weatherConfig.name}"
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
                text = "Are you sure you want to delete ${weatherConfig.name}?",
                confirmText = "Delete",
                dismissText = "Cancel",
                onConfirm = onDelete,
                onDismiss = { showConfirmationDialog = false }
            )
        }
    }
}
