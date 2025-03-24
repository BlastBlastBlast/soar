package no.uio.ifi.in2000.met2025.ui.screens.weathercardscreen

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import no.uio.ifi.in2000.met2025.ui.components.HourlyExpandableCard

@Preview(showSystemUi = true, showBackground = true)
@Composable
fun WeatherCardPreview() {
    val sampleForecastDisplayData = WeatherCardViewmodel.ForecastDisplayData(
        temperatures = mapOf(
            "08:00" to 10.0,
            "09:00" to 11.0,
            "10:00" to 12.0,
            "11:00" to 13.0
        ),
        humidities = mapOf(
            "08:00" to 80.0,
            "09:00" to 78.0,
            "10:00" to 75.0,
            "11:00" to 70.0
        ),
        windSpeeds = mapOf(
            "08:00" to 3.0,
            "09:00" to 3.5,
            "10:00" to 4.0,
            "11:00" to 4.5
        ),
        windGusts = mapOf(
            "08:00" to 5.0,
            "09:00" to 5.5,
            "10:00" to 6.0,
            "11:00" to 6.5
        ),
        windDirections = mapOf(
            "08:00" to 180.0,
            "09:00" to 190.0,
            "10:00" to 200.0,
            "11:00" to 210.0
        ),
        precipitations = mapOf(
            "08:00" to 0.0,
            "09:00" to 0.1,
            "10:00" to 0.0,
            "11:00" to 0.2
        ),
        visibilities = mapOf(
            "08:00" to 100.0,
            "09:00" to 98.0,
            "10:00" to 95.0,
            "11:00" to 92.0
        ),
        dewPoints = mapOf(
            "08:00" to 5.0,
            "09:00" to 6.0,
            "10:00" to 7.0,
            "11:00" to 8.0
        ),
        cloudCovers = mapOf(
            "08:00" to 20.0,
            "09:00" to 30.0,
            "10:00" to 50.0,
            "11:00" to 60.0
        ),
        thunderProbabilities = mapOf(
            "08:00" to 0.0,
            "09:00" to 10.0,
            "10:00" to 20.0,
            "11:00" to 30.0
        )
    )

    Column(modifier = Modifier.padding(16.dp)) {
        sampleForecastDisplayData.temperatures.keys.sorted().forEach { hour ->
            HourlyExpandableCard(
                displayData = sampleForecastDisplayData,
                hour = hour,
                modifier = Modifier.padding(vertical = 8.dp)
            )
        }
    }
}
