package no.uio.ifi.in2000.met2025.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logging
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import no.uio.ifi.in2000.met2025.data.remote.forecast.LocationForecastDataSource
import no.uio.ifi.in2000.met2025.data.remote.forecast.LocationForecastRepository
import javax.inject.Named
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    // Shared JSON configuration
    private val jsonConfig = Json {
        ignoreUnknownKeys = true
        prettyPrint = true
        isLenient = true
    }

    // Provide a JSON client for fetching location forecast data.
    @Provides
    @Singleton
    @Named("jsonClient")
    fun provideJsonClient(): HttpClient = HttpClient(CIO) {
        install(ContentNegotiation) {
            json(jsonConfig)
        }
        install(Logging) {
            level = LogLevel.INFO
        }
    }

    // Optionally, provide a GRIB client if you need it for binary or isobaric data.
    @Provides
    @Singleton
    @Named("gribClient")
    fun provideGribClient(): HttpClient = HttpClient(CIO) {
        install(Logging) {
            level = LogLevel.HEADERS // Only headers for binary files
        }
        expectSuccess = false
    }

    // Provide the LocationForecastDataSource using the JSON client.
    @Provides
    @Singleton
    fun provideLocationForecastDataSource(@Named("jsonClient") client: HttpClient): LocationForecastDataSource {
        return LocationForecastDataSource(client)
    }

    // Provide the LocationForecastRepository which depends on the data source.
    @Provides
    @Singleton
    fun provideLocationForecastRepository(
        dataSource: LocationForecastDataSource
    ): LocationForecastRepository {
        return LocationForecastRepository(dataSource)
    }
}
