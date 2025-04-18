package no.uio.ifi.in2000.met2025.ui.screens.weathercardscreen.components.config

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.fadeOut
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import no.uio.ifi.in2000.met2025.data.local.database.ConfigProfile
import no.uio.ifi.in2000.met2025.ui.theme.Black
import no.uio.ifi.in2000.met2025.ui.theme.WarmOrange

@Composable
fun ConfigMenuOverlay(
    configList: List<ConfigProfile>,
    onConfigSelected: (ConfigProfile) -> Unit,
    onNavigateToEditConfigs: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Black.copy(alpha = 0.3f))
    ) {
        Box(
            modifier = modifier
            ) {
                AnimatedVisibility(
                visible = true,
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
                        .padding(16.dp),
                    color           = WarmOrange,
                    tonalElevation  = 4.dp,
                    shadowElevation = 8.dp,
                    shape           = RoundedCornerShape(12.dp)
                ) {
                    Column {
                        EditConfigsMenuItem(
                            onClick = {
                                onNavigateToEditConfigs()
                                onDismiss()
                            },
                            enabled = true,
                            modifier = Modifier
                                .padding(vertical = 4.dp)
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
                                    ConfigMenuItem(
                                        config            = cfg,
                                        onConfigSelected  = {
                                            onConfigSelected(it)
                                            onDismiss()
                                        },
                                        modifier          = Modifier.weight(1f)
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
}