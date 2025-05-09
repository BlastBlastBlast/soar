package no.uio.ifi.in2000.met2025.ui.screens.launchSiteScreen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
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

    val listState = rememberLazyListState()

    // Filter out temporary markers
    val displaySites = launchSites.filter { it.name != "New Marker" && it.name != "Last Visited" }
    val sortedDisplaySites = displaySites.sortedBy { it.name }

    Box(
        Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 4.dp,
            shadowElevation = 8.dp,
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(Modifier.fillMaxSize()) {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    state = listState
                ) {
                    item {
                        Box(
                            Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 16.dp)
                                .background(
                                    WarmOrange,
                                    shape = RoundedCornerShape(4.dp)
                                )
                                .padding(horizontal = 12.dp, vertical = 8.dp)
                                .semantics { heading() },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "LAUNCH SITES",
                                style = MaterialTheme.typography.titleLarge,
                                color = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier.fillMaxWidth(),
                                textAlign = TextAlign.Center
                            )
                        }
                        Spacer(Modifier.height(8.dp))
                    }
                    itemsIndexed(sortedDisplaySites) { index, site ->
                        LaunchSiteItem(
                            site = site,
                            onDelete = { viewModel.deleteLaunchSite(site) },
                            onEdit = { updatedSite -> viewModel.updateLaunchSite(updatedSite, site.name) },
                            updateStatus = updateStatus,
                            viewModel = viewModel,
                            listState = listState,
                            itemIndex = index
                        )
                    }
                }
            }
        }
    }
    LaunchedEffect(updateStatus) {
        if (updateStatus is LaunchSiteViewModel.UpdateStatus.Success) {
            viewModel.setUpdateStatusToIdle()
        }
    }
}