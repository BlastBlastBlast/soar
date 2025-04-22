package no.uio.ifi.in2000.met2025.ui.screens.weathercardscreen.components


import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.text.style.TextOverflow


val weatherPriority = listOf(
    "heavyrain", "heavyrainandthunder", "rainandthunder", "rain",
    "sleet", "snow",
    "cloudy", "partlycloudy_day", "fair_day", "clearsky_day"
)

fun getDominantSymbolCode(items: List<ForecastDataItem>): String? {
    val allCodes = items.mapNotNull { it.values.symbolCode }

    for (prioritySymbol in weatherPriority) {
        if (allCodes.any { it.contains(prioritySymbol) }) {
            return allCodes.first { it.contains(prioritySymbol) }
        }
    }

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
                colors = listOf(Color(0xFFFFF59D), Color(0xFFFFB300), Color(0xFFFF8F00))
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

data class WeatherInfoItem(
    val label: String,
    val value: String
)

@Composable
fun DailyForecastCard(
    forecastItems: List<ForecastDataItem>,
    modifier: Modifier = Modifier
) {
    if (forecastItems.isEmpty()) return

    val avgTemp = forecastItems.map { it.values.airTemperature }.average()
    val symbolCode = getDominantSymbolCode(forecastItems)
    val dayLabel = formatZuluTimeToLocalDate(forecastItems.first().time)
    val description = getSymbolDescription(symbolCode)
    val iconRes = getWeatherIconRes(symbolCode)

    val avgCloudCover = forecastItems.mapNotNull { it.values.cloudAreaFraction }.average()
    val totalPrecipitation = forecastItems.mapNotNull { it.values.precipitationAmount }.sum()
    val avgFog = forecastItems.mapNotNull { it.values.fogAreaFraction }.average()
    val maxHumidity = forecastItems.mapNotNull { it.values.relativeHumidity }.maxOrNull() ?: 0.0
    val maxDewPoint = forecastItems.mapNotNull { it.values.dewPointTemperature }.maxOrNull() ?: 0.0
    val maxAirWind = forecastItems.mapNotNull { it.values.windSpeedOfGust }.maxOrNull() ?: 0.0
    val minAirWind = forecastItems.mapNotNull { it.values.windSpeedOfGust }.minOrNull() ?: 0.0
    val maxGroundWind = forecastItems.mapNotNull { it.values.windSpeed }.maxOrNull() ?: 0.0
    val minGroundWind = forecastItems.mapNotNull { it.values.windSpeed }.minOrNull() ?: 0.0
    val avgWindDirection = forecastItems.mapNotNull { it.values.windFromDirection }.average()

    val infoItems = listOf(
        WeatherInfoItem("☁️ Cloud cover", "${"%.1f".format(avgCloudCover)}%"),
        WeatherInfoItem("🌧️ Percipitation", "${"%.1f".format(totalPrecipitation)} mm"),
        WeatherInfoItem("🌫️ Fog", "${"%.1f".format(avgFog)}%"),
        WeatherInfoItem("💧 Humidity ", "${"%.1f".format(maxHumidity)}%"),
        WeatherInfoItem("🌡️ Dewpoint", "${"%.1f".format(maxDewPoint)}°C"),
        WeatherInfoItem("💨 Air wind", "Min ${"%.1f".format(minAirWind)} / Max ${"%.1f".format(maxAirWind)} m/s"),
        WeatherInfoItem("🌬️ Ground wind", "Min ${"%.1f".format(minGroundWind)} / Max ${"%.1f".format(maxGroundWind)} m/s"),
        WeatherInfoItem("🧭 Wind direction", "${"%.1f".format(avgWindDirection)}°")
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
                .background(getGradientForSymbol(symbolCode)) // 🎨 ny og bedre gradient
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Øverst: dato og værbeskrivelse
                Text(dayLabel, style = MaterialTheme.typography.titleLarge, color = Color.White)

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
                        Text(text = description, color = Color.White)
                    }

                    iconRes?.let {
                        Image(
                            painter = painterResource(id = it),
                            contentDescription = null,
                            modifier = Modifier.size(130.dp)
                        )
                    }
                }


                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    infoItems.forEach { item ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = item.label,
                                modifier = Modifier.weight(0.9f),
                                color = Color.LightGray,
                                style = MaterialTheme.typography.labelMedium,
                                maxLines = 1
                            )
                            Text(
                                text = item.value,
                                modifier = Modifier
                                    .weight(1.1f) // Mer plass her!
                                    .padding(start = 4.dp),
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