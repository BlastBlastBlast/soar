// File: RocketConfigEditScreen.kt
package no.uio.ifi.in2000.met2025.ui.screens.rocketconfig

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import no.uio.ifi.in2000.met2025.data.models.getDefaultRocketParameterValues
import no.uio.ifi.in2000.met2025.data.local.database.RocketConfig
import no.uio.ifi.in2000.met2025.ui.AppOutlinedTextField
import no.uio.ifi.in2000.met2025.ui.theme.WarmOrange

@Composable
fun RocketConfigEditScreen(
    rocketParameters: RocketConfig? = null,
    viewModel: RocketConfigEditViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit
) {
    val defaults = getDefaultRocketParameterValues().valueMap
    var name            by remember(rocketParameters) { mutableStateOf(rocketParameters?.name ?: "New Rocket Config") }
    var apogee          by remember(rocketParameters) { mutableStateOf(rocketParameters?.apogee?.toString() ?: (defaults["APOGEE"]?.toString() ?: "5000.0")) }
    var launchDirection by remember(rocketParameters) { mutableStateOf(rocketParameters?.launchDirection?.toString() ?: (defaults["LAUNCH_DIRECTION"]?.toString() ?: "90.0")) }
    var launchAngle     by remember(rocketParameters) { mutableStateOf(rocketParameters?.launchAngle?.toString() ?: (defaults["LAUNCH_ANGLE"]?.toString() ?: "80.0")) }
    var thrust          by remember(rocketParameters) { mutableStateOf(rocketParameters?.thrust?.toString() ?: (defaults["THRUST_NEWTONS"]?.toString() ?: "4500.0")) }
    var burnTime        by remember(rocketParameters) { mutableStateOf(rocketParameters?.burnTime?.toString() ?: (defaults["BURN_TIME"]?.toString() ?: "12.0")) }
    var dryWeight       by remember(rocketParameters) { mutableStateOf(rocketParameters?.dryWeight?.toString() ?: (defaults["DRY_WEIGHT"]?.toString() ?: "100.0")) }
    var wetWeight       by remember(rocketParameters) { mutableStateOf(rocketParameters?.wetWeight?.toString() ?: (defaults["WET_WEIGHT"]?.toString() ?: "130.0")) }
    var resolution      by remember(rocketParameters) { mutableStateOf(rocketParameters?.resolution?.toString() ?: (defaults["RESOLUTION"]?.toString() ?: "1.0")) }

    Box(
        Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Wrapper on "surface" with tonal+shadow
        Surface(
            modifier        = Modifier
                .fillMaxSize()
                .padding(16.dp),
            color           = MaterialTheme.colorScheme.surface,
            tonalElevation  = 4.dp,
            shadowElevation = 8.dp,
            shape           = RoundedCornerShape(12.dp)
        ) {
            Column(
                Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
            ) {
                // Orange header band
                Box(
                    Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .background(WarmOrange, RoundedCornerShape(4.dp))
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text      = if (rocketParameters == null) "NEW ROCKET CONFIG" else "EDIT ROCKET CONFIG",
                        style     = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold),
                        color     = MaterialTheme.colorScheme.onPrimary,
                        textAlign = TextAlign.Center,
                        modifier  = Modifier.fillMaxWidth()
                    )
                }

                Spacer(Modifier.height(8.dp))

                // Form fields
                Column(Modifier.padding(horizontal = 16.dp)) {
                    AppOutlinedTextField(
                        value         = name,
                        onValueChange = { name = it },
                        label         = { Text("Configuration Name") },
                        modifier      = Modifier.fillMaxWidth()
                    )
                    Spacer(Modifier.height(8.dp))
                    AppOutlinedTextField(
                        value         = apogee,
                        onValueChange = { apogee = it },
                        label         = { Text("Apogee (m)") },
                        modifier      = Modifier.fillMaxWidth()
                    )
                    Spacer(Modifier.height(8.dp))
                    AppOutlinedTextField(
                        value         = launchDirection,
                        onValueChange = { launchDirection = it },
                        label         = { Text("Launch Direction (°)") },
                        modifier      = Modifier.fillMaxWidth()
                    )
                    Spacer(Modifier.height(8.dp))
                    AppOutlinedTextField(
                        value         = launchAngle,
                        onValueChange = { launchAngle = it },
                        label         = { Text("Launch Angle (°)") },
                        modifier      = Modifier.fillMaxWidth()
                    )
                    Spacer(Modifier.height(8.dp))
                    AppOutlinedTextField(
                        value         = thrust,
                        onValueChange = { thrust = it },
                        label         = { Text("Thrust (N)") },
                        modifier      = Modifier.fillMaxWidth()
                    )
                    Spacer(Modifier.height(8.dp))
                    AppOutlinedTextField(
                        value         = burnTime,
                        onValueChange = { burnTime = it },
                        label         = { Text("Burn Time (s)") },
                        modifier      = Modifier.fillMaxWidth()
                    )
                    Spacer(Modifier.height(8.dp))
                    AppOutlinedTextField(
                        value         = dryWeight,
                        onValueChange = { dryWeight = it },
                        label         = { Text("Dry Weight (kg)") },
                        modifier      = Modifier.fillMaxWidth()
                    )
                    Spacer(Modifier.height(8.dp))
                    AppOutlinedTextField(
                        value         = wetWeight,
                        onValueChange = { wetWeight = it },
                        label         = { Text("Wet Weight (kg)") },
                        modifier      = Modifier.fillMaxWidth()
                    )
                    Spacer(Modifier.height(8.dp))
                    AppOutlinedTextField(
                        value         = resolution,
                        onValueChange = { resolution = it },
                        label         = { Text("Resolution") },
                        modifier      = Modifier.fillMaxWidth()
                    )
                }

                Spacer(Modifier.height(16.dp))

                // Save button
                Button(
                    onClick  = {
                        val updated = RocketConfig(
                            id              = rocketParameters?.id ?: 0,
                            name            = name,
                            apogee          = apogee.toDoubleOrNull()          ?: defaults["APOGEE"]!!,
                            launchDirection = launchDirection.toDoubleOrNull() ?: defaults["LAUNCH_DIRECTION"]!!,
                            launchAngle     = launchAngle.toDoubleOrNull()     ?: defaults["LAUNCH_ANGLE"]!!,
                            thrust          = thrust.toDoubleOrNull()          ?: defaults["THRUST_NEWTONS"]!!,
                            burnTime        = burnTime.toDoubleOrNull()        ?: defaults["BURN_TIME"]!!,
                            dryWeight       = dryWeight.toDoubleOrNull()       ?: defaults["DRY_WEIGHT"]!!,
                            wetWeight       = wetWeight.toDoubleOrNull()       ?: defaults["WET_WEIGHT"]!!,
                            resolution      = resolution.toDoubleOrNull()      ?: defaults["RESOLUTION"]!!,
                            isDefault       = rocketParameters?.isDefault ?: false
                        )
                        if (rocketParameters == null) viewModel.saveRocketConfig(updated)
                        else viewModel.updateRocketConfig(updated)
                        onNavigateBack()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    colors   = ButtonDefaults.buttonColors(
                        containerColor = WarmOrange,
                        contentColor   = MaterialTheme.colorScheme.onPrimary
                    )
                ) {
                    Text("Save Rocket Configuration")
                }

                Spacer(Modifier.height(16.dp))
            }
        }
    }
}