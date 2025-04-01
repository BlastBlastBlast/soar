package no.uio.ifi.in2000.met2025.ui.screens.launchsite

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import no.uio.ifi.in2000.met2025.data.local.Database.LaunchSite

@Composable
fun LaunchSiteItem(
    site: LaunchSite,
    onDelete: () -> Unit,
    onUpdate: (LaunchSite) -> Unit
) {
    var isEditing by remember { mutableStateOf(false) }
    var name by remember { mutableStateOf(site.name) }
    var latitudeText by remember { mutableStateOf(site.latitude.toString()) }
    var longitudeText by remember { mutableStateOf(site.longitude.toString()) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        shape = RoundedCornerShape(8.dp),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            if (isEditing) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Site Name") },
                    modifier = Modifier.fillMaxWidth()
                )
            } else {
                Text(
                    text = name,
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold
                    )
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            if (isEditing) {
                OutlinedTextField(
                    value = latitudeText,
                    onValueChange = { latitudeText = it },
                    label = { Text("Latitude") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(4.dp))
                OutlinedTextField(
                    value = longitudeText,
                    onValueChange = { longitudeText = it },
                    label = { Text("Longitude") },
                    modifier = Modifier.fillMaxWidth()
                )
            } else {
                Text(
                    text = "Lat: ${"%.4f".format(site.latitude)}",
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = "Lon: ${"%.4f".format(site.longitude)}",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                if (isEditing) {
                    Button(onClick = {
                        val newLat = latitudeText.toDoubleOrNull()
                        val newLon = longitudeText.toDoubleOrNull()
                        if (newLat != null && newLon != null && name.isNotBlank()) {
                            onUpdate(LaunchSite(uid = site.uid, latitude = newLat, longitude = newLon, name = name))
                            isEditing = false
                        }
                    }) {
                        Text("Save")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(onClick = {
                        name = site.name
                        latitudeText = site.latitude.toString()
                        longitudeText = site.longitude.toString()
                        isEditing = false
                    }) {
                        Text("Cancel")
                    }
                } else {
                    Button(onClick = { isEditing = true }) {
                        Text("Edit")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    IconButton(onClick = onDelete) {
                        Icon(imageVector = Icons.Filled.Delete, contentDescription = "Delete")
                    }
                }
            }
        }
    }
}
