package no.uio.ifi.in2000.met2025.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.*
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import no.uio.ifi.in2000.data.grib.GribDataSource
import no.uio.ifi.in2000.data.grib.GribRepository
import no.uio.ifi.in2000.data.weather.WeatherDataSource
import no.uio.ifi.in2000.data.weather.WeatherRepository
import javax.inject.Named
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    // Shared JSON config
    private val jsonConfig = Json {
        ignoreUnknownKeys = true
        prettyPrint = true
        isLenient = true
    }

    // ✅ JSON Client (Locationforecast)
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
        // header for identifikasjon
        defaultRequest {
            headers {
                append("RocketApplication/1.0 https://github.uio.no/IN2000-V25/team-21 (torbjeh@uio.no.com)")
            }
        }
    }

    // ✅ GRIB Client (binary or isobaric data)
    @Provides
    @Singleton
    @Named("gribClient")
    fun provideGribClient(): HttpClient = HttpClient(CIO) {
        install(Logging) {
            level = LogLevel.HEADERS // Only headers for binary files
        }
        expectSuccess = false
    }

    // ✅ Weather Repository
    // TODO

    // ✅ Grib Repository
    // TODO

    // ✅ Weather Data Source
    // TODO

    // ✅ Grib Data Source
    // TODO

}
