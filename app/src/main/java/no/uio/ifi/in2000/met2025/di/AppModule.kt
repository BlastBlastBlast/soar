package no.uio.ifi.in2000.met2025.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStoreFile
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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.serialization.json.Json
import no.uio.ifi.in2000.met2025.data.UserPreferences
import no.uio.ifi.in2000.met2025.data.local.database.AppDatabase
import no.uio.ifi.in2000.met2025.data.local.database.WeatherConfigDao
import no.uio.ifi.in2000.met2025.data.local.database.GribDataDAO
import no.uio.ifi.in2000.met2025.data.local.database.GribUpdatedDAO
import no.uio.ifi.in2000.met2025.data.local.database.LaunchSiteDAO
import no.uio.ifi.in2000.met2025.data.local.database.RocketConfigDao
import no.uio.ifi.in2000.met2025.data.local.rocketconfig.RocketConfigRepository
import no.uio.ifi.in2000.met2025.data.remote.forecast.LocationForecastDataSource
import no.uio.ifi.in2000.met2025.data.remote.forecast.LocationForecastRepository
import no.uio.ifi.in2000.met2025.data.remote.isobaric.IsobaricDataSource
import no.uio.ifi.in2000.met2025.data.remote.isobaric.IsobaricRepository
import no.uio.ifi.in2000.met2025.domain.IsobaricInterpolator
import no.uio.ifi.in2000.met2025.data.remote.sunrise.SunriseDataSource
import no.uio.ifi.in2000.met2025.data.remote.sunrise.SunriseRepository
import no.uio.ifi.in2000.met2025.domain.WeatherModel
import javax.inject.Named
import javax.inject.Singleton

/**
 * AppModule
 *
 * Provides application-wide dependencies, including configured HTTP clients,
 * remote data sources, repositories, and the WeatherModel.
 *
 * Special notes:
 * - Uses two distinct HttpClient instances: one for JSON APIs with content negotiation
 *   and one for GRIB binary downloads.
 * - Sets a custom header on all JSON requests for team identification.
 */
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
    fun provideSunriseDataSource(@Named("jsonClient") client: HttpClient): SunriseDataSource {
        return SunriseDataSource(client)
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
    fun provideIsobaricDataSource(
        @Named("gribClient") gribClient: HttpClient,
        @Named("jsonClient") jsonClient: HttpClient
    ): IsobaricDataSource {
        return IsobaricDataSource(gribClient, jsonClient)
    }

    @Provides
    @Singleton
    fun provideIsobaricRepository(
        dataSource: IsobaricDataSource,
        gribDataDAO: GribDataDAO,
        gribUpdatedDAO: GribUpdatedDAO
    ): IsobaricRepository {
        return IsobaricRepository(dataSource, gribDataDAO, gribUpdatedDAO)
    }

    @Provides
    @Singleton
    fun provideSunriseRepository(dataSource: SunriseDataSource): SunriseRepository {
        return SunriseRepository(dataSource)
    }

    @Provides
    @Singleton
    fun provideWeatherModel(
        locationForecastRepository: LocationForecastRepository,
        isobaricRepository: IsobaricRepository
    ): WeatherModel {
        return WeatherModel(locationForecastRepository, isobaricRepository)
    }

    @Provides
    @Singleton
    fun provideRocketConfigRepository(rocketParametersDao: RocketConfigDao): RocketConfigRepository =
        RocketConfigRepository(rocketParametersDao)

    @Provides
    @Singleton
    fun provideIsobaricInterpolator(locationForecastRepository: LocationForecastRepository,
        isobaricRepository: IsobaricRepository
    ): IsobaricInterpolator {
        return IsobaricInterpolator(locationForecastRepository, isobaricRepository)
    }
}


/**
 * DatabaseModule
 *
 * Configures the Room database and provides DAO instances.
 *
 * Special notes:
 * - Uses fallbackToDestructiveMigration, which wipes existing data on schema version changes.
 */
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
        )
            .fallbackToDestructiveMigration() // This wipes data on version change – use with caution!
            .build()
    }

    @Provides
    fun provideLaunchSiteDao(db: AppDatabase): LaunchSiteDAO = db.launchSiteDao()

    @Provides
    fun provideGribDataDao(db: AppDatabase): GribDataDAO = db.gribDataDao()

    @Provides
    fun provideGribUpdatedDao(db: AppDatabase): GribUpdatedDAO = db.gribUpdatedDao()

    @Provides
    fun provideConfigProfileDao(db: AppDatabase): WeatherConfigDao = db.WeatherConfigDao()

    @Provides
    fun provideRocketConfigDao(db: AppDatabase): RocketConfigDao = db.rocketConfigDao()
}

@Module
@InstallIn(SingletonComponent::class)
object DataStoreModule {
    private const val DATASTORE_NAME = "user_prefs"

    @Provides
    @Singleton
    fun providePreferencesDataStore(
        @ApplicationContext appContext: Context
    ): DataStore<Preferences> {
        return PreferenceDataStoreFactory.create(
            scope = CoroutineScope(Dispatchers.IO + SupervisorJob()),
            // no initialData here!
            // corruptionHandler and migrations are optional:
            corruptionHandler = null,
            migrations = emptyList(),
            produceFile = {
                // this MUST return a File
                appContext.preferencesDataStoreFile(DATASTORE_NAME)
            }
        )
    }

    @Provides
    @Singleton
    fun provideUserPreferences(
        dataStore: DataStore<Preferences>
    ): UserPreferences = UserPreferences(dataStore)
}