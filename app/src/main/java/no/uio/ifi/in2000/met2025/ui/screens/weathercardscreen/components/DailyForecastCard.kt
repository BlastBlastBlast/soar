package no.uio.ifi.in2000.met2025.ui.screens.weathercardscreen.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CutCornerShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import no.uio.ifi.in2000.met2025.data.models.locationforecast.ForecastDataItem
import no.uio.ifi.in2000.met2025.data.models.getWeatherIconRes
import no.uio.ifi.in2000.met2025.domain.helpers.formatZuluTimeToLocalDate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import no.uio.ifi.in2000.met2025.domain.helpers.formatZuluTimeToLocalDayMonth
import no.uio.ifi.in2000.met2025.ui.theme.IconPurple


val weatherPriority = listOf(
    "heavyrainandthunder",
    "rainandthunder",
    "heavyrain",
    "rain",
    "sleet",
    "snow",
    "fog",
    "cloudy",
    "partlycloudy_day",
    "fair_day",
    "clearsky_day"
)


fun getDominantSymbolCode(items: List<ForecastDataItem>): String? {
    // If there are no symbol codes at all, bail out immediately
    val allCodes = items.mapNotNull { it.values.symbolCode }
    if (allCodes.isEmpty()) return null

    // First: look for any of our priority symbols
    for (prioritySymbol in weatherPriority) {
        allCodes.firstOrNull { it.contains(prioritySymbol) }?.let { return it }
    }

    // Next: try to pick a "day" variant if possible
    val dayCode = allCodes.firstOrNull { it.contains("day", ignoreCase = true) }
    if (dayCode != null) return dayCode

    // Finally: pick the most common symbol
    return allCodes
        .groupingBy { it }
        .eachCount()
        .maxByOrNull { it.value }
        ?.key
}

fun getSymbolDescription(symbolCode: String?): String {
    if (symbolCode.isNullOrBlank()) return ""

    val base = symbolCode
        .removeSuffix("_day")
        .removeSuffix("_night")
        .removeSuffix("_polartwilight")
        .lowercase()

    val explicit = when {
        base.contains("heavyrainandthunder") -> "Heavy Rain and Thunder"
        base.contains("rainandthunder") -> "Rain and Thunder"
        base.contains("heavyrain") -> "Heavy Rain"
        base.contains("rain") -> "Rain"
        base.contains("sleet") -> "Sleet"
        base.contains("snow") -> "Snow"
        base.contains("fog") -> "Fog"
        base.contains("cloudy") && !base.contains("partly") -> "Cloudy"
        base.contains("partlycloudy") -> "Partly Cloudy"
        base.contains("fair") -> "Fair"
        base.contains("clearsky") -> "Clear Sky"
        else -> null
    }
    if (explicit != null) return explicit

    return base
        .split(Regex("[^A-Za-z]+"))
        .filter { it.isNotBlank() }
        .joinToString(" ") { token -> token.replaceFirstChar { it.uppercase() } }
}


fun getGradientForSymbol(symbolCode: String?): Brush {
    return when {
        symbolCode == null -> Brush.verticalGradient(
            colors = listOf(Color(0xFF2D2D40), Color(0xFF121212))
        )

        symbolCode.contains("clearsky") || symbolCode.contains("fair") ->
            Brush.verticalGradient(
                colors = listOf(
                    Color(0xFFFFF8E1), Color(0xFFFFCC80), Color(0xFFFFA726)
                )
            )


        symbolCode.contains("partlycloudy") || symbolCode.contains("cloudy") ->
            Brush.verticalGradient(
                colors = listOf(Color(0xFFCFD8DC), Color(0xFF90A4AE), Color(0xFF455A64))
            )

        symbolCode.contains("rain") || symbolCode.contains("showers") ->
            Brush.verticalGradient(
                colors = listOf(Color(0xFF81D4FA), Color(0xFF4FC3F7), Color(0xFF0288D1))
            )

        symbolCode.contains("snow") || symbolCode.contains("sleet") ->
            Brush.verticalGradient(
                colors = listOf(Color(0xFFE1F5FE), Color(0xFFB3E5FC), Color(0xFF81D4FA))
            )

        symbolCode.contains("fog") ->
            Brush.verticalGradient(
                colors = listOf(Color(0xFFECEFF1), Color(0xFFB0BEC5), Color(0xFF607D8B))
            )

        symbolCode.contains("thunder") ->
            Brush.verticalGradient(
                colors = listOf(Color(0xFFD1C4E9), Color(0xFF9575CD), Color(0xFF512DA8))
            )

        else -> Brush.verticalGradient(
            colors = listOf(Color(0xFF424242), Color(0xFF1C1C1C))
        )
    }
}

fun List<Double>.averageOrNull(): Double? =
    if (isNotEmpty()) average() else null

data class WeatherInfoItem(
    val label: String,
    val value: String?  // null means “no data”
)

