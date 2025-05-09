package no.uio.ifi.in2000.met2025.data.local.configprofiles

import kotlinx.coroutines.flow.Flow
import no.uio.ifi.in2000.met2025.data.local.database.WeatherConfig
import no.uio.ifi.in2000.met2025.data.local.database.ConfigProfileDAO
import javax.inject.Inject

class WeatherConfigRepository @Inject constructor(
    private val configProfileDAO: ConfigProfileDAO
) {

    suspend fun insertConfigProfile(weatherConfig: WeatherConfig) {
        configProfileDAO.insertConfigProfile(weatherConfig)
    }

    suspend fun updateConfigProfile(weatherConfig: WeatherConfig) {
        configProfileDAO.updateConfigProfile(weatherConfig)
    }

    suspend fun deleteConfigProfile(weatherConfig: WeatherConfig) {
        configProfileDAO.deleteConfigProfile(weatherConfig)
    }

    fun getAllConfigProfiles(): Flow<List<WeatherConfig>> {
        return configProfileDAO.getAllConfigProfiles()
    }

    fun getDefaultConfigProfile(): Flow<WeatherConfig?> {
        return configProfileDAO.getDefaultConfigProfile()
    }

    fun getWeatherConfig(configId: Int): Flow<WeatherConfig?> {
        return configProfileDAO.getConfigProfile(configId)
    }

    fun getAllConfigProfileNames(): Flow<List<String>> {
        return configProfileDAO.getAllConfigProfileNames()
    }
}
