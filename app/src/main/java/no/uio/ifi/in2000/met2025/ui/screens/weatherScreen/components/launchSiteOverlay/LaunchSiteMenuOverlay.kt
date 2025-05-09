package no.uio.ifi.in2000.met2025.ui.screens.weatherScreen.components.launchSiteOverlay

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import no.uio.ifi.in2000.met2025.data.local.database.LaunchSite
import no.uio.ifi.in2000.met2025.ui.theme.Black
import no.uio.ifi.in2000.met2025.ui.theme.WarmOrange

@Composable
fun LaunchSitesMenuOverlay(
    launchSites: List<LaunchSite>,
    onSiteSelected: (LaunchSite) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val configuration   = LocalConfiguration.current
    val screenWidth     = configuration.screenWidthDp.dp
    val screenHeight    = configuration.screenHeightDp.dp
    val minWidth        = screenWidth * 0.3f
    val maxWidth        = screenWidth * 0.55f
    val maxSurfaceWidth = screenWidth * 0.6f
    val maxHeight       = screenHeight * 0.5f
    val bottomBarHeight = 56.dp

    // 1) full‑screen dimmed backdrop
    Box(
        Modifier
            .fillMaxSize()
            .background(Black.copy(alpha = 0.3f))
            .padding(bottom = bottomBarHeight)
            .semantics {
                role = Role.Button
                contentDescription = "Dismiss launch sites menu by clicking outside"
            }
            .clickable(
                onClick = onDismiss,
                indication = null,
                interactionSource = remember { MutableInteractionSource() }
            )
    ) {
        // 2) orange sheet, popped up at bottom
        AnimatedVisibility(
            visible = true,
            enter   = expandVertically(expandFrom = Alignment.Bottom, animationSpec = tween(300)) + fadeIn(tween(300)),
            exit    = shrinkVertically(shrinkTowards = Alignment.Bottom, animationSpec = tween(300)) + fadeOut(tween(300)),
            modifier = modifier.semantics {
                contentDescription = "Launch sites list"
            }        ) {
            Surface(
                modifier        = Modifier
                    .widthIn(max = maxSurfaceWidth, min = minWidth)
                    .padding(16.dp)
                    .clickable(enabled = false) {},
                color           = WarmOrange,
                tonalElevation  = 4.dp,
                shadowElevation = 8.dp,
                shape           = RoundedCornerShape(12.dp)
            ) {
                Column(
                    modifier = Modifier
                        .heightIn(max = maxHeight)
                        .padding(vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    SiteMenuItemList(
                        launchSites   = launchSites,
                        onSelect      = { site ->
                            onSiteSelected(site)
                            onDismiss()
                        },
                        minWidth      = minWidth,
                        maxWidth      = maxWidth
                    )
                }
            }
        }
    }
}
