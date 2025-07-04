package no.uio.ifi.in2000.met2025.ui.screens.weatherScreen.components.windcomponents

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import no.uio.ifi.in2000.met2025.data.local.database.WeatherConfig
import no.uio.ifi.in2000.met2025.data.models.ConfigParameter
import no.uio.ifi.in2000.met2025.data.models.safetyevaluation.LaunchStatusIcon
import no.uio.ifi.in2000.met2025.data.models.safetyevaluation.evaluateConditions
import no.uio.ifi.in2000.met2025.domain.helpers.floorModDouble
import no.uio.ifi.in2000.met2025.domain.helpers.roundToDecimals
import no.uio.ifi.in2000.met2025.domain.helpers.unit
import no.uio.ifi.in2000.met2025.ui.screens.weatherScreen.components.WindDirectionIcon
import androidx.compose.foundation.layout.size
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle

/**
 * Displays a row representing wind shear data including direction, speed, and safety status
 */
@Composable
fun WindShearRow(
    config: WeatherConfig,
    configParameter: ConfigParameter,
    name: String = "Shear",
    windSpeed: Double?,
    windDirection: Double?,
    modifier: Modifier,
    style: TextStyle
) {

    val windSpeedText = (windSpeed
        ?.roundToDecimals(1) ?: "--")
        .toString()

    val windDirectionText = (windDirection
        ?.floorModDouble(360)
        ?.roundToDecimals(1) ?: "--")
        .toString()

    Row(
        modifier = modifier
            .semantics {
            contentDescription = buildString {
                append(name)
                append("Wind direction ${windDirectionText}. ")
                append("Wind speed ${windSpeedText}. ")
            }
        }
    ) {
        Text(
            text = name,
            style = style,
            modifier = Modifier.weight(1f)
        )

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
            LaunchStatusIcon(evaluateConditions(config, ConfigParameter.AIR_WIND, windSpeed), modifier = Modifier.size(24.dp))
        }
    }
}