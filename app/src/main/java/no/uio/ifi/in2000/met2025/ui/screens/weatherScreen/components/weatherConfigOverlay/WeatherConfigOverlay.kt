package no.uio.ifi.in2000.met2025.ui.screens.weatherScreen.components.weatherConfigOverlay

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.fadeOut
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import no.uio.ifi.in2000.met2025.data.local.database.WeatherConfig
import no.uio.ifi.in2000.met2025.ui.theme.Black
import no.uio.ifi.in2000.met2025.ui.theme.WarmOrange

/**
 * WeatherConfigOverlay
 *
 * Displays a full-screen overlay with a bottom-aligned weather configuration menu.
 * Allows users to select, edit, or dismiss weather configuration profiles.
 *
 * Special notes:
 * - The overlay dims the background and captures clicks to dismiss itself.
 * - Menu content is animated in/out with expand/fade effects.
 */
@Composable
fun WeatherConfigOverlay(
    configList: List<WeatherConfig>,
    onConfigSelected: (WeatherConfig) -> Unit,
    onNavigateToEditConfigs: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val bottomBarHeight = 56.dp

    Box(
        modifier = Modifier
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
            AnimatedVisibility(
            visible = true,
            modifier = modifier.semantics {
                contentDescription = "Weather config selection menu"
            },
            enter = expandVertically(
                expandFrom = Alignment.Bottom,
                animationSpec = tween(300)
            ) + fadeIn(tween(300)),
            exit = shrinkVertically(
                shrinkTowards = Alignment.Bottom,
                animationSpec = tween(300)
            ) + fadeOut(tween(300))
        ) {
            Surface(
                modifier        = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .clickable(enabled = false) {},
                color           = WarmOrange,
                tonalElevation  = 4.dp,
                shadowElevation = 8.dp,
                shape           = RoundedCornerShape(12.dp)
            ) {
                Column {
                    EditWeatherConfig(
                        onClick = {
                            onNavigateToEditConfigs()
                            onDismiss()
                        },
                        enabled = true,
                        modifier = Modifier
                            .padding(vertical = 8.dp)
                            .align(Alignment.CenterHorizontally)
                    )
                    HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f))
                    Spacer(Modifier.height(4.dp))

                    // two items per row
                    configList.chunked(2).forEach { pair ->
                        Row(
                            Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            for (cfg in pair) {
                                WeatherConfigItem(
                                    weatherConfig = cfg,
                                    onConfigSelected = {
                                        onConfigSelected(it)
                                        onDismiss()
                                    },
                                    modifier = Modifier
                                        .weight(1f)
                                        .padding(vertical = 8.dp)
                                )
                            }
                            if (pair.size == 1) {
                                Spacer(Modifier.weight(1f))
                            }
                        }
                        Spacer(Modifier.height(4.dp))
                    }
                }
            }
        }
    }
}