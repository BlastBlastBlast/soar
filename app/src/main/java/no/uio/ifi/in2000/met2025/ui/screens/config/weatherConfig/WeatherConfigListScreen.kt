package no.uio.ifi.in2000.met2025.ui.screens.config.weatherConfig

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
import no.uio.ifi.in2000.met2025.data.local.database.WeatherConfig
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import no.uio.ifi.in2000.met2025.ui.theme.WarmOrange
import androidx.compose.ui.Alignment
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import no.uio.ifi.in2000.met2025.ui.screens.config.ConfigViewModel

/**
 * WeatherConfigListScreen
 *
 * Displays the list of saved WeatherConfig profiles in a scrollable column.
 * Highlights default profiles and disables edit/delete for them.
 * Every non-default item can be selected, edited, or deleted.
 * Also provides a button to add a new configuration.
 */
@Composable
fun WeatherConfigListScreen(
    viewModel: ConfigViewModel = hiltViewModel(),
    onEditConfig: (WeatherConfig) -> Unit,
    onAddConfig: () -> Unit,
    onSelectConfig: (WeatherConfig) -> Unit
) {
    val weatherConfigList by viewModel.weatherConfigs.collectAsState(initial = emptyList())

    Box(
        Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Surface(
            modifier        = Modifier
                .fillMaxSize()
                .padding(16.dp),
            color           = MaterialTheme.colorScheme.primary,
            tonalElevation  = 8.dp,    // subtle tint under items
            shadowElevation = 10.dp,
            shape           = RoundedCornerShape(12.dp)
        ) {
            Column(Modifier.fillMaxSize()) {
                // — HEADER IN ORANGE BAND —
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .background(WarmOrange, RoundedCornerShape(4.dp))
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                        .semantics { heading() },
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
                    items(weatherConfigList) { weatherConfig ->
                        WeatherConfigListItem(
                            weatherConfig    = weatherConfig,
                            onClick   = { onSelectConfig(weatherConfig) },
                            onEdit    = { onEditConfig(weatherConfig) },
                            onDelete  = { viewModel.deleteWeatherConfig(weatherConfig) }
                        )
                    }
                }

                Spacer(Modifier.height(8.dp))

                Button(
                    onClick  = onAddConfig,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .semantics {
                            role = Role.Button
                            contentDescription = "Add new weather configuration"
                        },
                    colors   = ButtonDefaults.buttonColors(
                        containerColor = WarmOrange,
                        contentColor   = MaterialTheme.colorScheme.onPrimary
                    )
                ) {
                    Text(text = "+", style = MaterialTheme.typography.titleLarge)
                }
                Spacer(Modifier.height(16.dp))
            }
        }
    }
}
