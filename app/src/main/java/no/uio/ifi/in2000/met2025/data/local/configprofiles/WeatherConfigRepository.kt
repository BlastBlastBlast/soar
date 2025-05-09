package no.uio.ifi.in2000.met2025.data.local.configprofiles

import kotlinx.coroutines.flow.Flow
import no.uio.ifi.in2000.met2025.data.local.database.WeatherConfig
import no.uio.ifi.in2000.met2025.data.local.database.ConfigProfileDAO
import javax.inject.Inject

class WeatherConfigRepository @Inject constructor(
    private val configProfileDAO: ConfigProfileDAO
) {

    suspend fun insertWeatherConfig(weatherConfig: WeatherConfig) {
        configProfileDAO.insertWeatherConfig(weatherConfig)
    }

    suspend fun updateWeatherConfig(weatherConfig: WeatherConfig) {
        configProfileDAO.updateWeatherConfig(weatherConfig)
    }

    suspend fun deleteWeatherConfig(weatherConfig: WeatherConfig) {
        configProfileDAO.deleteWeatherConfig(weatherConfig)
    }

    fun getAllWeatherConfigs(): Flow<List<WeatherConfig>> {
        return configProfileDAO.getAllWeatherConfigs()
    }

    fun getDefaultWeatherConfig(): Flow<WeatherConfig?> {
        return configProfileDAO.getDefaultWeatherConfig()
    }

    fun getWeatherConfig(configId: Int): Flow<WeatherConfig?> {
        return configProfileDAO.getWeatherConfig(configId)
    }

    fun getAllWeatherConfigNames(): Flow<List<String>> {
        return configProfileDAO.getAllWeatherConfigNames()
    }
}
