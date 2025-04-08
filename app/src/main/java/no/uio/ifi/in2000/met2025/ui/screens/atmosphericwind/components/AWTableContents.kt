package no.uio.ifi.in2000.met2025.ui.screens.atmosphericwind.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
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
import no.uio.ifi.in2000.met2025.data.models.IsobaricData
import no.uio.ifi.in2000.met2025.domain.helpers.formatZuluTimeToLocal


@Composable
fun AWTableContents(
    item: IsobaricData,
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

                AnimatedVisibility(visible = showTime) {
                    AWTimeDisplay(
                        formatZuluTimeToLocal(item.time),
                        style = MaterialTheme.typography.titleLarge
                    )
                }

                AnimatedVisibility(visible = expanded) {

                    Column {
                        HorizontalDivider(thickness = 1.dp, color = Color.Gray)

                        // Static header row to label columns
                        WindLayerRow(
                            altitudeText = "Altitude",
                            windSpeedText = "Wind Speed",
                            windDirectionText = "Wind Direction",
                            style = MaterialTheme.typography.titleSmall
                        )

                        // Static header row to label columns
                        WindShearRow(
                            backgroundColor = windShearColor,
                            speedText = "Wind Shear Speed",
                            directionText = "Wind Shear Direction",
                            style = MaterialTheme.typography.titleSmall
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        WindDataColumn(item, windShearColor)
                    }
                }
            }
        }
    }
}