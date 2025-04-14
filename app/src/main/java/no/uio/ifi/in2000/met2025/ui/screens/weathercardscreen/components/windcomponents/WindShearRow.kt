package no.uio.ifi.in2000.met2025.ui.screens.weathercardscreen.components.windcomponents

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import no.uio.ifi.in2000.met2025.data.local.database.ConfigProfile
import no.uio.ifi.in2000.met2025.domain.helpers.floorModDouble
import no.uio.ifi.in2000.met2025.domain.helpers.roundToDecimals

@Composable
fun WindShearRow(
    config: ConfigProfile,
    backgroundColor: Color,
    windSpeed: Double?,
    windDirection: Double?,
    style: androidx.compose.ui.text.TextStyle
) {
    val windSpeedText = (windSpeed
        ?.roundToDecimals(1) ?: "--")
        .toString()
    val windDirectionText = (windDirection
        ?.floorModDouble(360)
        ?.roundToDecimals(1) ?: "--")
        .toString()

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .background(
                color = backgroundColor,
                shape = RoundedCornerShape(4.dp)
            )
            .padding(4.dp) // Add border and padding for visual offset
    ) {
        Box(modifier = Modifier.weight(1f))

        Text(
            text = windSpeedText,
            style = style,
            modifier = Modifier.weight(1f)
        )

        Text(
            text = windDirectionText,
            style = style,
            modifier = Modifier.weight(1f)
        )
    }
}