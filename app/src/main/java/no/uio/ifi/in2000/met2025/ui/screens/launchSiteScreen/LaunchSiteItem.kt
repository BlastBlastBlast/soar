package no.uio.ifi.in2000.met2025.ui.screens.launchSiteScreen

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.MaterialTheme
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import no.uio.ifi.in2000.met2025.data.local.database.LaunchSite
import no.uio.ifi.in2000.met2025.ui.common.AppOutlinedTextField
import no.uio.ifi.in2000.met2025.ui.common.ConfirmationDialog
import no.uio.ifi.in2000.met2025.ui.theme.IconGreen
import no.uio.ifi.in2000.met2025.ui.theme.IconRed
import no.uio.ifi.in2000.met2025.ui.theme.WarmOrange

/*
 * This composable displays an interactive card representing a single launch site.
 * It supports viewing, editing, and deleting launch site data (name, latitude, longitude).
 */


@Composable
fun LaunchSiteItem(
    site: LaunchSite,
    onDelete: () -> Unit,
    onEdit: (LaunchSite) -> Unit,
    updateStatus: LaunchSiteViewModel.UpdateStatus,
    viewModel: LaunchSiteViewModel,
    listState: LazyListState,
    itemIndex: Int
) {
    // UI state variables for edit mode and field content
    var isEditing by rememberSaveable { mutableStateOf(false) }
    var name by remember { mutableStateOf(site.name) }
    var latitudeText by remember { mutableStateOf(site.latitude.toString()) }
    var longitudeText by remember { mutableStateOf(site.longitude.toString()) }
    val coroutineScope = rememberCoroutineScope()
    val orangeStripHeight = 16.dp
    val cornerShape = RoundedCornerShape(8.dp)
    var showConfirmationDialog by rememberSaveable { mutableStateOf(false) }

    // Automatically exit editing mode if update succeeded for this item
    LaunchedEffect(updateStatus) {
        if (updateStatus is LaunchSiteViewModel.UpdateStatus.Success && updateStatus.siteUid == site.uid && isEditing) {
            isEditing = false
            viewModel.setUpdateStatusToIdle()
        }
    }

    Surface(
        modifier        = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable(onClick = onDelete)   // or whatever your click target is
            .semantics {
                role = Role.Button
                contentDescription = if (!isEditing) {
                    "${site.name}, latitude ${"%.4f".format(site.latitude)}, longitude ${"%.4f".format(site.longitude)}. Double-tap to edit."
                } else {
                    "Editing site ${site.name}. Double-tap to save or cancel."
                }
            },
        color           = MaterialTheme.colorScheme.surfaceVariant,
        tonalElevation  = 8.dp,
        shadowElevation = 10.dp,
        shape           = cornerShape,
        border = BorderStroke(
            width = 1.dp,
            color = Color.Black
        )
    ) {
        Column(Modifier.clip(cornerShape)) {
            // Top decorative strip
            Box(
                Modifier
                    .fillMaxWidth()
                    .height(orangeStripHeight)
                    .background(
                        WarmOrange,
                        shape = RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp)
                    )
            )

            // Main content area
            Box(
                Modifier
                    .fillMaxWidth()
                    .background(
                        MaterialTheme.colorScheme.surface,
                        shape = RoundedCornerShape(bottomStart = 8.dp, bottomEnd = 8.dp)
                    )
                    .padding(horizontal = 8.dp, vertical = 12.dp)
            ) {
                if (!isEditing) {
                    Row(
                        Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(Modifier.weight(1f)) {
                            Text(site.name, style = MaterialTheme.typography.bodyLarge)
                            Text(
                                "Lat: %.4f   Lon: %.4f".format(site.latitude, site.longitude),
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                        Row {
                            // Enable edit mode and scroll to item
                            IconButton(onClick = {
                                isEditing = true
                                coroutineScope.launch {
                                    listState.animateScrollToItem(itemIndex)
                                }
                            },  modifier = Modifier.semantics { contentDescription = "Edit site" }

                            ) {
                                Icon(
                                    Icons.Default.Edit,
                                    contentDescription = "Edit",
                                    tint = IconGreen
                                )
                            }
                            // Open confirmation dialog
                            IconButton(onClick = { showConfirmationDialog = true },
                                modifier = Modifier.semantics { contentDescription = "Delete site" }
                            ) {
                                Icon(
                                    Icons.Default.Delete,
                                    contentDescription = "Delete",
                                    tint = IconRed
                                )
                            }
                        }
                    }
                } else {
                    // Edit mode
                    Row(
                        Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(Modifier.weight(1f)) {
                            AppOutlinedTextField(
                                value = name,
                                onValueChange = {
                                    name = it
                                    viewModel.checkNameAvailability(it)
                                },
                                labelText = "Name",
                                modifier = Modifier.fillMaxWidth()
                            )
                            // Show validation error if name taken
                            if (updateStatus is LaunchSiteViewModel.UpdateStatus.Error && isEditing && name != site.name) {
                                Text(
                                    text = updateStatus.message,
                                    color = Color.Red
                                )
                            }
                            Text(
                                "Lat: %.4f   Lon: %.4f".format(site.latitude, site.longitude),
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                        Row {
                            // Save edited values if changed and valid
                            IconButton(onClick = {
                                val newLat = latitudeText.toDoubleOrNull()
                                val newLon = longitudeText.toDoubleOrNull()
                                if (newLat == site.latitude && newLon == site.longitude && name == site.name) {
                                    isEditing = false
                                } else if (newLat != null && newLon != null && name.isNotBlank()) {
                                    onEdit(
                                        LaunchSite(
                                            uid = site.uid,
                                            latitude = newLat,
                                            longitude = newLon,
                                            name = name
                                        )
                                    )
                                }
                            },  modifier = Modifier.semantics { contentDescription = "Save changes" }
                            ) {
                                Icon(Icons.Default.Check, contentDescription = "Save", tint = IconGreen)
                            }
                            // Cancel editing and reset values
                            IconButton(onClick = {
                                name = site.name
                                latitudeText = site.latitude.toString()
                                longitudeText = site.longitude.toString()
                                isEditing = false
                            },  modifier = Modifier.semantics { contentDescription = "Cancel edit" }
                            ) {
                                Icon(Icons.Default.Close, contentDescription = "Cancel", tint = IconRed)
                            }
                        }
                    }
                }
            }
        }
    }
    // Show confirmation dialog if user initiates deletion
    if (showConfirmationDialog) {
        ConfirmationDialog(
            title = "Delete Configuration",
            text = "Are you sure you want to delete ${site.name}?",
            confirmText = "Delete",
            dismissText = "Cancel",
            onConfirm = onDelete,
            onDismiss = { showConfirmationDialog = false }
        )
    }
}
