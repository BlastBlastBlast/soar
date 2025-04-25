package no.uio.ifi.in2000.met2025.ui.screens.weathercardscreen.components.filter

import no.uio.ifi.in2000.met2025.data.models.safetyevaluation.LaunchStatus
import no.uio.ifi.in2000.met2025.data.models.locationforecast.ForecastDataItem
import no.uio.ifi.in2000.met2025.data.models.safetyevaluation.evaluateLaunchConditions
import no.uio.ifi.in2000.met2025.data.local.database.ConfigProfile
import no.uio.ifi.in2000.met2025.data.models.safetyevaluation.ParameterState
import no.uio.ifi.in2000.met2025.data.models.safetyevaluation.launchStatus


data class LaunchStatusFilter(
    val allowedLaunchStatuses: Set<LaunchStatus> = setOf(
        LaunchStatus.SAFE,
        LaunchStatus.CAUTION,
        //LaunchStatus.DISABLED
    )
)

fun forecastPassesFilter(
    forecastItem: ForecastDataItem,
    config: ConfigProfile,
    filter: LaunchStatusFilter
): Boolean {
    val state = evaluateLaunchConditions(forecastItem, config)
    return state is ParameterState.Available && launchStatus(state.relativeUnsafety) in filter.allowedLaunchStatuses
//state is ParameterState.Disabled || (state is ParameterState.Enabled && state.relativeUnsafety < UNSAFE_THRESHOLD)
}