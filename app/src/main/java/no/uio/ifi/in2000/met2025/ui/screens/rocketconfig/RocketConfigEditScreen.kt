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
import no.uio.ifi.in2000.met2025.ui.theme.WarmOrange

@Composable
fun RocketConfigEditScreen(
    rocketParameters: RocketConfig? = null,
    viewModel: RocketConfigEditViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit
) {
    val defaultsMap = getDefaultRocketParameterValues().valueMap

    var name by remember(rocketParameters) { mutableStateOf(rocketParameters?.name ?: "New Rocket Config") }
    var launchAzimuth by remember(rocketParameters) { mutableStateOf(rocketParameters?.launchAzimuth?.toString()
        ?: defaultsMap[RocketParameterType.LAUNCH_AZIMUTH.name]?.toString() ?: "") }
    var launchPitch by remember(rocketParameters) { mutableStateOf(rocketParameters?.launchPitch?.toString()
        ?: defaultsMap[RocketParameterType.LAUNCH_PITCH.name]?.toString() ?: "") }
    var launchRailLength by remember(rocketParameters) { mutableStateOf(rocketParameters?.launchRailLength?.toString()
        ?: defaultsMap[RocketParameterType.LAUNCH_RAIL_LENGTH.name]?.toString() ?: "") }
    var wetMass by remember(rocketParameters) { mutableStateOf(rocketParameters?.wetMass?.toString()
        ?: defaultsMap[RocketParameterType.WET_MASS.name]?.toString() ?: "") }
    var dryMass by remember(rocketParameters) { mutableStateOf(rocketParameters?.dryMass?.toString()
        ?: defaultsMap[RocketParameterType.DRY_MASS.name]?.toString() ?: "") }
    var burnTime by remember(rocketParameters) { mutableStateOf(rocketParameters?.burnTime?.toString()
        ?: defaultsMap[RocketParameterType.BURN_TIME.name]?.toString() ?: "") }
    var thrust by remember(rocketParameters) { mutableStateOf(rocketParameters?.thrust?.toString()
        ?: defaultsMap[RocketParameterType.THRUST.name]?.toString() ?: "") }
    var stepSize by remember(rocketParameters) { mutableStateOf(rocketParameters?.stepSize?.toString()
        ?: defaultsMap[RocketParameterType.STEP_SIZE.name]?.toString() ?: "") }
    var crossSectionalArea by remember(rocketParameters) { mutableStateOf(rocketParameters?.crossSectionalArea?.toString()
        ?: defaultsMap[RocketParameterType.CROSS_SECTIONAL_AREA.name]?.toString() ?: "") }
    var dragCoefficient by remember(rocketParameters) { mutableStateOf(rocketParameters?.dragCoefficient?.toString()
        ?: defaultsMap[RocketParameterType.DRAG_COEFFICIENT.name]?.toString() ?: "") }
    var parachuteCrossSectionalArea by remember(rocketParameters) { mutableStateOf(rocketParameters?.parachuteCrossSectionalArea?.toString()
        ?: defaultsMap[RocketParameterType.PARACHUTE_CROSS_SECTIONAL_AREA.name]?.toString() ?: "") }
    var parachuteDragCoefficient by remember(rocketParameters) { mutableStateOf(rocketParameters?.parachuteDragCoefficient?.toString()
        ?: defaultsMap[RocketParameterType.PARACHUTE_DRAG_COEFFICIENT.name]?.toString() ?: "") }

    Box(
        Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 4.dp,
            shadowElevation = 8.dp,
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(
                Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
            ) {
                Box(
                    Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .background(WarmOrange, RoundedCornerShape(4.dp))
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (rocketParameters == null) "NEW ROCKET CONFIG" else "EDIT ROCKET CONFIG",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold),
                        color = MaterialTheme.colorScheme.onPrimary,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                Spacer(Modifier.height(8.dp))

                Column(Modifier.padding(horizontal = 16.dp)) {
                    AppOutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Configuration Name") }, modifier = Modifier.fillMaxWidth())
                    Spacer(Modifier.height(8.dp))
                    AppOutlinedTextField(value = launchAzimuth, onValueChange = { launchAzimuth = it }, label = { Text("Launch Azimuth (°)") }, modifier = Modifier.fillMaxWidth())
                    Spacer(Modifier.height(8.dp))
                    AppOutlinedTextField(value = launchPitch, onValueChange = { launchPitch = it }, label = { Text("Launch Pitch (°)") }, modifier = Modifier.fillMaxWidth())
                    Spacer(Modifier.height(8.dp))
                    AppOutlinedTextField(value = launchRailLength, onValueChange = { launchRailLength = it }, label = { Text("Launch Rail Length (m)") }, modifier = Modifier.fillMaxWidth())
                    Spacer(Modifier.height(8.dp))
                    AppOutlinedTextField(value = wetMass, onValueChange = { wetMass = it }, label = { Text("Wet Mass (kg)") }, modifier = Modifier.fillMaxWidth())
                    Spacer(Modifier.height(8.dp))
                    AppOutlinedTextField(value = dryMass, onValueChange = { dryMass = it }, label = { Text("Dry Mass (kg)") }, modifier = Modifier.fillMaxWidth())
                    Spacer(Modifier.height(8.dp))
                    AppOutlinedTextField(value = burnTime, onValueChange = { burnTime = it }, label = { Text("Burn Time (s)") }, modifier = Modifier.fillMaxWidth())
                    Spacer(Modifier.height(8.dp))
                    AppOutlinedTextField(value = thrust, onValueChange = { thrust = it }, label = { Text("Thrust (N)") }, modifier = Modifier.fillMaxWidth())
                    Spacer(Modifier.height(8.dp))
                    AppOutlinedTextField(value = stepSize, onValueChange = { stepSize = it }, label = { Text("Step Size (s)") }, modifier = Modifier.fillMaxWidth())
                    Spacer(Modifier.height(8.dp))
                    AppOutlinedTextField(value = crossSectionalArea, onValueChange = { crossSectionalArea = it }, label = { Text("Cross-Sectional Area (m²)") }, modifier = Modifier.fillMaxWidth())
                    Spacer(Modifier.height(8.dp))
                    AppOutlinedTextField(value = dragCoefficient, onValueChange = { dragCoefficient = it }, label = { Text("Drag Coefficient") }, modifier = Modifier.fillMaxWidth())
                    Spacer(Modifier.height(8.dp))
                    AppOutlinedTextField(value = parachuteCrossSectionalArea, onValueChange = { parachuteCrossSectionalArea = it }, label = { Text("Parachute Cross-Sectional Area (m²)") }, modifier = Modifier.fillMaxWidth())
                    Spacer(Modifier.height(8.dp))
                    AppOutlinedTextField(value = parachuteDragCoefficient, onValueChange = { parachuteDragCoefficient = it }, label = { Text("Parachute Drag Coefficient") }, modifier = Modifier.fillMaxWidth())
                }

                Spacer(Modifier.height(16.dp))

                Button(
                    onClick = {
                        val updated = RocketConfig(
                            id = rocketParameters?.id ?: 0,
                            name = name,
                            launchAzimuth = launchAzimuth.toDoubleOrNull() ?: defaultsMap[RocketParameterType.LAUNCH_AZIMUTH.name]!!,
                            launchPitch = launchPitch.toDoubleOrNull() ?: defaultsMap[RocketParameterType.LAUNCH_PITCH.name]!!,
                            launchRailLength = launchRailLength.toDoubleOrNull() ?: defaultsMap[RocketParameterType.LAUNCH_RAIL_LENGTH.name]!!,
                            wetMass = wetMass.toDoubleOrNull() ?: defaultsMap[RocketParameterType.WET_MASS.name]!!,
                            dryMass = dryMass.toDoubleOrNull() ?: defaultsMap[RocketParameterType.DRY_MASS.name]!!,
                            burnTime = burnTime.toDoubleOrNull() ?: defaultsMap[RocketParameterType.BURN_TIME.name]!!,
                            thrust = thrust.toDoubleOrNull() ?: defaultsMap[RocketParameterType.THRUST.name]!!,
                            stepSize = stepSize.toDoubleOrNull() ?: defaultsMap[RocketParameterType.STEP_SIZE.name]!!,
                            crossSectionalArea = crossSectionalArea.toDoubleOrNull() ?: defaultsMap[RocketParameterType.CROSS_SECTIONAL_AREA.name]!!,
                            dragCoefficient = dragCoefficient.toDoubleOrNull() ?: defaultsMap[RocketParameterType.DRAG_COEFFICIENT.name]!!,
                            parachuteCrossSectionalArea = parachuteCrossSectionalArea.toDoubleOrNull() ?: defaultsMap[RocketParameterType.PARACHUTE_CROSS_SECTIONAL_AREA.name]!!,
                            parachuteDragCoefficient = parachuteDragCoefficient.toDoubleOrNull() ?: defaultsMap[RocketParameterType.PARACHUTE_DRAG_COEFFICIENT.name]!!,
                            isDefault = rocketParameters?.isDefault ?: false
                        )
                        if (rocketParameters == null) viewModel.saveRocketConfig(updated)
                        else viewModel.updateRocketConfig(updated)
                        onNavigateBack()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = WarmOrange,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    )
                ) {
                    Text("Save Rocket Configuration")
                }

                Spacer(Modifier.height(16.dp))
            }
        }
    }
}
