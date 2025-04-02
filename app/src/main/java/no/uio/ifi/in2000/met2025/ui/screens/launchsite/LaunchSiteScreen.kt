package no.uio.ifi.in2000.met2025.ui.screens.launchsite

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel

@Composable
fun LaunchSiteScreen(viewModel: LaunchSiteViewModel = hiltViewModel()) {
    val launchSites by viewModel.launchSites.collectAsState(initial = emptyList())

    Column(modifier = Modifier.padding(16.dp)) {
        Text("Saved Launch Sites", style = MaterialTheme.typography.titleLarge)
        Spacer(modifier = Modifier.height(8.dp))
        LazyColumn {
            items(launchSites.filter { it.name != "Last Visited" }) { site ->
                LaunchSiteItem(
                    site = site,
                    onDelete = { viewModel.deleteLaunchSite(site) },
                    onUpdate = { updatedSite -> viewModel.updateLaunchSite(updatedSite) }
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
        // Input fields to add a new permanent launch site manually.
        var newSiteName by remember { mutableStateOf("") }
        var newSiteLat by remember { mutableStateOf("") }
        var newSiteLon by remember { mutableStateOf("") }
        OutlinedTextField(
            value = newSiteName,
            onValueChange = { newSiteName = it },
            label = { Text("New Site Name") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = newSiteLat,
            onValueChange = { newSiteLat = it },
            label = { Text("New Site Latitude") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = newSiteLon,
            onValueChange = { newSiteLon = it },
            label = { Text("New Site Longitude") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = {
                val lat = newSiteLat.toDoubleOrNull()
                val lon = newSiteLon.toDoubleOrNull()
                if (lat != null && lon != null && newSiteName.isNotBlank()) {
                    viewModel.addLaunchSite(lat, lon, newSiteName)
                    newSiteName = ""
                    newSiteLat = ""
                    newSiteLon = ""
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Legg til LaunchSite")
        }
    }
}
