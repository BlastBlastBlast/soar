package no.uio.ifi.in2000.met2025.ui.screens.weatherScreen.components.windcomponents

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import no.uio.ifi.in2000.met2025.data.local.database.WeatherConfig
import no.uio.ifi.in2000.met2025.data.models.isobaric.IsobaricData
import no.uio.ifi.in2000.met2025.data.models.safetyevaluation.LaunchStatusIcon
import no.uio.ifi.in2000.met2025.data.models.safetyevaluation.evaluateConditions
import no.uio.ifi.in2000.met2025.domain.helpers.formatZuluTimeToLocal
import java.time.Instant
import java.time.temporal.ChronoUnit
import androidx.compose.foundation.layout.size
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight

/**
 * This composable displays a card with wind layer data (isobaric) for a given forecast item.
 * It includes launch status icon, optional time display, and expandable table view
 * showing altitude, wind speed/direction, and wind shear layers.
 */

@Composable
fun AWTableContents(
    item: IsobaricData,
    config: WeatherConfig,
    showTime: Boolean = true,
) {
    val cardBackgroundColor = MaterialTheme.colorScheme.primary
    val windShearColor = MaterialTheme.colorScheme.primary

    var expanded by remember { mutableStateOf(true) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .clickable { expanded = !expanded }
            .semantics {
                role = Role.Button
                contentDescription = if (expanded) "Collapse wind data table"
                else "Expand wind data table"
            },
        colors = CardDefaults.cardColors(containerColor = cardBackgroundColor),
        shape = RoundedCornerShape(corner = CornerSize(8.dp))
    ) {
        CompositionLocalProvider(LocalContentColor provides MaterialTheme.colorScheme.onPrimary) {
            Column(modifier = Modifier.padding(0.dp)) {

                Row {
                    AnimatedVisibility(visible = showTime, modifier = Modifier.weight(1f)) {
                        AWTimeDisplay(
                            time = formatZuluTimeToLocal(item.time) + " - " + formatZuluTimeToLocal(Instant.parse(item.time).plus(2, ChronoUnit.HOURS).plus(59, ChronoUnit.MINUTES).toString()),
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                            // semantics in class file
                        )
                    }
                    LaunchStatusIcon(evaluateConditions(item, config), modifier = Modifier.size(24.dp).semantics {
                        role = Role.Image
                        contentDescription = "Launch status: ${evaluateConditions(item, config)}" }
                    )
                }

                // Expandable wind data table
                AnimatedVisibility(visible = expanded, modifier = Modifier.semantics { liveRegion = LiveRegionMode.Polite }
                ) {

                    Column {
                        HorizontalDivider(thickness = 1.dp, color = MaterialTheme.colorScheme.onPrimary)

                        // Static header row to label columns
                        WindLayerHeader(
                            altitudeText = "<Altitude>",
                            windSpeedText = "<Wind Speed>",
                            windDirectionText = "<Wind Direction>",
                            modifier = Modifier
                                .fillMaxWidth()
                                .semantics { heading() }
                                .border(1.dp, MaterialTheme.colorScheme.onPrimary)
                                .padding(vertical = 4.dp),
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                        )


                        // Static header row to label columns
                        WindLayerHeader(
                            altitudeText = " Wind Shear",
                            windSpeedText = "<Wind Shear Speed>",
                            windDirectionText = "<Wind Shear Direction>",
                            modifier = Modifier
                                .fillMaxWidth()
                                .semantics { contentDescription = "Shear data header row" }
                                .padding(vertical = 4.dp)
                                .border(1.dp, MaterialTheme.colorScheme.onPrimary)
                                .background(
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f),
                                    shape = RoundedCornerShape(0.dp)
                                )
                                .padding(top = 4.dp, bottom = 4.dp), // Add border and padding for visual offset
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        WindDataColumn(item, config, windShearColor)
                    }
                }
            }
        }
    }
}