package no.uio.ifi.in2000.met2025.data.local.configprofiles

import kotlinx.coroutines.flow.Flow
import no.uio.ifi.in2000.met2025.data.local.database.WeatherConfig
import no.uio.ifi.in2000.met2025.data.local.database.WeatherConfigDao
import javax.inject.Inject

/**
 * WeatherConfigRepository
 *
 * The WeatherConfigRepository is a repository layer for persisting
 * and retrieving WeatherConfig entities.
 * Delegates all operations to the provided WeatherConfigDao.
 */

class WeatherConfigRepository @Inject constructor(
    private val weatherConfigDao: WeatherConfigDao
) {

    suspend fun insertWeatherConfig(weatherConfig: WeatherConfig) {
        weatherConfigDao.insertWeatherConfig(weatherConfig)
    }

    suspend fun updateWeatherConfig(weatherConfig: WeatherConfig) {
        weatherConfigDao.updateWeatherConfig(weatherConfig)
    }

    suspend fun deleteWeatherConfig(weatherConfig: WeatherConfig) {
        weatherConfigDao.deleteWeatherConfig(weatherConfig)
    }

    fun getAllWeatherConfigs(): Flow<List<WeatherConfig>> {
        return weatherConfigDao.findAllWeatherConfigs()
    }

    fun getDefaultWeatherConfig(): Flow<WeatherConfig?> {
        return weatherConfigDao.findDefaultWeatherConfig()
    }

    fun getWeatherConfig(configId: Int): Flow<WeatherConfig?> {
        return weatherConfigDao.findWeatherConfig(configId)
    }

    fun getAllWeatherConfigNames(): Flow<List<String>> {
        return weatherConfigDao.findAllWeatherConfigNames()
    }
}
