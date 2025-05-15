package no.uio.ifi.in2000.met2025.ui.screens.weatherScreen.components.weatherFilterOverlay

import no.uio.ifi.in2000.met2025.data.models.safetyevaluation.LaunchStatus
import no.uio.ifi.in2000.met2025.data.models.locationforecast.ForecastDataItem
import no.uio.ifi.in2000.met2025.data.models.safetyevaluation.evaluateConditions
import no.uio.ifi.in2000.met2025.data.local.database.WeatherConfig
import no.uio.ifi.in2000.met2025.data.models.safetyevaluation.ParameterState
import no.uio.ifi.in2000.met2025.data.models.safetyevaluation.launchStatus

/**
 * LaunchStatusFilter
 *
 * A data class used to specify which launch statuses are allowed when filtering forecasts.
 *
 * @property allowedLaunchStatuses The set of launch statuses considered acceptable.
 *                                 Defaults to allowing all (SAFE, CAUTION, UNSAFE).
 */
data class LaunchStatusFilter(
    val allowedLaunchStatuses: Set<LaunchStatus> = setOf(
        LaunchStatus.SAFE,
        LaunchStatus.CAUTION,
        LaunchStatus.UNSAFE
    )
)

/**
 * forecastPassesFilter
 *
 * Determines whether a given forecast item passes a weather configuration and launch status filter.
 *
 * @param forecastItem The forecast data to evaluate.
 * @param config The weather configuration (thresholds, toggles, etc.) to evaluate against.
 * @param filter A filter specifying which launch statuses are acceptable.
 * @return True if the forecast meets all configured parameters and has an acceptable launch status.
 */
fun forecastPassesFilter(
    forecastItem: ForecastDataItem,
    config: WeatherConfig,
    filter: LaunchStatusFilter
): Boolean {
    // Evaluate forecast data against weather configuration
    val state = evaluateConditions(forecastItem, config)

    // Forecast passes only if:
    // 1. The evaluated state is of type Available (i.e. not missing/incomplete).
    // 2. The derived launch status is within the allowed set in the filter.
    return state is ParameterState.Available && launchStatus(state.relativeUnsafety) in filter.allowedLaunchStatuses
}