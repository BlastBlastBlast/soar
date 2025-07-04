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

/**
 * SiteMenuItemList
 *
 * Displays a scrollable list of launch sites, separated visually with dividers.
 * The "New Marker" site (if present) is pinned at the top, followed by other sites.
 *
 * Special notes:
 * - Ensures consistent ordering by prioritizing pinned sites.
 * - Applies accessible semantics to describe the list content.
 */
@Composable
fun SiteMenuItemList(
    launchSites: List<LaunchSite>,
    onSelect: (LaunchSite) -> Unit,
    minWidth: Dp,
    maxWidth: Dp
) {
    // Separate pinned site (for example custom user marker) from other sites
    val pinned = launchSites.find { it.name == "New Marker" }
    val others = launchSites.filter { it.name != "New Marker" }

    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding      = PaddingValues(horizontal = 8.dp, vertical = 8.dp),
        modifier = Modifier.semantics {
            contentDescription = "Available launch sites"
        }
    ) {
        // Display pinned site at the top if it exists
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
