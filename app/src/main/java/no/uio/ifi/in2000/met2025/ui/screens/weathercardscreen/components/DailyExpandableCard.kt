package no.uio.ifi.in2000.met2025.ui.screens.weathercardscreen.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import no.uio.ifi.in2000.met2025.data.models.locationforecast.ForecastDataItem
import no.uio.ifi.in2000.met2025.domain.helpers.formatZuluTimeToLocalDate
import no.uio.ifi.in2000.met2025.data.models.getWeatherIconRes


/*
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
*/

val weatherPriority = listOf(
    "heavyrain", "heavyrainandthunder", "rainandthunder", "rain",
    "sleet", "snow",
    "cloudy", "partlycloudy_day", "fair_day", "clearsky_day"
)

fun getDominantSymbolCode(items: List<ForecastDataItem>): String? {
    val allCodes = items.mapNotNull { it.values.symbolCode }

    // Return the first symbolCode that appears in priority order
    for (prioritySymbol in weatherPriority) {
        if (allCodes.any { it.contains(prioritySymbol) }) {
            return allCodes.first { it.contains(prioritySymbol) }
        }
    }

    // Fallback
    return allCodes
        .groupingBy { it }
        .eachCount()
        .maxByOrNull { it.value }
        ?.key
}




@Composable
fun DailyForecastCard(
    forecastItems: List<ForecastDataItem>,
    modifier: Modifier = Modifier
) {
    val day = formatZuluTimeToLocalDate(forecastItems.first().time)
    val avgTemperature = forecastItems.map { it.values.airTemperature }.average()
    val avgFog = forecastItems.map { it.values.fogAreaFraction ?: 0.0 }.average()
    val totalPrecipitation = forecastItems.sumOf { it.values.precipitationAmount ?: 0.0 }
    val maxDewPoint = forecastItems.maxOf { it.values.dewPointTemperature ?: 0.0 }
    val maxHumidity = forecastItems.maxOf { it.values.relativeHumidity }
    val maxAirWind = forecastItems.maxOf { it.values.windSpeedOfGust ?: 0.0 }
    val minAirWind = forecastItems.minOf { it.values.windSpeedOfGust ?: 0.0 }
    val maxGroundWind = forecastItems.maxOf { it.values.windSpeed }
    val minGroundWind = forecastItems.minOf { it.values.windSpeed }
    val avgWindDirection = forecastItems.map { it.values.windFromDirection }.average()
    val avgCloudCover = forecastItems.map { it.values.cloudAreaFraction }.average()

    val dominantSymbol = getDominantSymbolCode(forecastItems)
    val iconRes = getWeatherIconRes(dominantSymbol)

    var expanded by remember { mutableStateOf(false) }

    Card(
        modifier = modifier
            .width(250.dp)
            .clickable { expanded = !expanded },
        elevation = CardDefaults.cardElevation(6.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Dag i hjørnet
            Box(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = day,
                    style = MaterialTheme.typography.labelLarge,
                    modifier = Modifier.align(Alignment.TopStart)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))


            iconRes?.let {
                Image(
                    painter = painterResource(id = it),
                    contentDescription = dominantSymbol,
                    modifier = Modifier.size(72.dp)
                )
            }

            Text(
                text = "${avgTemperature.toInt()}°C",
                style = MaterialTheme.typography.headlineLarge
            )


            Spacer(modifier = Modifier.height(8.dp))

            // Expandable section
            AnimatedVisibility(visible = expanded) {
                Column(modifier = Modifier.padding(top = 8.dp)) {
                    Text("☁️ Avg. Cloud Cover: ${"%.1f".format(avgCloudCover)}%", style = MaterialTheme.typography.bodySmall, maxLines = 1)
                    Text("🌧️ Total Precipitation: ${"%.1f".format(totalPrecipitation)} mm", style = MaterialTheme.typography.bodySmall,  maxLines = 1)
                    Text("🌫️ Avg. Fog: ${"%.1f".format(avgFog)}%", style = MaterialTheme.typography.bodySmall,  maxLines = 1)
                    Text("💧 Max Humidity: ${"%.1f".format(maxHumidity)}%", style = MaterialTheme.typography.bodySmall,  maxLines = 1)
                    Text("🌡️ Max Dew Point: ${"%.1f".format(maxDewPoint)}°C", style = MaterialTheme.typography.bodySmall,  maxLines = 1)
                    Text("💨 Air Wind Gust: ${"%.1f".format(minAirWind)} - ${"%.1f".format(maxAirWind)} m/s", style = MaterialTheme.typography.bodySmall,  maxLines = 1)
                    Text("🌬️ Ground Wind: ${"%.1f".format(minGroundWind)} - ${"%.1f".format(maxGroundWind)} m/s", style = MaterialTheme.typography.bodySmall,  maxLines = 1)
                    Text("🧭 Avg. Wind Direction: ${"%.1f".format(avgWindDirection)}°", style = MaterialTheme.typography.bodySmall, maxLines = 1)
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
