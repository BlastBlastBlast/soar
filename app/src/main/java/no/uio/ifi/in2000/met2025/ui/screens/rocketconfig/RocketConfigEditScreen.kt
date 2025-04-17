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
import no.uio.ifi.in2000.met2025.data.models.RocketParameterValues
import no.uio.ifi.in2000.met2025.data.models.getDefaultRocketParameterValues
import no.uio.ifi.in2000.met2025.data.local.database.RocketConfig
import no.uio.ifi.in2000.met2025.ui.AppOutlinedTextField

@Composable
fun RocketConfigEditScreen(
    rocketParameters: RocketConfig? = null,
    viewModel: RocketConfigEditViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit
) {
    // Get default values from the model function if no record is passed in.
    val defaultValues: RocketParameterValues = getDefaultRocketParameterValues()

    var name by remember(rocketParameters) { mutableStateOf(rocketParameters?.name ?: "New Rocket Config") }
    var apogee by remember(rocketParameters) { mutableStateOf(rocketParameters?.apogee?.toString() ?: defaultValues.valueMap["APOGEE"]?.toString() ?: "5000.0") }
    var launchDirection by remember(rocketParameters) { mutableStateOf(rocketParameters?.launchDirection?.toString() ?: defaultValues.valueMap["LAUNCH_DIRECTION"]?.toString() ?: "90.0") }
    var launchAngle by remember(rocketParameters) { mutableStateOf(rocketParameters?.launchAngle?.toString() ?: defaultValues.valueMap["LAUNCH_ANGLE"]?.toString() ?: "80.0") }
    var thrust by remember(rocketParameters) { mutableStateOf(rocketParameters?.thrust?.toString() ?: defaultValues.valueMap["THRUST_NEWTONS"]?.toString() ?: "4500.0") }
    var burnTime by remember(rocketParameters) { mutableStateOf(rocketParameters?.burnTime?.toString() ?: defaultValues.valueMap["BURN_TIME"]?.toString() ?: "12.0") }
    var dryWeight by remember(rocketParameters) { mutableStateOf(rocketParameters?.dryWeight?.toString() ?: defaultValues.valueMap["DRY_WEIGHT"]?.toString() ?: "100.0") }
    var wetWeight by remember(rocketParameters) { mutableStateOf(rocketParameters?.wetWeight?.toString() ?: defaultValues.valueMap["WET_WEIGHT"]?.toString() ?: "130.0") }
    var resolution by remember(rocketParameters) { mutableStateOf(rocketParameters?.resolution?.toString() ?: defaultValues.valueMap["RESOLUTION"]?.toString() ?: "1.0") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        AppOutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Configuration Name") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))
        AppOutlinedTextField(
            value = apogee,
            onValueChange = { apogee = it },
            label = { Text("Apogee (m)") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))
        AppOutlinedTextField(
            value = launchDirection,
            onValueChange = { launchDirection = it },
            label = { Text("Launch Direction (°)") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))
        AppOutlinedTextField(
            value = launchAngle,
            onValueChange = { launchAngle = it },
            label = { Text("Launch Angle (°)") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))
        AppOutlinedTextField(
            value = thrust,
            onValueChange = { thrust = it },
            label = { Text("Thrust (Newtons)") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))
        AppOutlinedTextField(
            value = burnTime,
            onValueChange = { burnTime = it },
            label = { Text("Burn Time (s)") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))
        AppOutlinedTextField(
            value = dryWeight,
            onValueChange = { dryWeight = it },
            label = { Text("Dry Weight (kg)") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))
        AppOutlinedTextField(
            value = wetWeight,
            onValueChange = { wetWeight = it },
            label = { Text("Wet Weight (kg)") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))
        AppOutlinedTextField(
            value = resolution,
            onValueChange = { resolution = it },
            label = { Text("Resolution") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = {
                // Create the new RocketConfig by mapping the model values.
                val updatedConfig = RocketConfig(
                    id = rocketParameters?.id ?: 0,
                    name = name,
                    apogee = apogee.toDoubleOrNull() ?: (defaultValues.valueMap["APOGEE"]
                        ?: 5000.0),
                    launchDirection = launchDirection.toDoubleOrNull()
                        ?: (defaultValues.valueMap["LAUNCH_DIRECTION"] ?: 90.0),
                    launchAngle = launchAngle.toDoubleOrNull()
                        ?: (defaultValues.valueMap["LAUNCH_ANGLE"]
                            ?: 80.0),
                    thrust = thrust.toDoubleOrNull() ?: (defaultValues.valueMap["THRUST_NEWTONS"]
                        ?: 4500.0),
                    burnTime = burnTime.toDoubleOrNull() ?: (defaultValues.valueMap["BURN_TIME"]
                        ?: 12.0),
                    dryWeight = dryWeight.toDoubleOrNull() ?: (defaultValues.valueMap["DRY_WEIGHT"]
                        ?: 100.0),
                    wetWeight = wetWeight.toDoubleOrNull() ?: (defaultValues.valueMap["WET_WEIGHT"]
                        ?: 130.0),
                    resolution = resolution.toDoubleOrNull()
                        ?: (defaultValues.valueMap["RESOLUTION"] ?: 1.0),
                    isDefault = rocketParameters?.isDefault ?: false
                )
                if (rocketParameters == null) {
                    viewModel.saveRocketConfig(updatedConfig)
                } else {
                    viewModel.updateRocketConfig(updatedConfig)
                }
                onNavigateBack()
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Save Rocket Configuration")
        }
    }
}