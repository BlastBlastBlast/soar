package no.uio.ifi.in2000.met2025.ui.screens.weathercardscreen.components.site

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.Divider
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import no.uio.ifi.in2000.met2025.data.local.database.LaunchSite

@Composable
fun SiteMenuItemList(
    launchSites: List<LaunchSite>,
    onSelect: (LaunchSite) -> Unit
) {
    val pinned = launchSites.find { it.name == "New Marker" }
    val others = launchSites.filter { it.name != "New Marker" }

    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding      = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
    ) {
        pinned?.let { site ->
            item {
                SiteMenuItem(site = site, onClick = { onSelect(site) })
            }
            item {
                Divider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f))
            }
        }

        itemsIndexed(others) { idx, site ->
            SiteMenuItem(site = site, onClick = { onSelect(site) })
            if (idx < others.lastIndex) {
                HorizontalDivider(
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f),
                    modifier = Modifier.padding(vertical = 4.dp)
                )
            }
        }
    }
}
