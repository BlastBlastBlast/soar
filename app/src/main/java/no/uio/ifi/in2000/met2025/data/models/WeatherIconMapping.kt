package no.uio.ifi.in2000.met2025.data.models

import no.uio.ifi.in2000.met2025.R


fun getWeatherIconRes(symbolCode: String?): Int? {
    return when (symbolCode) {
        "clearsky_day" -> R.drawable._1d
        "clearsky_night" -> R.drawable._1n
        "clearsky_polartwilight" -> R.drawable._1m
        "fair_day" -> R.drawable._2d
        "fair_night" -> R.drawable._2n
        "fair_polartwilight" -> R.drawable._2m
        "partlycloudy_day" -> R.drawable._3d
        "partlycloudy_night" -> R.drawable._3n
        "partlycloudy_polartwilight" -> R.drawable._3m
        "cloudy" -> R.drawable._4
        "rainshowers_day" -> R.drawable._5d
        "rainshowers_night" -> R.drawable._5n
        "rainshowers_polartwilight" -> R.drawable._5m
        "rainshowersandthunder_day" -> R.drawable._6d
        "rainshowersandthunder_night" -> R.drawable._6n
        "rainshowersandthunder_polartwilight" -> R.drawable._6m
        "sleetshowers_day" -> R.drawable._7d
        "sleetshowers_night" -> R.drawable._7n
        "sleetshowers_polartwilight" -> R.drawable._7m
        "snowshowers_day" -> R.drawable._8d
        "snowshowers_night" -> R.drawable._8n
        "snowshowers_polartwilight" -> R.drawable._8m
        "rain" -> R.drawable._9
        "heavyrain" -> R.drawable._10
        "heavyrainandthunder" -> R.drawable._11
        "sleet" -> R.drawable._12
        "snow" -> R.drawable._13
        "snowandthunder" -> R.drawable._14
        "fog" -> R.drawable._15
        "sleetshowersandthunder_day" -> R.drawable._20d
        "sleetshowersandthunder_night" -> R.drawable._20n
        "sleetshowersandthunder_polartwilight" -> R.drawable._20m
        "snowshowersandthunder_day" -> R.drawable._21d
        "snowshowersandthunder_night" -> R.drawable._21n
        "snowshowersandthunder_polartwilight" -> R.drawable._21m
        "rainandthunder" -> R.drawable._22
        "sleetandthunder" -> R.drawable._23
        "lightrainshowersandthunder_day" -> R.drawable._24d
        "lightrainshowersandthunder_night" -> R.drawable._24n
        "lightrainshowersandthunder_polartwilight" -> R.drawable._24m
        "heavyrainshowersandthunder_day" -> R.drawable._25d
        "heavyrainshowersandthunder_night" -> R.drawable._25n
        "heavyrainshowersandthunder_polartwilight" -> R.drawable._25m
        "lightssleetshowersandthunder_day" -> R.drawable._26d
        "lightssleetshowersandthunder_night" -> R.drawable._26n
        "lightssleetshowersandthunder_polartwilight" -> R.drawable._26m
        "heavysleetshowersandthunder_day" -> R.drawable._27d
        "heavysleetshowersandthunder_night" -> R.drawable._27n
        "heavysleetshowersandthunder_polartwilight" -> R.drawable._27m
        "lightssnowshowersandthunder_day" -> R.drawable._28d
        "lightssnowshowersandthunder_night" -> R.drawable._28n
        "lightssnowshowersandthunder_polartwilight" -> R.drawable._28m
        "heavysnowshowersandthunder_day" -> R.drawable._29d
        "heavysnowshowersandthunder_night" -> R.drawable._29n
        "heavysnowshowersandthunder_polartwilight" -> R.drawable._29m
        "lightrainandthunder" -> R.drawable._30
        "lightsleetandthunder" -> R.drawable._31
        "heavysleetandthunder" -> R.drawable._32
        "lightsnowandthunder" -> R.drawable._33
        "heavysnowandthunder" -> R.drawable._34
        "lightrainshowers_day" -> R.drawable._40d
        "lightrainshowers_night" -> R.drawable._40n
        "lightrainshowers_polartwilight" -> R.drawable._40m
        "heavyrainshowers_day" -> R.drawable._41d
        "heavyrainshowers_night" -> R.drawable._41n
        "heavyrainshowers_polartwilight" -> R.drawable._41m
        "lightsleetshowers_day" -> R.drawable._42d
        "lightsleetshowers_night" -> R.drawable._42n
        "lightsleetshowers_polartwilight" -> R.drawable._42m
        "heavysleetshowers_day" -> R.drawable._43d
        "heavysleetshowers_night" -> R.drawable._43n
        "heavysleetshowers_polartwilight" -> R.drawable._43m
        "lightsnowshowers_day" -> R.drawable._44d
        "lightsnowshowers_night" -> R.drawable._44n
        "lightsnowshowers_polartwilight" -> R.drawable._44m
        "heavysnowshowers_day" -> R.drawable._45d
        "heavysnowshowers_night" -> R.drawable._45n
        "heavysnowshowers_polartwilight" -> R.drawable._45m
        "lightrain" -> R.drawable._46
        "lightsleet" -> R.drawable._47
        "heavysleet" -> R.drawable._48
        "lightsnow" -> R.drawable._49
        "heavysnow" -> R.drawable._50
        else -> null
    }
}
