package no.uio.ifi.in2000.met2025.ui.screens.mapScreen.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import no.uio.ifi.in2000.met2025.data.local.database.RocketConfig
import no.uio.ifi.in2000.met2025.ui.theme.WarmOrange

@Composable
fun RocketConfigCarousel(
    rocketConfigs: List<RocketConfig>,
    selectedConfig: RocketConfig?,
    onSelectConfig: (RocketConfig) -> Unit,
    modifier: Modifier = Modifier
) {
    val listState = rememberLazyListState()

    LaunchedEffect(selectedConfig, rocketConfigs) {
        selectedConfig?.let { sel ->
            val index = rocketConfigs.indexOfFirst { it.id == sel.id }
            if (index >= 0) {
                listState.animateScrollToItem(index)
            }
        }
    }
    LazyRow(
        state = listState,
        modifier = modifier
            .fillMaxWidth()
            .semantics(mergeDescendants = false) {
                contentDescription = "Carousel of rocket configurations"
            },
    ) {
        items(
            items = rocketConfigs,
            key   = { it.id }
        ) { cfg ->
            val isSelected = cfg.id == selectedConfig?.id
            Card(
                modifier  = Modifier
                    .size(width = 180.dp, height = 56.dp)
                    .padding(8.dp)
                    .clickable { onSelectConfig(cfg) }
                    .semantics {
                        role = Role.Button
                        stateDescription = if (isSelected) "Selected" else "Not selected"
                        contentDescription = "Select rocket profile ${cfg.name}"
                    },
                elevation = CardDefaults.cardElevation(
                    defaultElevation = if (isSelected) 8.dp else 2.dp
                ),
                colors    = CardDefaults.cardColors(
                    containerColor = if (isSelected)
                        WarmOrange
                    else
                        Color.White

                )
            ) {
                Box(
                    Modifier.fillMaxSize(),
                       contentAlignment = Alignment.Center
                ) {
                    Text(text = cfg.name,
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center,
                        color = Color.Black
                    )
                }
            }
        }
    }
}
