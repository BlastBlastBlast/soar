package no.uio.ifi.in2000.met2025.ui.screens.home.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import no.uio.ifi.in2000.met2025.data.local.database.RocketConfig

@Composable
fun RocketConfigCarousel(
    configs: List<RocketConfig>,
    selectedConfig: RocketConfig?,
    onSelectConfig: (RocketConfig) -> Unit,
    modifier: Modifier = Modifier
) {
    // 3a) remember a scroll state
    val listState = rememberLazyListState()

    // 3b) whenever the selection changes, scroll to it
    LaunchedEffect(selectedConfig, configs) {
        selectedConfig?.let { sel ->
            val index = configs.indexOfFirst { it.uid == sel.uid }
            if (index >= 0) {
                listState.animateScrollToItem(index)
            }
        }
    }

    LazyRow(
        state = listState,                                 // ← attach the state
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(horizontal = 16.dp)
    ) {
        items(
            items = configs,
            key   = { it.uid }                               // ← stable key
        ) { cfg ->
            val isSelected = cfg.uid == selectedConfig?.uid
            Card(
                modifier = Modifier
                    .width(160.dp)
                    .clickable { onSelectConfig(cfg) },
                elevation = if (isSelected) 8.dp else 2.dp,
                colors    = CardDefaults.cardColors(
                    containerColor = if (isSelected)
                        MaterialTheme.colorScheme.primaryContainer
                    else
                        MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Box(
                    Modifier
                        .fillMaxSize()
                        .padding(12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text      = cfg.name,
                        style     = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}