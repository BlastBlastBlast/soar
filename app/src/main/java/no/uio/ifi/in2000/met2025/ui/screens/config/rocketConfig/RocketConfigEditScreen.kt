package no.uio.ifi.in2000.met2025.ui.screens.config.rocketConfig

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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import no.uio.ifi.in2000.met2025.data.models.getDefaultRocketParameterValues
import no.uio.ifi.in2000.met2025.data.local.database.RocketConfig
import no.uio.ifi.in2000.met2025.data.models.RocketParameterType
import no.uio.ifi.in2000.met2025.ui.common.AppOutlinedNumberField
import no.uio.ifi.in2000.met2025.ui.common.AppOutlinedTextField
import no.uio.ifi.in2000.met2025.ui.screens.config.ConfigViewModel
import no.uio.ifi.in2000.met2025.ui.theme.WarmOrange

/**
 * RocketConfigEditScreen
 *
 * Screen for creating or editing a RocketConfig. Displays input fields for
 * each rocket parameter, validates the name against existing configs, and
 * saves or updates the entry via the ViewModel.
 *
 * Special notes:
 * - Uses getDefaultRocketParameterValues() to prefill fields when adding a new config.
 * - Validates uniqueness of the name and non-emptiness.
 * - Announces name errors via liveRegion semantics for accessibility.
 */
