package no.uio.ifi.in2000.met2025.data.models.safetyevaluation

import no.uio.ifi.in2000.met2025.data.local.database.WeatherConfig
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

/**
 * Represents the state of a weather parameter in the context of safety evaluation.
 * It can be either missing, disabled, or available with a relative unsafety value.
 *
 * The available state contains a relative unsafety value as this does not restrict us to one specific way of categorizing the unsafety level.
 */
sealed class ParameterState {
    data object Missing : ParameterState() // Any required values are missing
    data object Disabled : ParameterState() // Parameter evaluation is turned off
    data class Available(val relativeUnsafety: Double) : ParameterState()
}

/**
 * This class contains the state of an icon that represents LaunchStatus.
 */
sealed class EvaluationIcon {
    data class DrawableIcon(val resId: Int) : EvaluationIcon()
    data class VectorIcon(val icon: androidx.compose.ui.graphics.vector.ImageVector) : EvaluationIcon()
}

/**
 * Represents an icon that can be used to visually represent the state of a weather parameter.
 */
data class ParameterEvaluation(
    val label: String,
    val value: String,
    val state: ParameterState,
    val icon: EvaluationIcon? = null
)

/**
 * Evaluates the launch conditions based on the provided weather configuration and forecast data.
 * It checks if the required parameters are available and enabled, and returns the appropriate state.
 *
 * This function is used when evaluating the parameters for either forecast data or isobaric data, or both.
 *
 * @return The state of the parameter evaluation.
 */
fun evaluateConditions(config: WeatherConfig, forecastDataItem: ForecastDataItem? = null, isobaricData: IsobaricData? = null): ParameterState
=
    when {
        (forecastDataItem == null && isobaricData == null) ||
                (forecastDataItem != null && forecastDataItem.hasMissingValues()) -> ParameterState.Missing

        (forecastDataItem == null || (ConfigParameter.entries.all { !forecastDataItem.isEnabledAt(it, config) })) &&
                (isobaricData == null ||
                        (!config.isEnabledAirWind && !config.isEnabledWindShear) ||
                        (config.isEnabledAltitudeUpperBound && config.altitudeUpperBound <= 0)) -> ParameterState.Disabled

        else -> ParameterState.Available(
            relativeUnsafety(config = config, forecastDataItem = forecastDataItem, isobaricData = isobaricData)!!
        )
    }

/**
 * Evaluates launch conditions based on a single parameter value and weather configuration parameter.
 */
fun evaluateConditions(config: WeatherConfig, parameter: ConfigParameter, value: Double?): ParameterState
=
    when {
        value == null -> ParameterState.Missing
        !config.isEnabled(parameter) -> ParameterState.Disabled
        else -> ParameterState.Available(
            relativeUnsafety(value, config.threshold(parameter))!!
        )
    }

/**
 * Evaluates the launch conditions based on the provided weather configuration and forecast data item.
 * It checks if the required parameters are available and enabled, and returns a list of parameter evaluations
 * to be displayed in the UI.
 */
fun evaluateParameterConditions(forecast: ForecastDataItem, config: WeatherConfig): List<ParameterEvaluation>
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