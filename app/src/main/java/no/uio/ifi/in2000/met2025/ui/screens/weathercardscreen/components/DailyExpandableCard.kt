package no.uio.ifi.in2000.met2025.ui.screens.weathercardscreen.components


import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
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
    return symbolCode
        ?.replace("_", " ")
        ?.replaceFirstChar { it.uppercase() }
        ?.replace("day", "")
        ?.replace("night", "")
        ?.replace("polartwilight", "")
        ?.trim() ?: ""
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

    // Basic info
    val avgTemp = forecastItems.map { it.values.airTemperature }.average()
    val symbolCode = getDominantSymbolCode(forecastItems)
    val dayLabel = formatZuluTimeToLocalDayMonth(forecastItems.first().time)
    val description = getSymbolDescription(symbolCode)

    // Nullable statistics
    val avgCloudCover = forecastItems
        .mapNotNull { it.values.cloudAreaFraction }
        .averageOrNull()

    val totalPrecipitation = forecastItems
        .mapNotNull { it.values.precipitationAmount }
        .takeIf { it.isNotEmpty() }
        ?.sum()

    val avgFog = forecastItems
        .mapNotNull { it.values.fogAreaFraction }
        .averageOrNull()

    val maxHumidity = forecastItems
        .mapNotNull { it.values.relativeHumidity }
        .maxOrNull()

    val maxDewPoint = forecastItems
        .mapNotNull { it.values.dewPointTemperature }
        .maxOrNull()

    val maxAirWind = forecastItems
        .mapNotNull { it.values.windSpeedOfGust }
        .maxOrNull()

    val minAirWind = forecastItems
        .mapNotNull { it.values.windSpeedOfGust }
        .minOrNull()

    val maxGroundWind = forecastItems
        .mapNotNull { it.values.windSpeed }
        .maxOrNull()

    val minGroundWind = forecastItems
        .mapNotNull { it.values.windSpeed }
        .minOrNull()

    val avgWindDirection = forecastItems
        .mapNotNull { it.values.windFromDirection }
        .averageOrNull()

    // Build info items
    val infoItems = listOf(
        WeatherInfoItem("☁️ Cloud cover", avgCloudCover?.let { "%.1f%%".format(it) }),
        WeatherInfoItem("🌧️ Precipitation", totalPrecipitation?.let { "%.1f mm".format(it) }),
        WeatherInfoItem("🌫️ Fog", avgFog?.let { "%.1f%%".format(it) }),
        WeatherInfoItem("💧 Humidity", maxHumidity?.let { "%.1f%%".format(it) }),
        WeatherInfoItem("🌡️ Dew point", maxDewPoint?.let { "%.1f°C".format(it) }),
        WeatherInfoItem(
            "💨 Air wind",
            if (minAirWind != null && maxAirWind != null)
                "Min %.1f / Max %.1f m/s".format(minAirWind, maxAirWind)
            else null
        ),
        WeatherInfoItem(
            "🌬️ Ground wind",
            if (minGroundWind != null && maxGroundWind != null)
                "Min %.1f / Max %.1f m/s".format(minGroundWind, maxGroundWind)
            else null
        ),
        WeatherInfoItem("🧭 Wind direction", avgWindDirection?.let { "%.1f°".format(it) })
    )

    Card(
        modifier = modifier
            .padding(16.dp)
            .width(260.dp),
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
                // Date + description
                Text(dayLabel, style = MaterialTheme.typography.titleLarge, color = Color.White)

                // Temp + icon row
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
                            style = MaterialTheme.typography.displaySmall,
                            color = Color.White
                        )
                        Text(
                            text = description,
                            color = Color.White,
                            style = MaterialTheme.typography.bodySmall,
                            maxLines = 1
                        )
                    }

                    // Icon or fallback
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
                                contentDescription = null,
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

                // Info rows
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    infoItems.forEach { item ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            if (item.value == null) {
                                Text(
                                    text = "${item.label}: NOT AVAILABLE",
                                    color = IconPurple,
                                    style = MaterialTheme.typography.labelMedium
                                )
                            } else {
                                Text(
                                    text = buildAnnotatedString {
                                        append("${item.label} ")
                                        withStyle(
                                            style = androidx.compose.ui.text.SpanStyle(
                                                fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                                            )
                                        ) {
                                            append(item.value)
                                        }
                                    },
                                    color = Color.White,
                                    style = MaterialTheme.typography.labelMedium,
                                    maxLines = 1
                                )
                            }
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