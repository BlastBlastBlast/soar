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
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import no.uio.ifi.in2000.met2025.R
import no.uio.ifi.in2000.met2025.data.local.database.LaunchSite

@Composable
fun LaunchSiteItem(
    site: LaunchSite,
    onDelete: () -> Unit,
    onEdit: (LaunchSite) -> Unit
) {
    var isEditing      by remember { mutableStateOf(false) }
    var name           by remember { mutableStateOf(site.name) }
    var latitudeText   by remember { mutableStateOf(site.latitude.toString()) }
    var longitudeText  by remember { mutableStateOf(site.longitude.toString()) }
    val isSpecialMarker = site.name == "New Marker"

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        shape     = RoundedCornerShape(8.dp),
        elevation = CardDefaults.cardElevation(2.dp),
        colors    = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor   = MaterialTheme.colorScheme.onPrimary
        )
    ) {
        if (!isEditing) {
            Row(
                modifier            = Modifier.padding(horizontal = 8.dp, vertical = 12.dp),
                verticalAlignment   = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // TEXT COLUMN
                Column(modifier = Modifier.weight(1f)) {
                    if (isSpecialMarker) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                "Last Marker",
                                style = MaterialTheme.typography.bodyLarge
                            )
                            Spacer(Modifier.width(4.dp))
                            Image(
                                painter            = painterResource(R.drawable.red_marker),
                                contentDescription = "Launch Site Icon",
                                modifier           = Modifier.size(24.dp)
                            )
                        }
                    } else {
                        Text(
                            text  = site.name,
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                    Text(
                        text  = "Lat: %.4f   Lon: %.4f".format(site.latitude, site.longitude),
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                // ACTION ICONS
                Row {
                    IconButton(onClick = { isEditing = true }) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit Launch Site",
                            tint         = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                    IconButton(onClick = onDelete) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete Launch Site",
                            tint         = MaterialTheme.colorScheme.onPrimary
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
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor   = MaterialTheme.colorScheme.onPrimary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.5f)
                    )
                )
                Row(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = latitudeText,
                        onValueChange = { latitudeText = it },
                        label = { Text("Lat") },
                        modifier = Modifier.weight(1f),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor   = MaterialTheme.colorScheme.onPrimary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.5f)
                        )
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    OutlinedTextField(
                        value = longitudeText,
                        onValueChange = { longitudeText = it },
                        label = { Text("Lon") },
                        modifier = Modifier.weight(1f),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor   = MaterialTheme.colorScheme.onPrimary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.5f)
                        )
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
                                    uid = 0, // or leave 0 so that the database assigns a new UID.
                                    latitude = newLat,
                                    longitude = newLon,
                                    name = name
                                )
                            )
                            isEditing = false
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
            }
        }
    }
}
