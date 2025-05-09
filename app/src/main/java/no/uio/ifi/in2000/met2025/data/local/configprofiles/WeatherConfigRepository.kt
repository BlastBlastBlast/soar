package no.uio.ifi.in2000.met2025.data.local.configprofiles

import kotlinx.coroutines.flow.Flow
import no.uio.ifi.in2000.met2025.data.local.database.WeatherConfig
import no.uio.ifi.in2000.met2025.data.local.database.WeatherConfigDao
import javax.inject.Inject

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
        return weatherConfigDao.getAllWeatherConfigs()
    }

    fun getDefaultWeatherConfig(): Flow<WeatherConfig?> {
        return weatherConfigDao.getDefaultWeatherConfig()
    }

    fun getWeatherConfig(configId: Int): Flow<WeatherConfig?> {
        return weatherConfigDao.getWeatherConfig(configId)
    }

    fun getAllWeatherConfigNames(): Flow<List<String>> {
        return weatherConfigDao.getAllWeatherConfigNames()
    }
}
