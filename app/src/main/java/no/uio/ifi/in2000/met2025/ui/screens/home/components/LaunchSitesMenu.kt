package no.uio.ifi.in2000.met2025.ui.screens.home.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import no.uio.ifi.in2000.met2025.data.local.Database.LaunchSite

@Composable
fun LaunchSitesMenu(
    launchSites: List<LaunchSite>,
    onSiteSelected: (LaunchSite) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .background(Color.White.copy(alpha = 0.9f), shape = RoundedCornerShape(8.dp))
            .padding(8.dp)
    ) {
        launchSites.forEach { site ->
            Text(
                text = site.name,
                modifier = Modifier
                    .clickable { onSiteSelected(site) }
                    .padding(8.dp)
            )
        }
    }
}
