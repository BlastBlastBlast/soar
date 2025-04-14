package no.uio.ifi.in2000.met2025.ui.screens.weathercardscreen.components.filter

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun FilterMenuOverlay(
    isFilterActive: Boolean,
    onToggleFilter: () -> Unit,
    hoursToShow: Float,
    onHoursChanged: (Float) -> Unit,
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
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            FilterToggleValid(
                isActive = isFilterActive,
                onClick = onToggleFilter,
                modifier = Modifier.padding(vertical = 4.dp)
            )
            FilterSliderHours(
                hoursToShow = hoursToShow,
                onHoursChanged = onHoursChanged,
                modifier = Modifier.padding(vertical = 4.dp)
            )
        }
    }
}
