package no.uio.ifi.in2000.met2025.ui.screens.weatherScreen.components.weatherFilterOverlay

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
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
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
import no.uio.ifi.in2000.met2025.data.models.safetyevaluation.LaunchStatus
import no.uio.ifi.in2000.met2025.ui.theme.Black
import no.uio.ifi.in2000.met2025.ui.theme.WarmOrange

@Composable
fun WeatherFilterOverlay(
    isFilterActive: Boolean,
    onToggleFilter: () -> Unit,
    hoursToShow: Float,
    onHoursChanged: (Float) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
    selectedStatuses: Set<LaunchStatus>,
    onStatusToggled: (LaunchStatus) -> Unit,
    isSunFilterActive: Boolean,
    onToggleSunFilter: () -> Unit
) {
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
        // 2) our orange “sheet” container, positioned by the caller
        AnimatedVisibility(
            visible = true,
            enter   = expandVertically(expandFrom = Alignment.Bottom, animationSpec = tween(300)) + fadeIn(tween(300)),
            exit    = shrinkVertically(shrinkTowards = Alignment.Bottom, animationSpec = tween(300)) + fadeOut(tween(300)),
            modifier = modifier.semantics {
                contentDescription = "Filter menu overlay"
            }
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
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier            = Modifier.padding(vertical = 16.dp)
                ) {
                    // 2a) toggle row
                    FilterToggleValid(
                        isActive = isFilterActive,
                        onClick  = {
                            onToggleFilter()
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

                    SunriseFilter(
                        isSunFilterActive = isSunFilterActive,
                        onToggleSunFilter = onToggleSunFilter,
                        modifier = Modifier
                            .padding(horizontal = 16.dp)
                    )
                }
            }
        }
    }
}

