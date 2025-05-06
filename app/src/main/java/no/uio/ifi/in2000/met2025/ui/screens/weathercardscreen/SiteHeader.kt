package no.uio.ifi.in2000.met2025.ui.screens.weathercardscreen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import no.uio.ifi.in2000.met2025.data.local.database.LaunchSite
import no.uio.ifi.in2000.met2025.ui.theme.WarmOrange

@Composable
fun SiteHeader(site: LaunchSite, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .background(WarmOrange)
            .padding(horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = site.name,
            style = MaterialTheme.typography.titleLarge
        )
        Text(
            text = "Lat ${"%.4f".format(site.latitude)}, Lon ${"%.4f".format(site.longitude)}",
            style = MaterialTheme.typography.bodyMedium
        )
        Text(
            text = "Elevation: ${site.elevation.toInt()} m",
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

