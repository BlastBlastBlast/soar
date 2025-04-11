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
import androidx.compose.ui.unit.dp
import no.uio.ifi.in2000.met2025.data.models.IsobaricData
import no.uio.ifi.in2000.met2025.domain.helpers.floorModDouble
import no.uio.ifi.in2000.met2025.domain.helpers.roundToDecimals
import no.uio.ifi.in2000.met2025.domain.helpers.windShearDirection
import no.uio.ifi.in2000.met2025.domain.helpers.windShearSpeed

@Composable
fun WindDataColumn(isobaricData: IsobaricData, windShearColor: Color) {
    var expanded by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, Color.Gray, shape = CustomRoundedCornerShape(8.dp))
            .padding(top = 8.dp/*, bottom = 8.dp*/)
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

            // Iterate through layer pressure values and display data
            val pressureValues = isobaricData.valuesAtLayer.keys.sorted()
            val displayedValues = if (expanded) pressureValues else pressureValues.takeLast(6)
            displayedValues.forEachIndexed { index, layer ->
                val altitude = isobaricData.valuesAtLayer[layer]?.altitude?.toInt() ?: "--"
                val windSpeed = isobaricData.valuesAtLayer[layer]?.windSpeed
                    ?.roundToDecimals(1) ?: "--"
                val windDirection = isobaricData.valuesAtLayer[layer]?.windFromDirection
                    ?.floorModDouble(360)
                    ?.roundToDecimals(1) ?: "--"

                WindLayerRow(
                    altitudeText = "$altitude m",
                    windSpeedText = "$windSpeed m/s",
                    windDirectionText = "$windDirection°",
                    style = MaterialTheme.typography.bodyMedium
                )

                // Calculate wind shear if not the last item in list and both layers exist
                if (index < displayedValues.size - 1) {
                    val nextLayer = displayedValues[index + 1]

                    if (isobaricData.valuesAtLayer[layer] != null && isobaricData.valuesAtLayer[nextLayer] != null) {
                        val windShearValue = windShearSpeed(
                            isobaricData.valuesAtLayer[layer]!!,
                            isobaricData.valuesAtLayer[nextLayer]!!
                        )
                            .roundToDecimals(1)

                        val windShearDirection = windShearDirection(
                            isobaricData.valuesAtLayer[layer]!!,
                            isobaricData.valuesAtLayer[nextLayer]!!
                        )
                            .floorModDouble(360).roundToDecimals(1)

                        WindShearRow(
                            backgroundColor = windShearColor,
                            speedText = "$windShearValue m/s",
                            directionText = "$windShearDirection°",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
        }
    }
}