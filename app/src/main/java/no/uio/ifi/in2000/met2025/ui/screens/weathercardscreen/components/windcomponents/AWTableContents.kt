package no.uio.ifi.in2000.met2025.ui.screens.weathercardscreen.components.windcomponents

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import no.uio.ifi.in2000.met2025.data.local.database.ConfigProfile
import no.uio.ifi.in2000.met2025.data.models.ConfigParameter
import no.uio.ifi.in2000.met2025.data.models.IsobaricData
import no.uio.ifi.in2000.met2025.data.models.safetyevaluation.LaunchStatusIcon
import no.uio.ifi.in2000.met2025.data.models.safetyevaluation.evaluateLaunchConditions
import no.uio.ifi.in2000.met2025.data.models.safetyevaluation.evaluateParameterCondition
import no.uio.ifi.in2000.met2025.domain.helpers.formatZuluTimeToLocal
import java.time.Instant
import java.time.ZoneId
import java.time.temporal.ChronoUnit
import androidx.compose.foundation.layout.size


@Composable
fun AWTableContents(
    item: IsobaricData,
    config: ConfigProfile,
    showTime: Boolean = true,
) {
    val cardBackgroundColor = Color(0xFFE3F2FD)
    val windShearColor = Color(0xFFe2e0ff)

    var expanded by remember { mutableStateOf(true) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .clickable { expanded = !expanded },
        colors = CardDefaults.cardColors(containerColor = cardBackgroundColor),
        shape = RoundedCornerShape(corner = CornerSize(8.dp))
    ) {
        //FIXME: Add global darkmode support later
        CompositionLocalProvider(LocalContentColor provides Color.DarkGray) {
            Column(modifier = Modifier.padding(16.dp)) {

                Row {
                    AnimatedVisibility(visible = showTime, modifier = Modifier.weight(1f)) {
                        AWTimeDisplay(
                            time = formatZuluTimeToLocal(item.time) + " - " + formatZuluTimeToLocal(Instant.parse(item.time).plus(2, ChronoUnit.HOURS).plus(59, ChronoUnit.MINUTES).toString()),
                            style = MaterialTheme.typography.titleLarge
                        )
                    }
                    LaunchStatusIcon(evaluateLaunchConditions(item, config), modifier = Modifier.size(24.dp))
                }


                AnimatedVisibility(visible = expanded) {

                    Column {
                        HorizontalDivider(thickness = 1.dp, color = Color.Gray)

                        // Static header row to label columns
                        WindLayerHeader(
                            altitudeText = "Altitude",
                            windSpeedText = "Wind Speed",
                            windDirectionText = "Wind Direction",
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            style = MaterialTheme.typography.titleSmall
                        )

                        // Static header row to label columns
                        WindLayerHeader(
                            altitudeText = "",
                            windSpeedText = "Wind Shear Speed",
                            windDirectionText = "Wind Shear Direction",
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .background(
                                    color = windShearColor,
                                    shape = RoundedCornerShape(4.dp)
                                )
                                .padding(top = 4.dp, bottom = 4.dp), // Add border and padding for visual offset
                            style = MaterialTheme.typography.titleSmall
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        WindDataColumn(item, config, windShearColor)
                    }
                }
            }
        }
    }
}