package no.uio.ifi.in2000.met2025.ui.screens.weatherScreen.components.launchSiteOverlay

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import no.uio.ifi.in2000.met2025.data.local.database.LaunchSite

@Composable
fun SiteMenuItemList(
    launchSites: List<LaunchSite>,
    onSelect: (LaunchSite) -> Unit,
    minWidth: Dp,
    maxWidth: Dp
) {
    val pinned = launchSites.find { it.name == "New Marker" }
    val others = launchSites.filter { it.name != "New Marker" }

    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding      = PaddingValues(horizontal = 8.dp, vertical = 8.dp),
        modifier = Modifier.semantics {
            contentDescription = "Available launch sites"
        }
    ) {
        pinned?.let { site ->
            item {
                SiteMenuItem(
                    site = site,
                    onClick = { onSelect(site) },
                    minWidth = minWidth,
                    maxWidth = maxWidth
                )
            }
            item {
                HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f))
            }
        }

        itemsIndexed(others) { idx, site ->
            SiteMenuItem(
                site = site,
                onClick = { onSelect(site) },
                minWidth = minWidth,
                maxWidth = maxWidth
            )
            if (idx < others.lastIndex) {
                HorizontalDivider(
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f),
                    modifier = Modifier.padding(vertical = 4.dp)
                )
            }
        }
    }
}
