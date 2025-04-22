package no.uio.ifi.in2000.met2025.ui.screens.weathercardscreen.components.filter

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import no.uio.ifi.in2000.met2025.data.models.safetyevaluation.LaunchStatus
import no.uio.ifi.in2000.met2025.ui.theme.Black
import no.uio.ifi.in2000.met2025.ui.theme.WarmOrange

@Composable
fun FilterMenuOverlay(
    isFilterActive: Boolean,
    onToggleFilter: () -> Unit,
    hoursToShow: Float,
    onHoursChanged: (Float) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    selectedStatuses: Set<LaunchStatus>,
    onStatusToggled: (LaunchStatus) -> Unit,
) {
    // 1) full‑screen dimmed backdrop
    Box(
        Modifier
            .fillMaxSize()
            .background(Black.copy(alpha = 0.3f))
    ) {
        // 2) our orange “sheet” container, positioned by the caller
        AnimatedVisibility(
            visible = true,
            enter   = expandVertically(expandFrom = Alignment.Bottom, animationSpec = tween(300)) + fadeIn(tween(300)),
            exit    = shrinkVertically(shrinkTowards = Alignment.Bottom, animationSpec = tween(300)) + fadeOut(tween(300)),
            modifier = modifier
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
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier            = Modifier.padding(vertical = 16.dp)
                ) {
                    // 2a) toggle row
                    FilterToggleValid(
                        isActive = isFilterActive,
                        onClick  = {
                            onToggleFilter()
                            onDismiss()
                        },
                        modifier = Modifier
                            .padding(horizontal = 16.dp)
                    )

                    // 2b) slider row
                    FilterSliderHours(
                        hoursToShow    = hoursToShow,
                        onHoursChanged = onHoursChanged,
                        modifier       = Modifier
                            .padding(horizontal = 16.dp)
                    )

                    //launch status row
                    LaunchStatusToggleRow(
                        selectedStatuses = selectedStatuses,
                        onStatusToggled = onStatusToggled,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                }
            }
        }
    }
}
