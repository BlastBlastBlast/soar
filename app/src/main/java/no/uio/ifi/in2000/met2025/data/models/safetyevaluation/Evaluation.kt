package no.uio.ifi.in2000.met2025.data.models.safetyevaluation

import no.uio.ifi.in2000.met2025.data.local.database.ConfigProfile
import no.uio.ifi.in2000.met2025.data.models.ConfigParameter
import no.uio.ifi.in2000.met2025.data.models.locationforecast.ForecastDataItem
import no.uio.ifi.in2000.met2025.data.models.locationforecast.ForecastDataValues
import no.uio.ifi.in2000.met2025.data.models.isobaric.IsobaricData
import no.uio.ifi.in2000.met2025.domain.helpers.icon
import no.uio.ifi.in2000.met2025.domain.helpers.isEnabled
import no.uio.ifi.in2000.met2025.domain.helpers.isEnabledAt
import no.uio.ifi.in2000.met2025.domain.helpers.label
import no.uio.ifi.in2000.met2025.domain.helpers.threshold
import no.uio.ifi.in2000.met2025.domain.helpers.toConfigParameter
import no.uio.ifi.in2000.met2025.domain.helpers.unit
import no.uio.ifi.in2000.met2025.domain.helpers.valueAt
import kotlin.reflect.full.memberProperties

sealed class ParameterState {
    data object Missing : ParameterState() // Any required values are missing
    data object Disabled : ParameterState() // Parameter evaluation is turned off
    data class Available(val relativeUnsafety: Double) : ParameterState()
}

data class ParameterEvaluation(
    val label: String,
    val value: String,
    val state: ParameterState,
    val icon: EvaluationIcon? = null
)

fun evaluateLaunchConditions(config: ConfigProfile, forecastDataItem: ForecastDataItem? = null, isobaricData: IsobaricData? = null): ParameterState
=
    when {
        forecastDataItem == null && isobaricData == null -> ParameterState.Missing
        isobaricData == null -> evaluateLaunchConditions(forecastDataItem!!, config)
        forecastDataItem == null -> evaluateLaunchConditions(isobaricData, config)

        ConfigParameter.entries.all { !forecastDataItem.isEnabledAt(it, config) }
                && ((!config.isEnabledAirWind && !config.isEnabledWindShear)
                || (config.isEnabledAltitudeUpperBound && config.altitudeUpperBound <= 0)) -> ParameterState.Disabled

        ForecastDataValues::class.memberProperties.any { it.get(forecastDataItem.values) == null } -> ParameterState.Missing

        else -> ParameterState.Available(
            relativeUnsafety(config = config, forecastDataItem = forecastDataItem, isobaricData = isobaricData)!!
        )
    }

fun evaluateLaunchConditions(forecastDataItem: ForecastDataItem, config: ConfigProfile): ParameterState {
    if (ConfigParameter.entries.all { !forecastDataItem.isEnabledAt(it, config) }) {
        return ParameterState.Disabled
    }

    if (ForecastDataValues::class.memberProperties.any { it.get(forecastDataItem.values) == null }) {
        return ParameterState.Missing
    }

    return ParameterState.Available(
        relativeUnsafety(config = config, forecastDataItem = forecastDataItem, )!!
    )
}

fun evaluateLaunchConditions(isobaricData: IsobaricData, config: ConfigProfile): ParameterState {
    if ((!config.isEnabledAirWind && !config.isEnabledWindShear) || (config.isEnabledAltitudeUpperBound && config.altitudeUpperBound <= 0)) {
        return ParameterState.Disabled
    }

    return ParameterState.Available(
        relativeUnsafety(config = config, isobaricData = isobaricData)!!
    )
}

fun evaluateParameterCondition(value: Double?, config: ConfigProfile, parameter: ConfigParameter): ParameterState
=
    when {
        value == null -> ParameterState.Missing
        !config.isEnabled(parameter) -> ParameterState.Disabled
        else -> ParameterState.Available(
            relativeUnsafety(value, config.threshold(parameter))!!
        )
    }

fun evaluateParameterConditions(forecast: ForecastDataItem, config: ConfigProfile): List<ParameterEvaluation>
=
    ForecastDataValues::class.memberProperties
        .mapNotNull { it.toConfigParameter() }
        .sortedWith(compareBy { ConfigParameter.entries.indexOf(it) })
        .map { configParameter ->
            val value = forecast.valueAt(configParameter)
            val state = when {
                value == null -> ParameterState.Missing
                !config.isEnabled(configParameter) -> ParameterState.Disabled
                else -> ParameterState.Available(
                    relativeUnsafety(
                        value,
                        config.threshold(configParameter)
                    )!!
                )
            }

            ParameterEvaluation(
                label = configParameter.label(),
                value = when (state) {
                    is ParameterState.Missing -> "Not available"
                    is ParameterState.Disabled -> "$value ${configParameter.unit()}"
                    is ParameterState.Available -> {
                        "$value ${configParameter.unit()}"
                    }
                },
                state = state,
                icon = configParameter.icon()
            )
        }