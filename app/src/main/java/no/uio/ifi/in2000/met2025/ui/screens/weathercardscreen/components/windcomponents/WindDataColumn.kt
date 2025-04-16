package no.uio.ifi.in2000.met2025.ui.screens.weathercardscreen.components.windcomponents

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import no.uio.ifi.in2000.met2025.data.local.database.ConfigProfile
import no.uio.ifi.in2000.met2025.data.models.ConfigParameter
import no.uio.ifi.in2000.met2025.data.models.IsobaricData
import no.uio.ifi.in2000.met2025.domain.helpers.floorModDouble
import no.uio.ifi.in2000.met2025.domain.helpers.roundToDecimals
import no.uio.ifi.in2000.met2025.domain.helpers.windShearDirection
import no.uio.ifi.in2000.met2025.domain.helpers.windShearSpeed

@Composable
fun WindDataColumn(isobaricData: IsobaricData, config: ConfigProfile, windShearColor: Color) {
    var expanded by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp)
            .clickable { expanded = !expanded }
    ) {
        Column {
            Icon(
                imageVector = Icons.Default.ExpandMore,
                contentDescription = "Expand",
                tint = Color.Gray,
                modifier = Modifier
                    .fillMaxWidth()
                    .graphicsLayer(rotationZ = if (expanded) 0f else 180f)
            )

            val pressureValues = isobaricData.valuesAtLayer.keys.sorted()
            val displayedValues = if (expanded) pressureValues else pressureValues.takeLast(6)
            displayedValues.forEachIndexed { index, layer ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, Color.Black)
                        .padding(4.dp)
                ) {
                    WindLayerRow(
                        config = config,
                        configParameter = ConfigParameter.AIR_WIND,
                        altitude = isobaricData.valuesAtLayer[layer]?.altitude,
                        windSpeed = isobaricData.valuesAtLayer[layer]?.windSpeed,
                        windDirection = isobaricData.valuesAtLayer[layer]?.windFromDirection,
                        modifier = Modifier.fillMaxWidth(),
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                    )
                }

                if (index < displayedValues.size - 1) {
                    val nextLayer = displayedValues[index + 1]
                    val currentData = isobaricData.valuesAtLayer[layer]
                    val nextData = isobaricData.valuesAtLayer[nextLayer]

                    if (currentData != null && nextData != null) {
                        val windShearValue = windShearSpeed(
                            currentData,
                            nextData
                        ).roundToDecimals(1)
                        val windShearDirection = windShearDirection(
                            currentData,
                            nextData
                        ).floorModDouble(360).roundToDecimals(1)

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .border(1.dp, Color.Black)
                                .padding(horizontal = 4.dp, vertical = 4.dp)
                        ) {
                            WindLayerRow(
                                config = config,
                                configParameter = ConfigParameter.WIND_SHEAR_SPEED,
                                altitude = null,
                                windSpeed = windShearValue,
                                windDirection = windShearDirection,
                                modifier = Modifier.fillMaxWidth(),
                                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                            )
                        }
                    }
                }
            }
        }
    }
}
