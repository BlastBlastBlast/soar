package no.uio.ifi.in2000.met2025.data.local.configprofiles

import kotlinx.coroutines.flow.Flow
import no.uio.ifi.in2000.met2025.data.local.database.ConfigProfile
import no.uio.ifi.in2000.met2025.data.local.database.ConfigProfileDAO
import javax.inject.Inject

class ConfigProfileRepository @Inject constructor(
    private val configProfileDAO: ConfigProfileDAO
) {

    suspend fun insertConfigProfile(configProfile: ConfigProfile) {
        configProfileDAO.insertConfigProfile(configProfile)
    }

    suspend fun updateConfigProfile(configProfile: ConfigProfile) {
        configProfileDAO.updateConfigProfile(configProfile)
    }

    suspend fun deleteConfigProfile(configProfile: ConfigProfile) {
        configProfileDAO.deleteConfigProfile(configProfile)
    }

    fun getAllConfigProfiles(): Flow<List<ConfigProfile>> {
        return configProfileDAO.getAllConfigProfiles()
    }

    fun getDefaultConfigProfile(): Flow<ConfigProfile?> {
        return configProfileDAO.getDefaultConfigProfile()
    }

    fun getConfigProfile(configId: Int): Flow<ConfigProfile?> {
        return configProfileDAO.getConfigProfile(configId)
    }
}
