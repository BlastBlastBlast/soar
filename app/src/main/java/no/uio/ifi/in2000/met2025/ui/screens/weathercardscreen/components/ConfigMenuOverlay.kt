package no.uio.ifi.in2000.met2025.ui.screens.weathercardscreen.components
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.fadeOut
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import no.uio.ifi.in2000.met2025.data.local.database.ConfigProfile

@Composable
fun ConfigMenuOverlay(
    configList: List<ConfigProfile>,
    onConfigSelected: (ConfigProfile) -> Unit,
    onNavigateToEditConfigs: () -> Unit,
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
        modifier = modifier.padding(start = 16.dp)
    ) {
        Column {
            EditConfigsMenuItem(
                onClick = {
                    onNavigateToEditConfigs()
                    onDismiss() // Close the overlay after navigation
                },
                enabled = true,
                modifier = Modifier.padding(vertical = 4.dp)
            )
            configList.forEach { item ->
                ConfigMenuItem(
                    config = item,
                    onConfigSelected = { selected ->
                        onConfigSelected(selected)
                        onDismiss() // Close the overlay after selecting a config
                    },
                    modifier = Modifier.padding(vertical = 4.dp)
                )
            }
        }
    }
}
