package no.uio.ifi.in2000.met2025.ui.screens.weathercardscreen.components.filter

import no.uio.ifi.in2000.met2025.data.models.launchstatus.LaunchStatus
import no.uio.ifi.in2000.met2025.data.models.ForecastDataItem
import no.uio.ifi.in2000.met2025.data.models.launchstatus.evaluateLaunchConditions
import no.uio.ifi.in2000.met2025.data.local.database.ConfigProfile


data class LaunchStatusFilter(
    val allowedStatuses: Set<LaunchStatus> = setOf(
        LaunchStatus.SAFE,
        LaunchStatus.CAUTION,
        LaunchStatus.DISABLED
    )
)

fun forecastPassesFilter(
    forecastItem: ForecastDataItem,
    config: ConfigProfile,
    filter: LaunchStatusFilter
): Boolean {
    val status = evaluateLaunchConditions(forecastItem, config)
    return status in filter.allowedStatuses
}