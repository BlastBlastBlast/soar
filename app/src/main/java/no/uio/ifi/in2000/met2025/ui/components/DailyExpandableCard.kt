package no.uio.ifi.in2000.met2025.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import no.uio.ifi.in2000.met2025.data.models.ForecastDataItem
import no.uio.ifi.in2000.met2025.data.models.LaunchStatus
import no.uio.ifi.in2000.met2025.data.models.LaunchStatusIcon
import no.uio.ifi.in2000.met2025.data.models.evaluateLaunchConditions
import no.uio.ifi.in2000.met2025.data.models.evaluateParameterConditions


fun evaluateDailyLaunchStatus(items: List<ForecastDataItem>): LaunchStatus {
    var hasUnsafe = false
    var hasCaution = false

    for (item in items) {
        when (evaluateLaunchConditions(item)) {
            LaunchStatus.UNSAFE -> hasUnsafe = true
            LaunchStatus.CAUTION -> hasCaution = true
            else -> {}
        }
    }

    return when {
        hasUnsafe -> LaunchStatus.UNSAFE
        hasCaution -> LaunchStatus.CAUTION
        else -> LaunchStatus.SAFE
    }
}

@Composable
fun DailyForecastCard(
    forecastItems: List<ForecastDataItem>,
    modifier: Modifier = Modifier
) {
    val day = formatZuluTimeToLocalDate(forecastItems.first().time)
    val overallStatus = evaluateDailyLaunchStatus(forecastItems)

    // Bruk første time for å vise verdier fikser logikk for gjennomsnitt senere
    val avgFog = forecastItems.map { it.values.fogAreaFraction }.average()
    val totalPrecipitation = forecastItems.sumOf { it.values.precipitationAmount }
    val maxDewPoint = forecastItems.maxOf { it.values.dewPointTemperature }
    val maxHumidity = forecastItems.maxOf { it.values.relativeHumidity }
    val maxAirWind = forecastItems.maxOf { it.values.windSpeedOfGust }
    val minAirWind = forecastItems.minOf { it.values.windSpeedOfGust }
    val maxGroundWind = forecastItems.maxOf { it.values.windSpeed }
    val minGroundWind = forecastItems.minOf { it.values.windSpeed }
    val avgWindDirection = forecastItems.map { it.values.windFromDirection }.average()
    val avgCloudCover = forecastItems.map { it.values.cloudAreaFraction }.average()

    var expanded by remember { mutableStateOf(false) }



    Card(
        modifier = modifier
            .clickable { expanded = !expanded },
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Day: $day", style = MaterialTheme.typography.headlineSmall)
            LaunchStatusIcon(status = overallStatus)

            Spacer(modifier = Modifier.padding(top = 8.dp))

            Text(
                "☁️ Avg. Cloud Cover: ${"%.1f".format(avgCloudCover)}%",
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                "🌧️ Total Precipitation: ${"%.1f".format(totalPrecipitation)} mm",
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                "🌫️ Avg. Fog: ${"%.1f".format(avgFog)}%",
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                "💧 Max Humidity: ${"%.1f".format(maxHumidity)}%",
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                "🌡️ Max Dew Point: ${"%.1f".format(maxDewPoint)}°C",
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                "💨 Air Wind Gust: ${"%.1f".format(minAirWind)} - ${"%.1f".format(maxAirWind)} m/s",
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                "🌬️ Ground Wind: ${"%.1f".format(minGroundWind)} - ${"%.1f".format(maxGroundWind)} m/s",
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                "🧭 Avg. Wind Direction: ${"%.1f".format(avgWindDirection)}°",
                style = MaterialTheme.typography.bodyMedium
            )

            AnimatedVisibility(visible = expanded) {
                Column(modifier = Modifier.padding(top = 8.dp)) {
                    forecastItems.forEach {
                        Text("${formatZuluTimeToLocalTime(it.time)}: ${it.values.airTemperature}°C")
                    }
                }
            }
        }
    }

    @Composable
    fun DailyLazyRow(allForecastItems: List<ForecastDataItem>) {
        val dailyForecasts = allForecastItems
            .groupBy { formatZuluTimeToLocalDate(it.time) }
            .values
            .toList()
            .take(3) // bare de tre første dagene

        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
        ) {
            items(dailyForecasts) { dayForecast ->
                DailyForecastCard(
                    forecastItems = dayForecast,
                    modifier = Modifier.padding(end = 8.dp)
                )
            }
        }
    }

    @Composable
    fun DailyForecastRowSection(forecastItems: List<ForecastDataItem>) {
        Column {
            Text(
                text = "72 Hour Forecast",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
            DailyLazyRow(allForecastItems = forecastItems)
        }
    }
}