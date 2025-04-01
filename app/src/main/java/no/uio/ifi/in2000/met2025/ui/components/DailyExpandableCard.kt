package no.uio.ifi.in2000.met2025.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
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
    val representativeItem = forecastItems.first()
    val evaluations = evaluateParameterConditions(representativeItem)

    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Forecast for: $day", style = MaterialTheme.typography.headlineSmall)

            LaunchStatusIcon(status = overallStatus)

            Spacer(modifier = Modifier.padding(top = 8.dp))

            evaluations.forEach { param ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(param.label, style = MaterialTheme.typography.bodyMedium)
                    Row {
                        Text(param.value, style = MaterialTheme.typography.bodyMedium)
                        LaunchStatusIcon(status = param.status)
                    }
                }
            }
        }
    }
}



/*
@Composable
fun DailyExpandableCard(forecastItem: ForecastDataItem,
                        modifier: Modifier = Modifier
){
    var expanded by remember { mutableStateOf(false) }
    Card(modifier = modifier
        .fillMaxWidth()
        .clickable { expanded = !expanded },
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)) {

    }
    LazyRow(modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        item {

        }

    }


}*/