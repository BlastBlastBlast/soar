// SegmentedBottomBar.kt
package no.uio.ifi.in2000.met2025.ui.screens.weathercardscreen.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun SegmentedBottomBar(
    onConfigClick: () -> Unit,
    onFilterClick: () -> Unit,
    onLaunchClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp)
            .background(Color.Black),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .clickable { onConfigClick() },
            contentAlignment = Alignment.Center
        ) {
            Text(text = "Config", color = Color.White, style = MaterialTheme.typography.bodyLarge)
        }
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .clickable { onFilterClick() },
            contentAlignment = Alignment.Center
        ) {
            Text(text = "Filter", color = Color.White, style = MaterialTheme.typography.bodyLarge)
        }
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .clickable { onLaunchClick() },
            contentAlignment = Alignment.Center
        ) {
            Text(text = "Launch", color = Color.White, style = MaterialTheme.typography.bodyLarge)
        }
    }
}
