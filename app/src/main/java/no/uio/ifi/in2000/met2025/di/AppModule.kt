package no.uio.ifi.in2000.met2025.di

import android.content.Context
import androidx.lifecycle.SavedStateHandle
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.request.header
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import no.uio.ifi.in2000.met2025.data.local.Database.AppDatabase
import no.uio.ifi.in2000.met2025.data.local.Database.LaunchSiteDAO
import no.uio.ifi.in2000.met2025.data.remote.forecast.LocationForecastDataSource
import no.uio.ifi.in2000.met2025.data.remote.forecast.LocationForecastRepository
import no.uio.ifi.in2000.met2025.data.remote.isobaric.IsobaricDataSource
import no.uio.ifi.in2000.met2025.data.remote.isobaric.IsobaricRepository
import no.uio.ifi.in2000.met2025.ui.screens.home.maps.LocationViewModel
import no.uio.ifi.in2000.met2025.domain.WeatherModel
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
        defaultRequest {
            header("TEAM21", "RakettOppskyting larswt@uio.no")
        }
    }

    @Provides
    @Singleton
    @Named("gribClient")
    fun provideGribClient(): HttpClient = HttpClient(CIO) {
        install(ContentNegotiation) { }
        install(Logging) {
            level = LogLevel.INFO
        }
    }

    @Provides
    @Singleton
    fun provideLocationForecastDataSource(@Named("jsonClient") client: HttpClient): LocationForecastDataSource {
        return LocationForecastDataSource(client)
    }

    @Provides
    @Singleton
    fun provideLocationForecastRepository(
        dataSource: LocationForecastDataSource
    ): LocationForecastRepository {
        return LocationForecastRepository(dataSource)
    }

    @Provides
    @Singleton
    fun provideIsobaricDataSource(@Named("gribClient") client: HttpClient): IsobaricDataSource {
        return IsobaricDataSource(client)
    }

    @Provides
    @Singleton
    fun provideIsobaricRepository(
        dataSource: IsobaricDataSource
    ): IsobaricRepository {
        return IsobaricRepository(dataSource)
    }

    @Provides
    @Singleton
    fun provideWeatherModel(
        locationForecastRepository: LocationForecastRepository,
        isobaricRepository: IsobaricRepository
    ): WeatherModel {
        return WeatherModel(locationForecastRepository, isobaricRepository)
    }
}

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext appContext: Context): AppDatabase {
        return Room.databaseBuilder(
            appContext,
            AppDatabase::class.java,
            "launch_site_db"
        ).build()
    }

    @Provides
    fun provideLaunchSiteDao(db: AppDatabase): LaunchSiteDAO {
        return db.launchSiteDao()
    }
}
