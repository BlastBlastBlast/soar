package no.uio.ifi.in2000.met2025.ui.screens.home.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import no.uio.ifi.in2000.met2025.R
import no.uio.ifi.in2000.met2025.data.local.database.LaunchSite

@Composable
fun LaunchSitesMenu(
    launchSites: List<LaunchSite>,
    onSiteSelected: (LaunchSite) -> Unit,
    modifier: Modifier = Modifier
) {
    val configuration = LocalConfiguration.current
    val screenWidth   = configuration.screenWidthDp.dp
    val screenHeight  = configuration.screenHeightDp.dp
    val minWidth      = screenWidth * 0.3f
    val maxWidth      = screenWidth * 0.8f
    val maxHeight     = screenHeight * 0.5f

    // build list with "New Marker" first
    val items = buildList<LaunchSite> {
        launchSites.find { it.name == "New Marker" }?.let(::add)
        addAll(launchSites.filter { it.name != "New Marker" })
    }

    Column(
        modifier = modifier
            .padding(8.dp)
            .heightIn(max = maxHeight)
            .verticalScroll(rememberScrollState()), // scroll if too tall
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        items.forEachIndexed { idx, site ->
            AnimatedVisibility(
                visible = true,
                enter   = slideInVertically(
                    initialOffsetY = { it / 2 },
                    animationSpec  = tween(durationMillis = 150, delayMillis = idx * 30)
                ) + fadeIn(animationSpec = tween(150, idx * 30)),
                exit    = shrinkVertically(
                    shrinkTowards = Alignment.Bottom,
                    animationSpec = tween(150)
                ) + fadeOut(tween(150))
            ) {
                ElevatedCard(
                    modifier  = Modifier
                        .widthIn(min = minWidth, max = maxWidth)
                        .animateContentSize(tween(200))
                        .clickable { onSiteSelected(site) },
                    shape     = RoundedCornerShape(8.dp),
                    elevation = CardDefaults.elevatedCardElevation(
                        defaultElevation = 2.dp,
                        pressedElevation = 4.dp
                    ),
                    colors    = CardDefaults.elevatedCardColors(
                        containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
                        contentColor   = MaterialTheme.colorScheme.onSurface
                    )
                ) {
                    Row(
                        Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(Modifier.weight(1f)) {
                            Text(
                                text       = if (site.name == "New Marker") "Last Marker" else site.name,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text     = "%.4f, %.4f".format(site.latitude, site.longitude),
                                fontSize = 7.sp
                            )
                        }
                        if (site.name == "New Marker") {
                            Image(
                                painter            = painterResource(R.drawable.red_marker),
                                contentDescription = "New Marker",
                                modifier           = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }

            // add 8.dp space after every card
            Spacer(Modifier.height(8.dp))
        }
    }
}
