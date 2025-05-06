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
import no.uio.ifi.in2000.met2025.data.models.RocketParameterType
import no.uio.ifi.in2000.met2025.ui.common.AppOutlinedTextField
import no.uio.ifi.in2000.met2025.ui.screens.settings.SettingsViewModel
import no.uio.ifi.in2000.met2025.ui.theme.WarmOrange

@Composable
fun RocketConfigEditScreen(
    rocketParameters: RocketConfig? = null,
    viewModel: SettingsViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit
) {
    // ➊ collect the rocket‐edit status
    val updateStatus by viewModel.rocketUpdateStatus.collectAsState()

    // UI state
    val defaultsMap = getDefaultRocketParameterValues().valueMap
    var name            by remember(rocketParameters) { mutableStateOf(rocketParameters?.name ?: "New Rocket Config") }
    var apogee          by remember(rocketParameters) { mutableStateOf(rocketParameters?.apogee?.toString() ?: (defaultsMap[RocketParameterType.APOGEE.name]?.toString() ?: "")) }
    var launchDirection by remember(rocketParameters) { mutableStateOf(rocketParameters?.launchDirection?.toString() ?: (defaultsMap[RocketParameterType.LAUNCH_DIRECTION.name]?.toString() ?: "")) }
    var launchAngle     by remember(rocketParameters) { mutableStateOf(rocketParameters?.launchAngle?.toString() ?: (defaultsMap[RocketParameterType.LAUNCH_ANGLE.name]?.toString() ?: "")) }
    var thrust          by remember(rocketParameters) { mutableStateOf(rocketParameters?.thrust?.toString() ?: (defaultsMap[RocketParameterType.THRUST_NEWTONS.name]?.toString() ?: "")) }
    var burnTime        by remember(rocketParameters) { mutableStateOf(rocketParameters?.burnTime?.toString() ?: (defaultsMap[RocketParameterType.BURN_TIME.name]?.toString() ?: "")) }
    var dryWeight       by remember(rocketParameters) { mutableStateOf(rocketParameters?.dryWeight?.toString() ?: (defaultsMap[RocketParameterType.DRY_WEIGHT.name]?.toString() ?: "")) }
    var wetWeight       by remember(rocketParameters) { mutableStateOf(rocketParameters?.wetWeight?.toString() ?: (defaultsMap[RocketParameterType.WET_WEIGHT.name]?.toString() ?: "")) }
    var resolution      by remember(rocketParameters) { mutableStateOf(rocketParameters?.resolution?.toString() ?: (defaultsMap[RocketParameterType.RESOLUTION.name]?.toString() ?: "")) }
    var bodyDiameter    by remember(rocketParameters) { mutableStateOf(rocketParameters?.bodyDiameter?.toString() ?: (defaultsMap[RocketParameterType.BODY_DIAMETER.name]?.toString() ?: "")) }
    var dragCoefficient by remember(rocketParameters) { mutableStateOf(rocketParameters?.dragCoefficient?.toString() ?: (defaultsMap[RocketParameterType.DRAG_COEFFICIENT.name]?.toString() ?: "")) }
    var parachuteArea   by remember(rocketParameters) { mutableStateOf(rocketParameters?.parachuteArea?.toString() ?: (defaultsMap[RocketParameterType.PARACHUTE_AREA.name]?.toString() ?: "")) }
    var parachuteDragCoefficient by remember(rocketParameters) { mutableStateOf(rocketParameters?.parachuteDragCoefficient?.toString() ?: (defaultsMap[RocketParameterType.PARACHUTE_DRAG_COEFFICIENT.name]?.toString() ?: "")) }

    // ➋ re-check name uniqueness
    LaunchedEffect(name) {
        viewModel.checkRocketNameAvailability(name)
    }

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
            Column(
                Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
            ) {
                // Header
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

                Spacer(Modifier.height(16.dp))

                // Name + error
                AppOutlinedTextField(
                    value         = name,
                    onValueChange = { name = it },
                    label         = { Text("Configuration Name") },
                    modifier      = Modifier.fillMaxWidth()
                )
                if (updateStatus is SettingsViewModel.UpdateStatus.Error) {
                    Text(
                        text  = (updateStatus as SettingsViewModel.UpdateStatus.Error).message,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }

                Spacer(Modifier.height(16.dp))

                // Parameter fields
                AppOutlinedTextField(value = apogee,          onValueChange = { apogee          = it }, label = { Text("Apogee (m)") }, modifier = Modifier.fillMaxWidth())
                Spacer(Modifier.height(8.dp))
                AppOutlinedTextField(value = launchDirection, onValueChange = { launchDirection = it }, label = { Text("Launch Direction (°)") }, modifier = Modifier.fillMaxWidth())
                Spacer(Modifier.height(8.dp))
                AppOutlinedTextField(value = launchAngle,     onValueChange = { launchAngle     = it }, label = { Text("Launch Angle (°)") }, modifier = Modifier.fillMaxWidth())
                Spacer(Modifier.height(8.dp))
                AppOutlinedTextField(value = thrust,          onValueChange = { thrust          = it }, label = { Text("Thrust (N)") }, modifier = Modifier.fillMaxWidth())
                Spacer(Modifier.height(8.dp))
                AppOutlinedTextField(value = burnTime,        onValueChange = { burnTime        = it }, label = { Text("Burn Time (s)") }, modifier = Modifier.fillMaxWidth())
                Spacer(Modifier.height(8.dp))
                AppOutlinedTextField(value = dryWeight,       onValueChange = { dryWeight       = it }, label = { Text("Dry Weight (kg)") }, modifier = Modifier.fillMaxWidth())
                Spacer(Modifier.height(8.dp))
                AppOutlinedTextField(value = wetWeight,       onValueChange = { wetWeight       = it }, label = { Text("Wet Weight (kg)") }, modifier = Modifier.fillMaxWidth())
                Spacer(Modifier.height(8.dp))
                AppOutlinedTextField(value = resolution,      onValueChange = { resolution      = it }, label = { Text("Resolution") }, modifier = Modifier.fillMaxWidth())
                Spacer(Modifier.height(8.dp))
                AppOutlinedTextField(value = bodyDiameter,    onValueChange = { bodyDiameter    = it }, label = { Text("Body Diameter (m)") }, modifier = Modifier.fillMaxWidth())
                Spacer(Modifier.height(8.dp))
                AppOutlinedTextField(value = dragCoefficient, onValueChange = { dragCoefficient = it }, label = { Text("Drag Coefficient") }, modifier = Modifier.fillMaxWidth())
                Spacer(Modifier.height(8.dp))
                AppOutlinedTextField(value = parachuteArea,   onValueChange = { parachuteArea   = it }, label = { Text("Parachute Area (m²)") }, modifier = Modifier.fillMaxWidth())
                Spacer(Modifier.height(8.dp))
                AppOutlinedTextField(
                    value         = parachuteDragCoefficient,
                    onValueChange = { parachuteDragCoefficient = it },
                    label         = { Text("Parachute Drag Coefficient") },
                    modifier      = Modifier.fillMaxWidth()
                )

                Spacer(Modifier.height(24.dp))

                // Save button
                Button(
                    onClick = {
                        val updated = RocketConfig(
                            id                          = rocketParameters?.id ?: 0,
                            name                        = name,
                            apogee                      = apogee.toDoubleOrNull()          ?: defaultsMap[RocketParameterType.APOGEE.name]!!,
                            launchDirection             = launchDirection.toDoubleOrNull() ?: defaultsMap[RocketParameterType.LAUNCH_DIRECTION.name]!!,
                            launchAngle                 = launchAngle.toDoubleOrNull()     ?: defaultsMap[RocketParameterType.LAUNCH_ANGLE.name]!!,
                            thrust                      = thrust.toDoubleOrNull()          ?: defaultsMap[RocketParameterType.THRUST_NEWTONS.name]!!,
                            burnTime                    = burnTime.toDoubleOrNull()        ?: defaultsMap[RocketParameterType.BURN_TIME.name]!!,
                            dryWeight                   = dryWeight.toDoubleOrNull()       ?: defaultsMap[RocketParameterType.DRY_WEIGHT.name]!!,
                            wetWeight                   = wetWeight.toDoubleOrNull()       ?: defaultsMap[RocketParameterType.WET_WEIGHT.name]!!,
                            resolution                  = resolution.toDoubleOrNull()      ?: defaultsMap[RocketParameterType.RESOLUTION.name]!!,
                            bodyDiameter                = bodyDiameter.toDoubleOrNull()    ?: defaultsMap[RocketParameterType.BODY_DIAMETER.name]!!,
                            dragCoefficient             = dragCoefficient.toDoubleOrNull() ?: defaultsMap[RocketParameterType.DRAG_COEFFICIENT.name]!!,
                            parachuteArea               = parachuteArea.toDoubleOrNull()   ?: defaultsMap[RocketParameterType.PARACHUTE_AREA.name]!!,
                            parachuteDragCoefficient    = parachuteDragCoefficient.toDoubleOrNull()
                                ?: defaultsMap[RocketParameterType.PARACHUTE_DRAG_COEFFICIENT.name]!!,
                            isDefault                   = rocketParameters?.isDefault ?: false
                        )
                        if (rocketParameters == null) {
                            viewModel.saveRocketConfig(updated)
                        } else {
                            viewModel.updateRocketConfig(updated)
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = WarmOrange,
                        contentColor   = MaterialTheme.colorScheme.onPrimary
                    )
                ) {
                    Text("Save Rocket Configuration")
                }
            }
        }
    }

    // ➍ navigate back on success
    LaunchedEffect(updateStatus) {
        if (updateStatus is SettingsViewModel.UpdateStatus.Success) {
            viewModel.resetRocketStatus()
            onNavigateBack()
        }
    }
}
