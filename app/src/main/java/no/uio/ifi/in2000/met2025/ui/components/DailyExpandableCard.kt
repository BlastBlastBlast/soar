package no.uio.ifi.in2000.met2025.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import no.uio.ifi.in2000.met2025.data.models.*

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

    val avgTemperature = forecastItems.map { it.values.airTemperature }.average()
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

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                "🌡️ Avg. Temperature: ${"%.1f".format(avgTemperature)}°C",
                style = MaterialTheme.typography.bodyMedium
            )

            AnimatedVisibility(visible = expanded) {
                Column(modifier = Modifier.padding(top = 8.dp)) {
                    Text("☁️ Avg. Cloud Cover: ${"%.1f".format(avgCloudCover)}%", style = MaterialTheme.typography.bodyMedium)
                    Text("🌧️ Total Precipitation: ${"%.1f".format(totalPrecipitation)} mm", style = MaterialTheme.typography.bodyMedium)
                    Text("🌫️ Avg. Fog: ${"%.1f".format(avgFog)}%", style = MaterialTheme.typography.bodyMedium)
                    Text("💧 Max Humidity: ${"%.1f".format(maxHumidity)}%", style = MaterialTheme.typography.bodyMedium)
                    Text("🌡️ Max Dew Point: ${"%.1f".format(maxDewPoint)}°C", style = MaterialTheme.typography.bodyMedium)
                    Text("💨 Air Wind Gust: ${"%.1f".format(minAirWind)} - ${"%.1f".format(maxAirWind)} m/s", style = MaterialTheme.typography.bodyMedium)
                    Text("🌬️ Ground Wind: ${"%.1f".format(minGroundWind)} - ${"%.1f".format(maxGroundWind)} m/s", style = MaterialTheme.typography.bodyMedium)
                    Text("🧭 Avg. Wind Direction: ${"%.1f".format(avgWindDirection)}°", style = MaterialTheme.typography.bodyMedium)

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
        .take(3) // Vis maks tre dager

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
