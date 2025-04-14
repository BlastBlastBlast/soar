package no.uio.ifi.in2000.met2025.ui.screens.weathercardscreen.components.site

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import no.uio.ifi.in2000.met2025.data.local.database.LaunchSite
import no.uio.ifi.in2000.met2025.ui.screens.home.components.LaunchSitesMenu

@Composable
fun LaunchSitesMenuOverlay(
    launchSites: List<LaunchSite>,
    onSiteSelected: (LaunchSite) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = true,
        enter = expandVertically(
            expandFrom = Alignment.Bottom,
            animationSpec = tween(durationMillis = 300)
        ) + fadeIn(animationSpec = tween(durationMillis = 300)),
        exit = shrinkVertically(
            shrinkTowards = Alignment.Bottom,
            animationSpec = tween(durationMillis = 300)
        ) + fadeOut(animationSpec = tween(durationMillis = 300)),
        modifier = modifier.padding(horizontal = 16.dp)
    ) {
        // Reuse the LaunchSitesMenu from your home screen
        LaunchSitesMenu(
            launchSites = launchSites,
            onSiteSelected = { selectedSite ->
                onSiteSelected(selectedSite)
            },
            modifier = Modifier
        )
    }
}