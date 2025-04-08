package no.uio.ifi.in2000.met2025.ui.screens.home.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.RoundedCornerShape
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
    // Get screen width from configuration.
    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp.dp
    // Set a minimum width (e.g. 40% of the screen) and a maximum width (e.g. 80%).
    val minWidth = screenWidth * 0.4f
    val maxWidth = screenWidth * 0.8f

    // Separate the pinned marker ("New Marker") from the rest.
    val pinnedMarker = launchSites.find { it.name == "New Marker" }
    val otherSites = launchSites.filter { it.name != "New Marker" }

    Column(modifier = modifier.padding(8.dp)) {
        // Pinned marker element at the top.
        pinnedMarker?.let { site ->
            AnimatedVisibility(
                visible = true,
                enter = slideInVertically(
                    initialOffsetY = { it / 2 },
                    animationSpec = tween(durationMillis = 150)
                ) + fadeIn(animationSpec = tween(durationMillis = 150))
            ) {
                Row(
                    modifier = Modifier
                        .clickable { onSiteSelected(site) }
                        .animateContentSize()
                        .shadow(elevation = 8.dp, shape = RoundedCornerShape(8.dp))
                        // Use the theme's surface color for background.
                        .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.9f), shape = RoundedCornerShape(8.dp))
                        .padding(4.dp)
                        .widthIn(min = minWidth, max = maxWidth)
                        .wrapContentWidth(Alignment.Start),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Column for text (title and coordinates)
                    Column(modifier = Modifier.wrapContentWidth(Alignment.Start)) {
                        Text(
                            text = "Last Marker",
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${"%.4f".format(site.latitude)}, ${"%.4f".format(site.longitude)}",
                            fontSize = 7.sp,
                            textAlign = TextAlign.Start
                        )
                    }
                    Image(
                        painter = painterResource(id = R.drawable.red_marker),
                        contentDescription = "New Marker",
                        modifier = Modifier
                            .padding(start = 4.dp)
                            .size(20.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
        }

        // Other (unpinned) site elements.
        otherSites.forEachIndexed { index, site ->
            AnimatedVisibility(
                visible = true,
                enter = slideInVertically(
                    initialOffsetY = { it / 2 },
                    animationSpec = tween(durationMillis = 150, delayMillis = index * 50)
                ) + fadeIn(animationSpec = tween(durationMillis = 150, delayMillis = index * 50))
            ) {
                Column {
                    Row(
                        modifier = Modifier
                            .clickable { onSiteSelected(site) }
                            .animateContentSize()
                            .shadow(elevation = 8.dp, shape = RoundedCornerShape(8.dp))
                            // Use the theme's surface color so it adapts to dark/light mode.
                            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.9f), shape = RoundedCornerShape(8.dp))
                            .padding(4.dp)
                            .widthIn(min = minWidth, max = maxWidth)
                            .wrapContentWidth(Alignment.Start),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.wrapContentWidth(Alignment.Start)) {
                            Text(
                                text = site.name,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "${"%.4f".format(site.latitude)}, ${"%.4f".format(site.longitude)}",
                                fontSize = 7.sp,
                                textAlign = TextAlign.Start
                            )
                        }
                    }
                    if (index < otherSites.lastIndex) {
                        HorizontalDivider(
                            modifier = Modifier.padding(horizontal = 8.dp),
                            thickness = 1.dp,
                            color = Color.LightGray
                        )
                    }
                }
            }
        }
    }
}