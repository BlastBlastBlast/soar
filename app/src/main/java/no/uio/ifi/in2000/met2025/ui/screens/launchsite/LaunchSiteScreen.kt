package no.uio.ifi.in2000.met2025.ui.screens.launchsite

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import no.uio.ifi.in2000.met2025.data.local.Database.LaunchSite


@Composable
fun LaunchSiteScreen(viewModel: LaunchSiteViewModel = hiltViewModel()) {
    val launchSites by viewModel.launchSites.collectAsState(initial = emptyList())

    Column(modifier = Modifier.padding(16.dp)) {
        Text("Lagrede Launchsites", style = MaterialTheme.typography.titleLarge)

        // Viser lagrede koordinater
        LazyColumn {
            items(launchSites) { site ->
                Row(modifier = Modifier.fillMaxWidth().padding(8.dp)) {
                    Text("${site.name}: (${site.latitude}, ${site.longitude})")
                    Spacer(modifier = Modifier.weight(1f))
                    IconButton(onClick = { viewModel.deleteLaunchSite(site) }) {
                        Icon(Icons.Default.Delete, contentDescription = "Slett")
                    }
                }
            }
        }

        // Knapp for å legge til en ny favoritt
        Button(onClick = {
            viewModel.addLaunchSite(59.9139, 10.7522, "Oslo")
        }) {
            Text("Legg til koordinat")
        }
    }
}

@Preview(showBackground = true)
@Composable
fun LaunchSiteScreenPreview() {
    val mockSites = listOf(
        LaunchSite(uid = 1, latitude = 59.9139, longitude = 10.7522, name = "Oslo"),
        LaunchSite(uid = 2, latitude = 60.4720, longitude = 8.4689, name = "Norge midtpunkt")
    )

    // Manuelt preview uten ViewModel
    MaterialTheme {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Lagrede Launchsites", style = MaterialTheme.typography.titleLarge)
            Spacer(modifier = Modifier.height(8.dp))

            LazyColumn {
                items(mockSites) { site ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp)
                    ) {
                        Text("${site.name}: (${site.latitude}, ${site.longitude})")
                        Spacer(modifier = Modifier.weight(1f))
                        IconButton(onClick = {}) {
                            Icon(Icons.Filled.Delete, contentDescription = "Slett")
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(onClick = {}
                ,  modifier = Modifier.align(Alignment.CenterHorizontally)) {

                Text("Legg til LaunchSite")
            }
        }
    }
}
