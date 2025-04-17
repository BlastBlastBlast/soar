package no.uio.ifi.in2000.met2025.ui.screens.launchsite

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.MaterialTheme
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import no.uio.ifi.in2000.met2025.R
import no.uio.ifi.in2000.met2025.data.local.database.LaunchSite
import no.uio.ifi.in2000.met2025.ui.screens.home.HomeScreenViewModel

@Composable
fun LaunchSiteItem(
    site: LaunchSite,
    onDelete: () -> Unit,
    onEdit: (LaunchSite) -> Unit,
    updateStatus: LaunchSiteViewModel.UpdateStatus
) {
    var isEditing by remember { mutableStateOf(false) }
    var name by remember { mutableStateOf(site.name) }
    var latitudeText by remember { mutableStateOf(site.latitude.toString()) }
    var longitudeText by remember { mutableStateOf(site.longitude.toString()) }

    // Check if this is the special "New Marker" item.
    val isSpecialMarker = site.name == "New Marker"

    //TODO: Fix keyboard overlapping cards while editing. Add onDone to keyboard.

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        shape = RoundedCornerShape(8.dp),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        if (!isEditing) {
            Row(
                modifier = Modifier
                    .padding(horizontal = 8.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    if (isSpecialMarker) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Last Marker",
                                style = MaterialTheme.typography.bodyLarge,
                                maxLines = 1
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Image(
                                painter = painterResource(id = R.drawable.red_marker),
                                contentDescription = "Launch Site Icon",
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    } else {
                        Text(
                            text = site.name,
                            style = MaterialTheme.typography.bodyLarge,
                            maxLines = 1
                        )
                    }
                    Text(
                        text = "Lat: ${"%.4f".format(site.latitude)}  Lon: ${"%.4f".format(site.longitude)}",
                        style = MaterialTheme.typography.bodySmall,
                        maxLines = 1
                    )
                }
                Row {
                    IconButton(onClick = { isEditing = true }) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit Launch Site"
                        )
                    }
                    IconButton(onClick = onDelete) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete Launch Site"
                        )
                    }
                }
            }
        } else {
            Column(modifier = Modifier.padding(8.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Name") },
                    modifier = Modifier.fillMaxWidth()
                )
                Row(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = latitudeText,
                        onValueChange = { latitudeText = it },
                        label = { Text("Lat") },
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    OutlinedTextField(
                        value = longitudeText,
                        onValueChange = { longitudeText = it },
                        label = { Text("Lon") },
                        modifier = Modifier.weight(1f)
                    )
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp),
                    horizontalArrangement = Arrangement.End
                ) {
                    IconButton(onClick = {
                        val newLat = latitudeText.toDoubleOrNull()
                        val newLon = longitudeText.toDoubleOrNull()
                        if (newLat != null && newLon != null && name.isNotBlank()) {
                            // Save the edited marker as a new launch site.
                            onEdit(
                                LaunchSite(
                                    uid = site.uid, // or leave 0 so that the database assigns a new UID.
                                    latitude = newLat,
                                    longitude = newLon,
                                    name = name
                                )
                            )
                        }
                    }) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Save"
                        )
                    }
                    IconButton(onClick = {
                        isEditing = false
                        // Reset the fields.
                        name = site.name
                        latitudeText = site.latitude.toString()
                        longitudeText = site.longitude.toString()
                    }) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Cancel"
                        )
                    }
                }
                if (updateStatus is LaunchSiteViewModel.UpdateStatus.Error) {
                    Text(
                        text = updateStatus.message,
                        color = Color.Red
                    )
                }
                if (updateStatus is LaunchSiteViewModel.UpdateStatus.Success) {
                    isEditing = false
                }
            }
        }
    }
}
