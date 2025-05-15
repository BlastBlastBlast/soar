package no.uio.ifi.in2000.met2025.ui.screens.weatherScreen.components.weatherFilterOverlay

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import no.uio.ifi.in2000.met2025.data.models.safetyevaluation.LaunchStatus
import no.uio.ifi.in2000.met2025.ui.theme.WarmOrange

/**
 * LaunchStatusToggleRow.kt
 *
 * This composable displays a horizontal row of toggleable filter chips for launch status categories.
 * It allows the user to include or exclude forecast results based on their safety classification:
 * SAFE, CAUTION, or UNSAFE.
 *
 * Main functionality:
 * - Each status is represented by a selectable chip
 * - The UI reflects the current filter state via `selectedStatuses`
 * - The parent can react to toggle changes via `onStatusToggled`
 *
 */
@Composable
fun LaunchStatusToggleRow(
    selectedStatuses: Set<LaunchStatus>,
    onStatusToggled: (LaunchStatus) -> Unit,
    modifier: Modifier = Modifier
) {
    val screenWidth = LocalConfiguration.current.screenWidthDp.dp
    val minW = screenWidth * 0.4f
    val maxW = screenWidth * 0.8f
    ElevatedCard(
        modifier = modifier.widthIn(min = minW, max = maxW)
            .semantics {
                contentDescription = "Filter by launch status"
            },
        shape = RoundedCornerShape(8.dp),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f)
        )
    ) {
        Row(
            Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Display a chip for each launch status value (SAFE, CAUTION, UNSAFE)
            LaunchStatus.entries.forEach { status ->
                val isSelected = selectedStatuses.contains(status)
                FilterChip(
                    selected = isSelected,
                    onClick = { onStatusToggled(status) },
                    modifier = Modifier.semantics {
                        role = Role.Checkbox
                        contentDescription =
                            "${status.name.lowercase().replaceFirstChar { it.uppercase() }} filter, " +
                                    if (isSelected) "selected" else "not selected"
                    },
                    label = { Text(status.name.lowercase().replaceFirstChar { it.uppercase() }) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = WarmOrange,
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                )
            }
        }
    }
}
