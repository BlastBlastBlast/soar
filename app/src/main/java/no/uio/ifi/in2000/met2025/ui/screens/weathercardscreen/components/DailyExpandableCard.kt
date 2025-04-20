package no.uio.ifi.in2000.met2025.ui.screens.weathercardscreen.components


import androidx.compose.foundation.Image
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.unit.dp
import no.uio.ifi.in2000.met2025.data.models.locationforecast.ForecastDataItem
import no.uio.ifi.in2000.met2025.data.models.getWeatherIconRes
import no.uio.ifi.in2000.met2025.domain.helpers.formatZuluTimeToLocalDate

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

fun getBackgroundColorForSymbol(symbolCode: String?): Color {
    return when {
        symbolCode == null -> Color(0xFF2D2D40)
        symbolCode.contains("clearsky") || symbolCode.contains("fair") ->
            Color(0xFFfdd835)
        symbolCode.contains("partlycloudy") || symbolCode.contains("cloudy") ->
            Color(0xFF90A4AE)
        symbolCode.contains("rain") || symbolCode.contains("showers") ->
            Color(0xFF4FC3F7)
        symbolCode.contains("snow") || symbolCode.contains("sleet") ->
            Color(0xFFB3E5FC)
        symbolCode.contains("fog") ->
            Color(0xFFB0BEC5)
        symbolCode.contains("thunder") ->
            Color(0xFF9575CD)
        else -> Color(0xFF2D2D40)
    }
}
data class WeatherInfoItem(
    val label: String,
    val value: String,
    val icon: @Composable () -> Unit
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
    val backgroundColor = getBackgroundColorForSymbol(symbolCode)

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
        WeatherInfoItem("Clouds", "${"%.1f".format(avgCloudCover)}%", {
            Icon(Icons.Default.Cloud, contentDescription = null, tint = Color.White)
        }),
        WeatherInfoItem("Percip", "${"%.1f".format(totalPrecipitation)} mm", {
            Icon(Icons.Default.WaterDrop, contentDescription = null, tint = Color.White)
        }),
        WeatherInfoItem("Fog", "${"%.1f".format(avgFog)}%", {
            Icon(Icons.Default.VisibilityOff, contentDescription = null, tint = Color.White)
        }),
        WeatherInfoItem("Humidity", "${"%.1f".format(maxHumidity)}%", {
            Icon(Icons.Default.InvertColors, contentDescription = null, tint = Color.White)
        }),
        WeatherInfoItem("Dew Point", "${"%.1f".format(maxDewPoint)}°C", {
            Icon(Icons.Default.Thermostat, contentDescription = null, tint = Color.White)
        }),
        WeatherInfoItem("Air Wind", "Min ${"%.1f".format(minAirWind)} / Max ${"%.1f".format(maxAirWind)} m/s", {
            Icon(Icons.Default.Air, contentDescription = null, tint = Color.White)
        }),
        WeatherInfoItem("Ground Wind", "Min ${"%.1f".format(minGroundWind)}  / Max ${"%.1f".format(maxGroundWind)} m/s", {
            Icon(Icons.Default.Air, contentDescription = null, tint = Color.White)
        }),
        WeatherInfoItem("Wind Direc.", "${"%.1f".format(avgWindDirection)}°", {
            Icon(Icons.Default.Explore, contentDescription = null, tint = Color.White)
        })
    )


    Card(
        modifier = modifier
            .padding(16.dp)
            .width(260.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        elevation = CardDefaults.cardElevation(8.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(dayLabel, style = MaterialTheme.typography.titleLarge, color = Color.White)

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
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
                        modifier = Modifier.size(72.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                items(infoItems) { item ->
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        item.icon()
                        Text(item.label, color = Color.LightGray, style = MaterialTheme.typography.labelSmall)
                        Text(item.value, color = Color.White, style = MaterialTheme.typography.bodySmall)
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




