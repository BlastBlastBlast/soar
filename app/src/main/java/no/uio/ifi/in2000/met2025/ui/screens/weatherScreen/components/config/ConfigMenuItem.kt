package no.uio.ifi.in2000.met2025.ui.screens.weatherScreen.components.config

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import no.uio.ifi.in2000.met2025.data.local.database.ConfigProfile
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics

@Composable
fun EditConfigsMenuItem(
    onClick: () -> Unit,
    enabled: Boolean,
    modifier: Modifier = Modifier
) {
    val screenWidth = LocalConfiguration.current.screenWidthDp.dp
    val minWidth    = screenWidth * 0.4f
    val maxWidth    = screenWidth * 0.8f

    ElevatedCard(
        modifier  = modifier
            .widthIn(min = minWidth, max = maxWidth)
            .animateContentSize(tween(200))
            .clickable(enabled = enabled, onClick = onClick)
            .semantics {
                role = Role.Button
                contentDescription =
                    if (enabled) "Edit configurations"
                    else       "Edit configurations (disabled)"
            },
        shape     = RoundedCornerShape(8.dp),
        elevation = CardDefaults.elevatedCardElevation(
            defaultElevation = 2.dp,
            pressedElevation = 4.dp
        ),
        colors    = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
            contentColor   = MaterialTheme.colorScheme.onSurface
        )
    ) {
        Row(
            Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp, horizontal = 16.dp)
                .semantics { heading() },
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "Edit Configs",
                fontSize   = 14.sp,
                style      = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                modifier   = Modifier.weight(1f)
            )
            Icon(
                imageVector   = Icons.Default.ArrowForward,
                contentDescription = "Go to edit screen",
                tint          = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
fun ConfigMenuItem(
    config: ConfigProfile,
    onConfigSelected: (ConfigProfile) -> Unit,
    modifier: Modifier = Modifier
) {
    val screenWidth = LocalConfiguration.current.screenWidthDp.dp
    val minWidth = screenWidth * 0.4f
    val maxWidth = screenWidth * 0.8f

    ElevatedCard(
        modifier  = modifier
            .widthIn(min = minWidth, max = maxWidth)
            .animateContentSize(tween(200))
            .clickable { onConfigSelected(config) }
            .semantics {
                role = Role.Button
                contentDescription = "Select configuration ${config.name}"
            },
        shape     = RoundedCornerShape(8.dp),
        elevation = CardDefaults.elevatedCardElevation(
            defaultElevation = 2.dp,
            pressedElevation = 4.dp
        ),
        colors    = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
            contentColor   = MaterialTheme.colorScheme.onSurface
        )
    ) {
        Box(
            Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
                .semantics { heading() },
            contentAlignment = Alignment.Center
        ) {
            Text(
                config.name,
                fontSize   = 14.sp,
                style      = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

