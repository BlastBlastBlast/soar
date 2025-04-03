package no.uio.ifi.in2000.met2025.ui.screens.home.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import no.uio.ifi.in2000.met2025.R
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
            Row(
                modifier = Modifier
                    .clickable { onSiteSelected(site) }
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (site.name == "New Marker") {
                    Text(text = "Last Marker")
                    Image(
                        painter = painterResource(id = R.drawable.red_marker),
                        contentDescription = "New Marker",
                        modifier = Modifier.padding(end = 4.dp).size(20.dp)
                    )
                } else {
                    Text(text = site.name)
                }
            }
        }
    }
}