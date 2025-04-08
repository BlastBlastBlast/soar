package no.uio.ifi.in2000.met2025.ui.screens.weathercardscreen.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.draw.shadow

@Composable
fun EditConfigsMenuItem(
    onClick: () -> Unit,
    enabled: Boolean,
    modifier: Modifier = Modifier
) {
    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp.dp
    val minWidth = screenWidth * 0.4f
    val maxWidth = screenWidth * 0.8f

    Row(
        modifier = modifier
            .clickable(enabled = enabled) { onClick() }
            .shadow(elevation = 8.dp, shape = RoundedCornerShape(8.dp))
            .background(
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
                shape = RoundedCornerShape(8.dp)
            )
            .padding(8.dp)
            .widthIn(min = minWidth, max = maxWidth),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "Edit Configs",
            fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.bodyMedium,
            fontSize = 14.sp
        )
    }
}

@Composable
fun ConfigMenuItem(
    config: ConfigProfile,
    onConfigSelected: (ConfigProfile) -> Unit,
    modifier: Modifier = Modifier
) {
    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp.dp
    val minWidth = screenWidth * 0.4f
    val maxWidth = screenWidth * 0.8f

    Row(
        modifier = modifier
            .clickable { onConfigSelected(config) }
            .shadow(elevation = 8.dp, shape = RoundedCornerShape(8.dp))
            .background(
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
                shape = RoundedCornerShape(8.dp)
            )
            .padding(8.dp)
            .widthIn(min = minWidth, max = maxWidth)
            .wrapContentWidth(Alignment.Start),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.wrapContentWidth(Alignment.Start)) {
            Text(
                text = config.name,
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}
