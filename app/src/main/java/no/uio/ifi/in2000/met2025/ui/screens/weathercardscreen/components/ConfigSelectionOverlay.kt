package no.uio.ifi.in2000.met2025.ui.screens.weathercardscreen.components
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.fadeOut
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import no.uio.ifi.in2000.met2025.data.local.Database.ConfigProfile

@Composable
fun ConfigSelectionOverlay(
    configList: List<ConfigProfile>,
    activeConfig: ConfigProfile,
    onConfigSelected: (ConfigProfile) -> Unit,
    // Overall modifier for positioning (passed from parent)
    modifier: Modifier = Modifier
) {
    var isConfigMenuExpanded by remember { mutableStateOf(false) }

    Box(modifier = modifier) {
        // The FloatingActionButton for config selection.
        FloatingActionButton(
            onClick = { isConfigMenuExpanded = !isConfigMenuExpanded },
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(16.dp)
                .size(90.dp),
            containerColor = MaterialTheme.colorScheme.primary
        ) {
            // Show an abbreviation of the active config's name.
            Text(text = activeConfig.name.take(3))
        }
        // Animated dropdown menu that expands upward.
        AnimatedVisibility(
            visible = isConfigMenuExpanded,
            enter = expandVertically(
                expandFrom = Alignment.Bottom,
                animationSpec = tween(durationMillis = 300)
            ) + fadeIn(animationSpec = tween(durationMillis = 300)),
            exit = shrinkVertically(
                shrinkTowards = Alignment.Bottom,
                animationSpec = tween(durationMillis = 300)
            ) + fadeOut(animationSpec = tween(durationMillis = 300)),
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(start = 16.dp, bottom = 100.dp)
        ) {
            Column {
                configList.forEach { item ->
                    ConfigMenuItem(
                        config = item,
                        onConfigSelected = { selected ->
                            onConfigSelected(selected)
                            isConfigMenuExpanded = false
                        },
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                }
            }
        }
    }
}