package no.uio.ifi.in2000.met2025.ui.screens.weathercardscreen

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import no.uio.ifi.in2000.met2025.data.models.ForecastDataItem
import no.uio.ifi.in2000.met2025.data.models.ForecastDataValues
import no.uio.ifi.in2000.met2025.ui.components.HourlyExpandableCard

@Preview(showSystemUi = true, showBackground = true)
@Composable
fun WeatherCardPreview() {
    val sampleForecastItems = listOf(
        ForecastDataItem(
            time = "08:00",
            values = ForecastDataValues(
                airTemperature = 10.0,
                relativeHumidity = 80.0,
                windSpeed = 3.0,
                windSpeedOfGust = 5.0,
                windFromDirection = 180.0,
                fogAreaFraction = 100.0, // used as visibility
                dewPointTemperature = 5.0,
                cloudAreaFraction = 20.0,
                precipitationAmount = 0.0,
                probabilityOfThunder = 0.0
            )
        ),
        ForecastDataItem(
            time = "09:00",
            values = ForecastDataValues(
                airTemperature = 11.0,
                relativeHumidity = 78.0,
                windSpeed = 3.5,
                windSpeedOfGust = 5.5,
                windFromDirection = 190.0,
                fogAreaFraction = 98.0,
                dewPointTemperature = 6.0,
                cloudAreaFraction = 30.0,
                precipitationAmount = 0.1,
                probabilityOfThunder = 10.0
            )
        ),
        ForecastDataItem(
            time = "10:00",
            values = ForecastDataValues(
                airTemperature = 12.0,
                relativeHumidity = 75.0,
                windSpeed = 4.0,
                windSpeedOfGust = 6.0,
                windFromDirection = 200.0,
                fogAreaFraction = 95.0,
                dewPointTemperature = 7.0,
                cloudAreaFraction = 50.0,
                precipitationAmount = 0.0,
                probabilityOfThunder = 20.0
            )
        ),
        ForecastDataItem(
            time = "11:00",
            values = ForecastDataValues(
                airTemperature = 13.0,
                relativeHumidity = 70.0,
                windSpeed = 4.5,
                windSpeedOfGust = 6.5,
                windFromDirection = 210.0,
                fogAreaFraction = 92.0,
                dewPointTemperature = 8.0,
                cloudAreaFraction = 60.0,
                precipitationAmount = 0.2,
                probabilityOfThunder = 30.0
            )
        )
    )

    Column(modifier = Modifier.padding(16.dp)) {
        sampleForecastItems.forEach { forecastItem ->
            HourlyExpandableCard(
                forecastItem = forecastItem,
                modifier = Modifier.padding(vertical = 8.dp)
            )
        }
    }
}
