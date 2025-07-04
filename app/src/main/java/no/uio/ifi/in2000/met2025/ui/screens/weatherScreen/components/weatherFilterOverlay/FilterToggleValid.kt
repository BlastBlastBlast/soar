package no.uio.ifi.in2000.met2025.ui.screens.weatherScreen.components.weatherFilterOverlay

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

/**
 * FilterToggleValid
 *
 * A card-style toggle switch that filters forecast hours based on validity.
 *
 * Parameters:
 * - isActive: Whether the filter is active or not. `true` shows all hours, `false` filters to valid only.
 * - onClick: Callback invoked when the card is clicked to toggle the filter.
 *
 * Behavior:
 * - Card acts like a toggle switch (with accessibility semantics).
 * - The label and content description change depending on `isActive`.
 * - Width adapts responsively to screen size.
 */
@Composable
fun FilterToggleValid(
    isActive: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val screenWidth = LocalConfiguration.current.screenWidthDp.dp
    val minW = screenWidth * 0.4f
    val maxW = screenWidth * 0.8f

    ElevatedCard(
        modifier = modifier
            .widthIn(min = minW, max = maxW)
            .clickable(onClick = onClick)
            .semantics {
                role = Role.Switch
                contentDescription =
                    if (isActive) "Show all forecast hours (filter off)"
                    else           "Show only valid hours (filter on)"
            },
        shape     = RoundedCornerShape(8.dp),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp, pressedElevation = 4.dp),
        colors    = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
            contentColor   = MaterialTheme.colorScheme.onSurface
        )
    ) {
        Row(
            Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Display the current state text
            Text(
                text       = if (isActive) "Show all" else "Show valid",
                fontWeight = FontWeight.Bold,
                style      = MaterialTheme.typography.bodyMedium
            )
        }
    }
}
