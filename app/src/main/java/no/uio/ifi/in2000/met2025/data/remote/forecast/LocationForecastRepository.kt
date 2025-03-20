package no.uio.ifi.in2000.met2025.data.remote.forecast

import no.uio.ifi.in2000.met2025.data.models.ForecastData
import no.uio.ifi.in2000.met2025.data.models.ForecastDataItem
import no.uio.ifi.in2000.met2025.data.models.ForecastDataValues
import javax.inject.Inject
import javax.inject.Named

class LocationForecastRepository @Inject constructor(
    @Named("jsonClient") private val locationForecastDataSource: LocationForecastDataSource
){
    suspend fun getForecastData(lat: Double, lon: Double, timeSpanInHours: Int): Result<ForecastData> {

        //TODO: rekne om timeSpanHours til tal element vi treng frå responsen,
        // evt lage noko som går gjennom lista og teller opp timar til vi når 72,
        // og berre returnere desse elementa.
        // Grunn: Når vi kjem opp i 50-60 element, så har data 6 timar mellomrom kontra 1 time.

        locationForecastDataSource.getForecastDataResponse(lat, lon) // hent data i form av Result<ForecastDataResponse>
            .onFailure { exception ->
                return Result.failure(exception)
            }
            .onSuccess { response ->
                return Result.success(ForecastData(
                    updatedAt = response.properties.meta.updatedAt,
                    timeSeries = response.properties.timeSeries.take(timeSpanInHours).map {
                        ForecastDataItem(
                            time = it.time,
                            values = ForecastDataValues(
                                airTemperature = it.data.instant.details.airTemperature,
                                relativeHumidity = it.data.instant.details.relativeHumidity,
                                windSpeed = it.data.instant.details.windSpeed,
                                windSpeedOfGust = it.data.instant.details.windSpeedOfGust,
                                windFromDirection = it.data.instant.details.windFromDirection,
                                fogAreaFraction = it.data.instant.details.fogAreaFraction,
                                dewPointTemperature = it.data.instant.details.dewPointTemperature,
                                cloudAreaFraction = it.data.instant.details.cloudAreaFraction,
                                precipitationAmount = it.data.next1Hours.details.precipitationAmount,
                                probabilityOfThunder = it.data.next1Hours.details.probabilityOfThunder
                            )
                        )
                    }
                ))
            }
        return Result.failure(Exception("Ukjent feil i henting av værdata"))
    }
}