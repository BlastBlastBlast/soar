package no.uio.ifi.in2000.met2025.ui.screens.launchsite

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel

@Composable
fun LaunchSiteScreen(viewModel: LaunchSiteViewModel = hiltViewModel()) {
    val launchSites by viewModel.launchSites.collectAsState(initial = emptyList())
    val updateStatus by viewModel.updateStatus.collectAsState()
    var showNewSiteDialog by remember { mutableStateOf(false) }

    // Separate "New Marker" from other launch sites as before.
    val newMarkerSite = launchSites.find { it.name == "New Marker" }
    val otherSites = launchSites.filter { it.name != "Last Visited" && it.name != "New Marker" }
        .sortedBy { it.name }
    val displaySites = listOfNotNull(newMarkerSite) + otherSites

    Column(modifier = Modifier.padding(16.dp)) {
        Text("Saved Launch Sites", style = MaterialTheme.typography.titleLarge)
        Spacer(modifier = Modifier.height(8.dp))
        LazyColumn {
            items(displaySites) { site ->
                LaunchSiteItem(
                    site = site,
                    onDelete = { viewModel.deleteLaunchSite(site) },
                    onEdit = { updatedSite ->
                        // Instead of updating in place, add a new record with the edited values.
                        viewModel.updateLaunchSite(updatedSite)
                    },
                    updateStatus = updateStatus
                )
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = { showNewSiteDialog = true },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("+")
        }
    }

    if (showNewSiteDialog) {
        // This dialog can also be used to add a completely new launch site.
        NewLaunchSiteDialog(
            onDismiss = { showNewSiteDialog = false },
            onConfirm = { name, latStr, lonStr ->
                val lat = latStr.toDoubleOrNull()
                val lon = lonStr.toDoubleOrNull()
                if (lat != null && lon != null && name.isNotBlank()) {
                    viewModel.addLaunchSite(lat, lon, name)
                    showNewSiteDialog = false
                }
            }
        )
    }
    LaunchedEffect(updateStatus) {
        if (updateStatus is LaunchSiteViewModel.UpdateStatus.Success) {
            // Clear the status after a successful update.
            viewModel.setUpdateStatusToIdle()
        }
    }
}
