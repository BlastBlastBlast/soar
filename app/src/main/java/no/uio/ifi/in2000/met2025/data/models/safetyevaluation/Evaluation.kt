package no.uio.ifi.in2000.met2025.data.models.safetyevaluation

import no.uio.ifi.in2000.met2025.data.local.database.ConfigProfile
import no.uio.ifi.in2000.met2025.data.models.ConfigParameter
import no.uio.ifi.in2000.met2025.data.models.ForecastDataItem
import no.uio.ifi.in2000.met2025.data.models.ForecastDataValues
import no.uio.ifi.in2000.met2025.data.models.IsobaricData
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

fun evaluateLaunchConditions(forecastDataItem: ForecastDataItem, config: ConfigProfile): ParameterState {
    if (ConfigParameter.entries.all { !forecastDataItem.isEnabledAt(it, config) }) {
        return ParameterState.Disabled
    }

    if (ForecastDataValues::class.memberProperties.any { it.get(forecastDataItem.values) == null }) {
        return ParameterState.Missing
    }

    return ParameterState.Available(
        relativeUnsafety(forecastDataItem, config)!!
    )
}

fun evaluateLaunchConditions(isobaricData: IsobaricData, config: ConfigProfile): ParameterState {
    if (!config.isEnabledAirWind && !config.isEnabledWindShear) {
        return ParameterState.Disabled
    }

    return ParameterState.Available(
        relativeUnsafety(isobaricData, config)!!
    )
}

fun evaluateParameterCondition(value: Double?, config: ConfigProfile, parameter: ConfigParameter): ParameterState {
    if (value == null) {
        return ParameterState.Missing
    }

    if (!config.isEnabled(parameter)) {
        return ParameterState.Disabled
    }

    return ParameterState.Available(
        relativeUnsafety(value, config.threshold(parameter))!!
    )
}

fun evaluateParameterConditions(forecast: ForecastDataItem, config: ConfigProfile): List<ParameterEvaluation> {
    return ForecastDataValues::class.memberProperties
        .mapNotNull { it.toConfigParameter() }
        .sortedWith(compareBy { ConfigParameter.entries.indexOf(it) })
        .map { configParameter ->

            val value = forecast.valueAt(configParameter)
            val state = when {
                value == null -> ParameterState.Missing
                config.isEnabled(configParameter) -> ParameterState.Available(
                    relativeUnsafety(
                        value,
                        config.threshold(configParameter)
                    )!!
                )
                else -> ParameterState.Disabled
            }

            ParameterEvaluation(
                label = configParameter.label(),
                value = when (state) {
                    is ParameterState.Missing -> "Not available"
                    is ParameterState.Disabled -> "Turned Off"
                    is ParameterState.Available -> {
                        "$value ${configParameter.unit()}"
                    }
                },
                state = state,
                icon = configParameter.icon()
            )
        }
}