@Composable
fun DailyForecastCard(
    forecastItems: List<ForecastDataItem>,
    modifier: Modifier = Modifier
) {
    if (forecastItems.isEmpty()) return

    val avgTemp          = forecastItems.map { it.values.airTemperature }.average()
    val symbolCode       = getDominantSymbolCode(forecastItems)
    val dayLabel         = formatZuluTimeToLocalDayMonth(forecastItems.first().time)
    val description      = getSymbolDescription(symbolCode)
    val avgCloudCover    = forecastItems.mapNotNull { it.values.cloudAreaFraction }.averageOrNull()
    val totalPrecip      = forecastItems.mapNotNull { it.values.precipitationAmount }
        .takeIf { it.isNotEmpty() }?.sum()
    val avgFog           = forecastItems.mapNotNull { it.values.fogAreaFraction }.averageOrNull()
    val maxHumidity      = forecastItems.mapNotNull { it.values.relativeHumidity }.maxOrNull()
    val maxDewPoint      = forecastItems.mapNotNull { it.values.dewPointTemperature }.maxOrNull()
    val maxAirWind       = forecastItems.mapNotNull { it.values.windSpeedOfGust }.maxOrNull()
    val minAirWind       = forecastItems.mapNotNull { it.values.windSpeedOfGust }.minOrNull()
    val maxGroundWind    = forecastItems.mapNotNull { it.values.windSpeed }.maxOrNull()
    val minGroundWind    = forecastItems.mapNotNull { it.values.windSpeed }.minOrNull()
    val avgWindDirection = forecastItems.mapNotNull { it.values.windFromDirection }.averageOrNull()

    val infoItems = listOf(
        "Cloud cover"    to avgCloudCover?.let { "%.1f%%".format(it) },
        "Precipitation"  to totalPrecip?.let { "%.1f mm".format(it) },
        "Fog"            to avgFog?.let { "%.1f%%".format(it) },
        "Humidity"       to maxHumidity?.let { "%.1f%%".format(it) },
        "Dew point"      to maxDewPoint?.let { "%.1f°C".format(it) },
        "Air wind"       to if (minAirWind != null && maxAirWind != null)
            "Min %.1f / Max %.1f m/s".format(minAirWind, maxAirWind)
        else null,
        "Ground wind"    to if (minGroundWind != null && maxGroundWind != null)
            "Min %.1f / Max %.1f m/s".format(minGroundWind, maxGroundWind)
        else null,
        "Wind direction" to avgWindDirection?.let { "%.1f°".format(it) }
    )

    Card(
        modifier = modifier
            .width(260.dp)
            .semantics {
                // Describe the entire card at once
                contentDescription = buildString {
                    append("$dayLabel forecast: ${avgTemp.toInt()}°C, $description. ")
                    infoItems.forEach { (label, value) ->
                        append("$label: ${value ?: "not available"}. ")
                    }
                }
            },
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(8.dp)
    ) {
        Box(
            modifier = Modifier
                .background(getGradientForSymbol(symbolCode))
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                // Date heading
                Box(
                    modifier = Modifier
                        .wrapContentWidth()
                        .background(
                            color = MaterialTheme.colorScheme.primary,
                            shape = CutCornerShape(
                                //topEnd = 32.dp,
                                bottomEnd = 40.dp
                            )
                        )
                        .padding(
                            start = 12.dp,
                            end = 32.dp,   // extra padding to balance the slant
                            top = 4.dp,
                            bottom = 4.dp
                        )
                ) {
                    Text(
                        text = dayLabel + " ",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.semantics { heading() }
                    )
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(150.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(
                        verticalArrangement = Arrangement.Center,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = "${avgTemp.toInt()}°",
                            style = MaterialTheme.typography.displayMedium,
                            color = Color.White
                        )
                        Text(
                            text = description,
                            color = Color.White,
                            style = MaterialTheme.typography.bodySmall,
                            maxLines = 1
                        )
                    }

                    // Weather icon or fallback
                    if (symbolCode == null) {
                        Icon(
                            imageVector = Icons.Default.CloudOff,
                            contentDescription = "No weather data",
                            tint = IconPurple,
                            modifier = Modifier.size(130.dp)
                        )
                    } else {
                        getWeatherIconRes(symbolCode)?.let { resId ->
                            Image(
                                painter = painterResource(id = resId),
                                contentDescription = getSymbolDescription(symbolCode),
                                modifier = Modifier.size(130.dp)
                            )
                        } ?: Icon(
                            imageVector = Icons.Default.CloudOff,
                            contentDescription = "Unknown symbol",
                            tint = IconPurple,
                            modifier = Modifier.size(130.dp)
                        )
                    }
                }

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    infoItems.forEach { (label, value) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .semantics {
                                    contentDescription = "$label: ${value ?: "not available"}"
                                },
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = label,
                                color = Color.White,
                                style = MaterialTheme.typography.labelMedium
                            )
                            Text(
                                text = value ?: "DATA NOT AVAILABLE",
                                color = if (value != null) Color.White else IconPurple,
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = if (value != null)
                                    androidx.compose.ui.text.font.FontWeight.Bold
                                else
                                    androidx.compose.ui.text.font.FontWeight.Normal
                            )
                        }
                    }
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
        .take(3)

    LazyRow(
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(dailyForecasts) { dayForecast ->
            DailyForecastCard(forecastItems = dayForecast)
        }
    }
}

@Composable
fun DailyForecastRowSection(forecastItems: List<ForecastDataItem>) {
    Column {
        Text(
            text = "Daily Forecast",
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(horizontal = 16.dp)
        )
        DailyLazyRow(allForecastItems = forecastItems)
    }
}