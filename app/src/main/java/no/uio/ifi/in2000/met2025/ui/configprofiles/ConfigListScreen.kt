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

@Composable
fun ConfigListScreen(
    viewModel: ConfigListViewModel = hiltViewModel(),
    onEditConfig: (ConfigProfile) -> Unit, // Called when the user taps the edit icon.
    onAddConfig: () -> Unit,               // Called when the user taps the "+" button.
    onSelectConfig: (ConfigProfile) -> Unit  // Called when the user taps an item to make it active.
) {
    val configList by viewModel.configList.collectAsState(initial = emptyList())

    Column(modifier = Modifier.padding(16.dp)) {
        Text("Saved Configurations", style = MaterialTheme.typography.titleLarge)
        Spacer(modifier = Modifier.height(8.dp))
        LazyColumn {
            items(configList) { config ->
                ConfigProfileItem(
                    config = config,
                    onClick = { onSelectConfig(config) },
                    onEdit = { onEditConfig(config) },
                    onDelete = { viewModel.deleteConfig(config) }
                )
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = onAddConfig,
        ) {
            Text("+")
        }
    }
}