@Composable
fun RocketConfigEditScreen(
    rocketParameters: RocketConfig? = null,
    viewModel: ConfigViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit
) {

    val updateStatus by viewModel.rocketUpdateStatus.collectAsState()
    val rocketNames by viewModel.rocketNames.collectAsState()

    val defaultsMap = getDefaultRocketParameterValues().valueMap

    var name by remember(rocketParameters) { mutableStateOf(rocketParameters?.name ?: "New Config") }
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
                .padding(16.dp)
                .semantics {
                    // announce whether we're creating or editing
                    contentDescription =
                        if (rocketParameters == null)
                            "New Rocket Configuration Screen"
                        else
                            "Edit Rocket Configuration Screen"
                },
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 4.dp,
            shadowElevation = 8.dp,
            shape = RoundedCornerShape(12.dp)
        ) {

            val isNameError = name in rocketNames && name != rocketParameters?.name
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
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                        .semantics { heading() },
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

                Spacer(Modifier.height(16.dp))

                Column(Modifier.padding(horizontal = 16.dp)) {
                    AppOutlinedTextField(
                        value = name,
                        onValueChange = {
                            name = it
                        },
                        labelText = "Name",
                        modifier = Modifier.fillMaxWidth(),
                        //Regex to limit name to 14 characters
                        filterRegex = Regex("^.{0,14}\$")
                    )
                    if (isNameError) {
                        Text(
                            text = "Config name already exists",
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                    Spacer(Modifier.height(8.dp))

                    AppOutlinedNumberField(
                        value        = launchAzimuth,
                        onValueChange= { launchAzimuth = it },
                        labelText    = "Launch Azimuth (°)",
                        modifier     = Modifier.fillMaxWidth()
                    )
                    Spacer(Modifier.height(8.dp))

                    AppOutlinedNumberField(
                        value        = launchPitch,
                        onValueChange= { launchPitch = it },
                        labelText    = "Launch Pitch (°)",
                        modifier     = Modifier.fillMaxWidth()
                    )
                    Spacer(Modifier.height(8.dp))

                    AppOutlinedNumberField(
                        value        = launchRailLength,
                        onValueChange= { launchRailLength = it },
                        labelText    = "Launch Rail Length (m)",
                        modifier     = Modifier.fillMaxWidth()
                    )
                    Spacer(Modifier.height(8.dp))

                    AppOutlinedNumberField(
                        value        = wetMass,
                        onValueChange= { wetMass = it },
                        labelText    = "Wet Mass (kg)",
                        modifier     = Modifier.fillMaxWidth()
                    )
                    Spacer(Modifier.height(8.dp))

                    AppOutlinedNumberField(
                        value        = dryMass,
                        onValueChange= { dryMass = it },
                        labelText    = "Dry Mass (kg)",
                        modifier     = Modifier.fillMaxWidth()
                    )
                    Spacer(Modifier.height(8.dp))

                    AppOutlinedNumberField(
                        value        = burnTime,
                        onValueChange= { burnTime = it },
                        labelText    = "Burn Time (s)",
                        modifier     = Modifier.fillMaxWidth()
                    )
                    Spacer(Modifier.height(8.dp))

                    AppOutlinedNumberField(
                        value        = thrust,
                        onValueChange= { thrust = it },
                        labelText    = "Thrust (N)",
                        modifier     = Modifier.fillMaxWidth()
                    )
                    Spacer(Modifier.height(8.dp))

                    AppOutlinedNumberField(
                        value        = stepSize,
                        onValueChange= { stepSize = it },
                        labelText    = "Step Size (s)",
                        modifier     = Modifier.fillMaxWidth()
                    )
                    Spacer(Modifier.height(8.dp))

                    AppOutlinedNumberField(
                        value        = crossSectionalArea,
                        onValueChange= { crossSectionalArea = it },
                        labelText    = "Cross-Sectional Area (m²)",
                        modifier     = Modifier.fillMaxWidth()
                    )
                    Spacer(Modifier.height(8.dp))

                    AppOutlinedNumberField(
                        value        = dragCoefficient,
                        onValueChange= { dragCoefficient = it },
                        labelText    = "Drag Coefficient",
                        modifier     = Modifier.fillMaxWidth()
                    )
                    Spacer(Modifier.height(8.dp))

                    AppOutlinedNumberField(
                        value        = parachuteCrossSectionalArea,
                        onValueChange= { parachuteCrossSectionalArea = it },
                        labelText    = "Parachute Cross-Sectional Area (m²)",
                        modifier     = Modifier.fillMaxWidth()
                    )
                    Spacer(Modifier.height(8.dp))

                    AppOutlinedNumberField(
                        value        = parachuteDragCoefficient,
                        onValueChange= { parachuteDragCoefficient = it },
                        labelText    = "Parachute Drag Coefficient",
                        modifier     = Modifier.fillMaxWidth()
                    )
                }

                Spacer(Modifier.height(24.dp))

                Button(
                    onClick = {
                        val updated = RocketConfig(
                            id = rocketParameters?.id ?: 0,
                            name = name,
                            launchAzimuth = launchAzimuth.toDoubleOrNull()
                                ?: defaultsMap[RocketParameterType.LAUNCH_AZIMUTH.name]!!,
                            launchPitch = launchPitch.toDoubleOrNull()
                                ?: defaultsMap[RocketParameterType.LAUNCH_PITCH.name]!!,
                            launchRailLength = launchRailLength.toDoubleOrNull()
                                ?: defaultsMap[RocketParameterType.LAUNCH_RAIL_LENGTH.name]!!,
                            wetMass = wetMass.toDoubleOrNull()
                                ?: defaultsMap[RocketParameterType.WET_MASS.name]!!,
                            dryMass = dryMass.toDoubleOrNull()
                                ?: defaultsMap[RocketParameterType.DRY_MASS.name]!!,
                            burnTime = burnTime.toDoubleOrNull()
                                ?: defaultsMap[RocketParameterType.BURN_TIME.name]!!,
                            thrust = thrust.toDoubleOrNull()
                                ?: defaultsMap[RocketParameterType.THRUST.name]!!,
                            stepSize = stepSize.toDoubleOrNull()
                                ?: defaultsMap[RocketParameterType.STEP_SIZE.name]!!,
                            crossSectionalArea = crossSectionalArea.toDoubleOrNull()
                                ?: defaultsMap[RocketParameterType.CROSS_SECTIONAL_AREA.name]!!,
                            dragCoefficient = dragCoefficient.toDoubleOrNull()
                                ?: defaultsMap[RocketParameterType.DRAG_COEFFICIENT.name]!!,
                            parachuteCrossSectionalArea = parachuteCrossSectionalArea.toDoubleOrNull()
                                ?: defaultsMap[RocketParameterType.PARACHUTE_CROSS_SECTIONAL_AREA.name]!!,
                            parachuteDragCoefficient = parachuteDragCoefficient.toDoubleOrNull()
                                ?: defaultsMap[RocketParameterType.PARACHUTE_DRAG_COEFFICIENT.name]!!,
                            isDefault = rocketParameters?.isDefault ?: false
                        )
                        if (rocketParameters == null) viewModel.saveRocketConfig(updated)
                        else viewModel.updateRocketConfig(updated)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .semantics {
                            role = Role.Button
                            contentDescription = "Save rocket configuration"
                        }
                        .padding(horizontal = 16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = WarmOrange,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    ),
                    enabled = !isNameError && name.isNotBlank()
                ) {
                    Text("Save Rocket Configuration")
                }
                if (isNameError) {
                    Text(
                        text = "Config name \"$name\" already exists",
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier
                            .padding(top = 4.dp)
                            .semantics {
                                liveRegion = LiveRegionMode.Polite
                                contentDescription = "Config name $name already exists"
                            }
                    )
                }
                if (name.isBlank()) {
                    Text(
                        text = "Configuration Name field must not be empty",
                        color = Color.Red,
                        modifier = Modifier.semantics {
                            liveRegion = LiveRegionMode.Polite
                            contentDescription = "Configuration Name field must not be empty"
                        }
                    )
                }

                Spacer(Modifier.height(16.dp))
            }
        }
    }


    //navigate back on success
    LaunchedEffect(updateStatus) {
        if (updateStatus is ConfigViewModel.UpdateStatus.Success) {
            viewModel.resetRocketStatus()
            onNavigateBack()
        }
    }
}
