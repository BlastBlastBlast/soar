package no.uio.ifi.in2000.met2025.ui.screens.weatherScreen.components.weatherConfigOverlay

import no.uio.ifi.in2000.met2025.data.local.database.WeatherConfig

object DefaultWeatherParameters {
    val instance = WeatherConfig(
        name = "Default",
        groundWindThreshold = 8.6,
        airWindThreshold = 17.2,
        cloudCoverThreshold = 15.0,
        cloudCoverHighThreshold = 15.0,
        cloudCoverMediumThreshold = 15.0,
        cloudCoverLowThreshold = 15.0,
        humidityThreshold = 75.0,
        dewPointThreshold = 15.0,
        fogThreshold = 0.0,
        precipitationThreshold = 0.0,
        probabilityOfThunderThreshold = 0.0,
        isEnabledGroundWind = true,
        isEnabledAirWind = true,
        isEnabledCloudCover = true,
        isEnabledCloudCoverHigh = true,
        isEnabledCloudCoverMedium = true,
        isEnabledCloudCoverLow = true,
        isEnabledHumidity = true,
        isEnabledDewPoint = true,
        isEnabledWindDirection = true,
        isEnabledFog = true,
        isEnabledPrecipitation = true,
        isEnabledProbabilityOfThunder = true,
        isDefault = true
    )
}

