package no.uio.ifi.in2000.met2025.ui.screens.weatherScreen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import no.uio.ifi.in2000.met2025.data.local.database.LaunchSite

@Composable
fun SiteHeader(
    site: LaunchSite?,
    coordinates: Pair<Double, Double>,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left column: title + optional elevation
            Column {
                Text(
                    text = site?.name ?: "Location",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                site?.let {
                    Spacer(Modifier.height(2.dp))
                    Text(
                        text = "Elevation: ${it.elevation?.toInt() ?: "--"} m",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Right column: lat/lon
            Column(
                horizontalAlignment = Alignment.End
            ) {
                Text(
                    text = "Lat ${"%.4f".format(site?.latitude ?: coordinates.first)}",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Lon ${"%.4f".format(site?.longitude ?: coordinates.second)}",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}


