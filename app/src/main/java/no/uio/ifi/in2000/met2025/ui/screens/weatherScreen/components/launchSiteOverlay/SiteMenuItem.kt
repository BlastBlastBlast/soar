package no.uio.ifi.in2000.met2025.ui.screens.weatherScreen.components.launchSiteOverlay

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import no.uio.ifi.in2000.met2025.R
import no.uio.ifi.in2000.met2025.data.local.database.LaunchSite

@Composable
fun SiteMenuItem(
    site: LaunchSite,
    onClick: () -> Unit,
    minWidth: Dp,
    maxWidth: Dp
) {
    ElevatedCard(
        modifier  = Modifier
            .widthIn(min = minWidth, max = maxWidth)
            .animateContentSize(tween(200))
            .semantics {
                role = Role.Button
                contentDescription = buildString {
                    append("Launch site ${site.name}. ")
                    append("Coordinates: %.4f, %.4f".format(site.latitude, site.longitude))
                    if (site.name == "New Marker") append(". Pinned site.")
                }
            }
            .clickable(onClick = onClick),
        shape     = RoundedCornerShape(8.dp),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp, pressedElevation = 4.dp),
        colors    = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
            contentColor   = MaterialTheme.colorScheme.onSurface
        )
    ) {
        Row(
            Modifier
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(Modifier.weight(1f)) {
                Text(site.name, fontWeight = FontWeight.Bold)
                Text(
                    text     = "%.4f, %.4f".format(site.latitude, site.longitude),
                    style    = MaterialTheme.typography.bodySmall
                )
            }
            if (site.name == "New Marker") {
                Icon(
                    painter           = painterResource(id = R.drawable.red_marker),
                    contentDescription= "Pinned",
                    modifier          = Modifier.size(24.dp)
                )
            }
        }
    }
}