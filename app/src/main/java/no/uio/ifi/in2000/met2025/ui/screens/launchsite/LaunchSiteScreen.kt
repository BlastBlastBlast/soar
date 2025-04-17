package no.uio.ifi.in2000.met2025.ui.screens.launchsite

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import no.uio.ifi.in2000.met2025.ui.theme.WarmOrange

@Composable
fun LaunchSiteScreen(
    viewModel: LaunchSiteViewModel = hiltViewModel()
) {
    val launchSites by viewModel.launchSites.collectAsState(initial = emptyList())
    val updateStatus by viewModel.updateStatus.collectAsState()
    var showNewSiteDialog by remember { mutableStateOf(false) }

    val newMarkerSite = launchSites.find { it.name == "New Marker" }
    val otherSites = launchSites
        .filter { it.name != "Last Visited" && it.name != "New Marker" }
        .sortedBy { it.name }
    val displaySites = listOfNotNull(newMarkerSite) + otherSites

    Box(
        Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)  // the system “window” background
    ) {
        Surface(
            modifier        = Modifier
                .fillMaxSize()
                .padding(16.dp),
            color           = MaterialTheme.colorScheme.surface,   // your CONFIG‐style slightly off‑white
            tonalElevation  = 4.dp,                                // blends a touch of primary into surface
            shadowElevation = 8.dp,                                // big enough shadow to see the lift
            shape           = RoundedCornerShape(12.dp)
        ) {
            Column(Modifier.fillMaxSize()) {
                // — HEADER IN ORANGE BAND —
                Box(
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 16.dp)
                        .background(
                            WarmOrange,
                            shape = RoundedCornerShape(4.dp)
                        )
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "LAUNCH SITES",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold),
                        color = MaterialTheme.colorScheme.onPrimary,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                Spacer(Modifier.height(8.dp))

                LazyColumn(
                    modifier            = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentPadding      = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(displaySites) { site ->
                        LaunchSiteItem(
                            site     = site,
                            onDelete = { viewModel.deleteLaunchSite(site) },
                            onEdit   = { updatedSite ->
                                viewModel.updateLaunchSite(updatedSite)
                            },
                            updateStatus = updateStatus
                        )
                    }
                }

                Spacer(Modifier.height(8.dp))

                Button(
                    onClick    = { showNewSiteDialog = true },
                    modifier   = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    colors     = ButtonDefaults.buttonColors(
                        containerColor = WarmOrange,
                        contentColor   = MaterialTheme.colorScheme.onPrimary
                    )
                ) {
                    Text("+")
                }
            }
        }

        if (showNewSiteDialog) {
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
    }
    LaunchedEffect(updateStatus) {
        if (updateStatus is LaunchSiteViewModel.UpdateStatus.Success) {
            // Clear the status after a successful update.
            viewModel.setUpdateStatusToIdle()
        }
    }
}
