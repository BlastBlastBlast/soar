package no.uio.ifi.in2000.met2025.ui.screens.weathercardscreen.components.windcomponents

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import no.uio.ifi.in2000.met2025.data.local.database.ConfigProfile
import no.uio.ifi.in2000.met2025.data.models.ConfigParameter
import no.uio.ifi.in2000.met2025.data.models.safetyevaluation.LaunchStatusIcon
import no.uio.ifi.in2000.met2025.data.models.safetyevaluation.evaluateParameterCondition
import no.uio.ifi.in2000.met2025.domain.helpers.floorModDouble
import no.uio.ifi.in2000.met2025.domain.helpers.roundToDecimals
import no.uio.ifi.in2000.met2025.domain.helpers.unit
import no.uio.ifi.in2000.met2025.ui.screens.weathercardscreen.components.WindDirectionIcon
import androidx.compose.foundation.layout.size

@Composable
fun WindLayerRow(
    config: ConfigProfile,
    configParameter: ConfigParameter,
    altitude: Double?,
    windSpeed: Double?,
    windDirection: Double?,
    modifier: Modifier,
    style: androidx.compose.ui.text.TextStyle
) {
    val altitudeText = (altitude
        ?.roundToDecimals(-2)
        ?.toInt() ?: "--")
        .toString()

    val windSpeedText = (windSpeed
        ?.roundToDecimals(1) ?: "--")
        .toString()

    val windDirectionText = (windDirection
        ?.floorModDouble(360)
        ?.roundToDecimals(1) ?: "--")
        .toString()

    Row(modifier = modifier) {
        if (altitude == null) {
            Box(modifier = Modifier.weight(1f))
        } else {
            Text(
                text = altitudeText + ConfigParameter.ALTITUDE_UPPER_BOUND.unit(),
                style = style,
                modifier = Modifier.weight(1f)
            )
        }

        Row(
            modifier = Modifier.weight(1f),
            horizontalArrangement = Arrangement.spacedBy(0.dp)
        ) {
            WindDirectionIcon(windDirection = windDirection)

            Text(
                text = windDirectionText + ConfigParameter.WIND_DIRECTION.unit(),
                style = style,
                modifier = Modifier.weight(1f)
            )
        }
        Row(
            modifier = Modifier.weight(1f),
            horizontalArrangement = Arrangement.spacedBy(0.dp)
        ) {
            Text(
                text = windSpeedText + " " + configParameter.unit(),
                style = style,
                modifier = Modifier.weight(1f)
            )
            LaunchStatusIcon(evaluateParameterCondition(windSpeed, config, ConfigParameter.AIR_WIND), modifier = Modifier.size(24.dp))
        }
    }
}