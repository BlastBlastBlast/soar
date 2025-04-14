// File: RocketConfigEditScreen.kt
package no.uio.ifi.in2000.met2025.ui.screens.rocketconfig

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import no.uio.ifi.in2000.met2025.data.local.database.RocketParameters

@Composable
fun RocketConfigEditScreen(
    rocketParameters: RocketParameters? = null,
    viewModel: RocketConfigEditViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit
) {
    // Initialize state with defaults or existing values.
    var name by remember { mutableStateOf(rocketParameters?.name ?: "") }
    var apogee by remember { mutableStateOf(rocketParameters?.apogee?.toString() ?: "5000.0") }
    var launchDirection by remember { mutableStateOf(rocketParameters?.launchDirection?.toString() ?: "90.0") }
    var launchAngle by remember { mutableStateOf(rocketParameters?.launchAngle?.toString() ?: "80.0") }
    var thrust by remember { mutableStateOf(rocketParameters?.thrust?.toString() ?: "4500.0") }
    var burnTime by remember { mutableStateOf(rocketParameters?.burnTime?.toString() ?: "12.0") }
    var dryWeight by remember { mutableStateOf(rocketParameters?.dryWeight?.toString() ?: "100.0") }
    var wetWeight by remember { mutableStateOf(rocketParameters?.wetWeight?.toString() ?: "130.0") }
    var resolution by remember { mutableStateOf(rocketParameters?.resolution?.toString() ?: "1.0") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Configuration Name") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = apogee,
            onValueChange = { apogee = it },
            label = { Text("Apogee (m)") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = launchDirection,
            onValueChange = { launchDirection = it },
            label = { Text("Launch Direction (°)") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = launchAngle,
            onValueChange = { launchAngle = it },
            label = { Text("Launch Angle (°)") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = thrust,
            onValueChange = { thrust = it },
            label = { Text("Thrust (Newtons)") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = burnTime,
            onValueChange = { burnTime = it },
            label = { Text("Burn Time (s)") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = dryWeight,
            onValueChange = { dryWeight = it },
            label = { Text("Dry Weight (kg)") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = wetWeight,
            onValueChange = { wetWeight = it },
            label = { Text("Wet Weight (kg)") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = resolution,
            onValueChange = { resolution = it },
            label = { Text("Resolution") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = {
                val updatedRocketParameters = RocketParameters(
                    id = rocketParameters?.id ?: 0,
                    name = name,
                    apogee = apogee.toDoubleOrNull() ?: 5000.0,
                    launchDirection = launchDirection.toDoubleOrNull() ?: 90.0,
                    launchAngle = launchAngle.toDoubleOrNull() ?: 80.0,
                    thrust = thrust.toDoubleOrNull() ?: 4500.0,
                    burnTime = burnTime.toDoubleOrNull() ?: 12.0,
                    dryWeight = dryWeight.toDoubleOrNull() ?: 100.0,
                    wetWeight = wetWeight.toDoubleOrNull() ?: 130.0,
                    resolution = resolution.toDoubleOrNull() ?: 1.0,
                    isDefault = rocketParameters?.isDefault ?: false
                )
                if (rocketParameters == null)
                    viewModel.saveRocketConfig(updatedRocketParameters)
                else
                    viewModel.updateRocketConfig(updatedRocketParameters)
                onNavigateBack()
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Save Rocket Configuration")
        }
    }
}
