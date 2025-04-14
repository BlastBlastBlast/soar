package no.uio.ifi.in2000.met2025.ui.screens.rocketconfig

// File: RocketConfigListScreen.kt

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import no.uio.ifi.in2000.met2025.data.local.database.RocketParameters
import androidx.compose.material3.Button


@Composable
fun RocketConfigListScreen(
    viewModel: RocketConfigListViewModel = hiltViewModel(),
    onEditRocketConfig: (RocketParameters) -> Unit,
    onAddRocketConfig: () -> Unit,
    onSelectRocketConfig: (RocketParameters) -> Unit
) {
    // Observe the list of rocket configs from the viewmodel.
    val rocketList by viewModel.rocketList.collectAsState(initial = emptyList())

    Column(modifier = Modifier.padding(16.dp)) {
        LazyColumn {
            items(rocketList) { rocket ->
                RocketConfigItem(
                    rocketParameters = rocket,
                    onClick = { onSelectRocketConfig(rocket) },
                    onEdit = {
                        // Only allow editing for non-default configurations.
                        if (!rocket.isDefault) onEditRocketConfig(rocket)
                    },
                    onDelete = {
                        if (!rocket.isDefault) viewModel.deleteRocketConfig(rocket)
                    }
                )
            }
        }
        Spacer(modifier = Modifier.height(16.dp))

        Button(onClick = onAddRocketConfig, modifier = Modifier.fillMaxWidth()) {
            Text("Add New Configuration")
        }
    }
}

@Composable
fun RocketConfigItem(
    rocketParameters: RocketParameters,
    onClick: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable { onClick() },
        shape = MaterialTheme.shapes.medium,
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = rocketParameters.name,
                    style = MaterialTheme.typography.bodyLarge
                )
                if (rocketParameters.isDefault) {
                    Text(
                        text = "Default Configuration",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
            Row {
                // Only show edit and delete icons for non-default configurations.
                if (!rocketParameters.isDefault) {
                    IconButton(onClick = onEdit) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit"
                        )
                    }
                    IconButton(onClick = onDelete) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete"
                        )
                    }
                }
            }
        }
    }
}
