package no.uio.ifi.in2000.met2025.ui.configprofiles

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import no.uio.ifi.in2000.met2025.data.local.database.ConfigProfile
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import no.uio.ifi.in2000.met2025.ui.theme.WarmOrange
import androidx.compose.ui.Alignment
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import no.uio.ifi.in2000.met2025.ui.screens.settings.SettingsViewModel


@Composable
fun ConfigListScreen(
    viewModel: SettingsViewModel = hiltViewModel(),
    onEditConfig: (ConfigProfile) -> Unit,
    onAddConfig: () -> Unit,
    onSelectConfig: (ConfigProfile) -> Unit
) {
    val configList by viewModel.weatherConfigs.collectAsState(initial = emptyList())

    Box(
        Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Surface(
            modifier        = Modifier
                .fillMaxSize()
                .padding(16.dp),
            color           = MaterialTheme.colorScheme.surface,
            tonalElevation  = 4.dp,    // subtle tint under items
            shadowElevation = 8.dp,
            shape           = RoundedCornerShape(12.dp)
        ) {
            Column(Modifier.fillMaxSize()) {
                // — HEADER IN ORANGE BAND —
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .background(WarmOrange, RoundedCornerShape(4.dp))
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "SAVED CONFIGURATIONS",
                        style     = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold),
                        color     = MaterialTheme.colorScheme.onPrimary,
                        textAlign = TextAlign.Center,
                        modifier  = Modifier.fillMaxWidth()
                    )
                }

                Spacer(Modifier.height(8.dp))

                LazyColumn(
                    modifier            = Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    contentPadding      = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(configList) { config ->
                        ConfigListItem(
                            config    = config,
                            onClick   = { onSelectConfig(config) },
                            onEdit    = { onEditConfig(config) },
                            onDelete  = { viewModel.deleteWeatherConfig(config) }
                        )
                    }
                }

                Spacer(Modifier.height(8.dp))

                Button(
                    onClick  = onAddConfig,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    colors   = ButtonDefaults.buttonColors(
                        containerColor = WarmOrange,
                        contentColor   = MaterialTheme.colorScheme.onPrimary
                    )
                ) {
                    Text("+")
                }
            }
        }
    }
}
