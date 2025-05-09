package no.uio.ifi.in2000.met2025.ui.screens.settings.rocketconfig

// File: RocketConfigListScreen.kt

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.ui.Alignment
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import no.uio.ifi.in2000.met2025.data.local.database.RocketConfig
import no.uio.ifi.in2000.met2025.ui.screens.settings.SettingsViewModel
import no.uio.ifi.in2000.met2025.ui.theme.WarmOrange

@Composable
fun RocketConfigListScreen(
    viewModel: SettingsViewModel = hiltViewModel(),
    onEditRocketConfig: (RocketConfig) -> Unit,
    onAddRocketConfig: () -> Unit,
    onSelectRocketConfig: (RocketConfig) -> Unit
) {
    val rockets by viewModel.rocketConfigs.collectAsState(initial = emptyList())

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
            tonalElevation  = 4.dp,
            shadowElevation = 8.dp,
            shape           = RoundedCornerShape(12.dp)
        ) {
            Column(Modifier.fillMaxSize()) {
                // Orange header band
                Box(
                    Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .background(WarmOrange, RoundedCornerShape(4.dp))
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                        .semantics { heading() }
                    ,
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "ROCKET CONFIGURATIONS",
                        style     = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold),
                        color     = MaterialTheme.colorScheme.onPrimary,
                        textAlign = TextAlign.Center,
                        modifier  = Modifier.fillMaxWidth()
                    )
                }

                Spacer(Modifier.height(8.dp))

                LazyColumn(
                    Modifier
                        .weight(1f)
                        .fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding      = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    items(rockets) { rocket ->
                        RocketConfigItem(
                            rocketConfig = rocket,
                            onClick      = { onSelectRocketConfig(rocket) },
                            onEdit       = { if (!rocket.isDefault) onEditRocketConfig(rocket) },
                            onDelete     = { if (!rocket.isDefault) viewModel.deleteRocketConfig(rocket) }
                        )
                    }
                }

                Spacer(Modifier.height(8.dp))

                Button(
                    onClick    = onAddRocketConfig,
                    modifier   = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .semantics {
                            role = Role.Button
                            contentDescription = "Add new rocket configuration"
                        },
                    colors     = ButtonDefaults.buttonColors(
                        containerColor = WarmOrange,
                        contentColor   = MaterialTheme.colorScheme.onPrimary
                    )
                ) {
                    Text("+")
                }

                Spacer(Modifier.height(16.dp))
            }
        }
    }
}
