package no.uio.ifi.in2000.met2025.ui.screens.launchsite

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import no.uio.ifi.in2000.met2025.R
import no.uio.ifi.in2000.met2025.data.local.database.LaunchSite
import no.uio.ifi.in2000.met2025.ui.AppOutlinedTextField
import no.uio.ifi.in2000.met2025.ui.theme.IconGreen
import no.uio.ifi.in2000.met2025.ui.theme.IconRed
import no.uio.ifi.in2000.met2025.ui.theme.WarmOrange

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
    val orangeStripHeight = 16.dp

    Card(
        modifier  = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .animateContentSize(),
        shape     = RoundedCornerShape(8.dp),
        elevation = CardDefaults.cardElevation(2.dp),
    ) {
        Column(Modifier.clip(RoundedCornerShape(8.dp))) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(orangeStripHeight)
                    .background(
                        color = WarmOrange,
                        shape = RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp)
                    )
            )
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        color = MaterialTheme.colorScheme.surface,
                        shape = RoundedCornerShape(bottomStart = 8.dp, bottomEnd = 8.dp)
                    )
                    .padding(horizontal = 8.dp, vertical = 12.dp)
            ) {
                if (!isEditing) {
                    Row(
                        modifier              = Modifier.fillMaxWidth(),
                        verticalAlignment     = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            if (isSpecialMarker) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text("Last Marker", style = MaterialTheme.typography.bodyLarge)
                                    Spacer(Modifier.width(4.dp))
                                    Image(
                                        painter            = painterResource(R.drawable.red_marker),
                                        contentDescription = "Launch Site Icon",
                                        modifier           = Modifier.size(24.dp)
                                    )
                                }
                            } else {
                                Text(site.name, style = MaterialTheme.typography.bodyLarge)
                            }
                            Text(
                                "Lat: %.4f   Lon: %.4f".format(site.latitude, site.longitude),
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                        Row {
                            IconButton(onClick = { isEditing = true }) {
                                Icon(Icons.Default.Edit, contentDescription = "Edit")
                            }
                            IconButton(onClick = onDelete) {
                                Icon(Icons.Default.Delete, contentDescription = "Delete")
                            }
                        }
                    }
                } else {
                    // EDIT MODE
                    Column {
                        AppOutlinedTextField(
                            value = name,
                            onValueChange = { name = it },
                            label = { Text("Name") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(Modifier.height(8.dp))
                        Row(modifier = Modifier.fillMaxWidth()) {
                            AppOutlinedTextField(
                                value = latitudeText,
                                onValueChange = { latitudeText = it },
                                label = { Text("Lat") },
                                modifier = Modifier.weight(1f),
                            )
                            Spacer(Modifier.width(8.dp))
                            AppOutlinedTextField(
                                value = longitudeText,
                                onValueChange = { longitudeText = it },
                                label = { Text("Lon") },
                                modifier = Modifier.weight(1f),
                            )
                        }
                        Spacer(Modifier.height(8.dp))
                        Row(
                            modifier            = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            IconButton(onClick = {
                                val newLat = latitudeText.toDoubleOrNull()
                                val newLon = longitudeText.toDoubleOrNull()
                                if (newLat != null && newLon != null && name.isNotBlank()) {
                                    onEdit(LaunchSite(0, newLat, newLon, name))
                                    isEditing = false
                                }
                            }) {
                                Icon(Icons.Default.Check, contentDescription = "Save", tint = IconGreen)
                            }
                            IconButton(onClick = {
                                isEditing = false
                                name = site.name
                                latitudeText = site.latitude.toString()
                                longitudeText = site.longitude.toString()
                            }) {
                                Icon(Icons.Default.Close, contentDescription = "Cancel", tint = IconRed)
                            }
                        }
                    }
                }
            }
        }
    }
}
