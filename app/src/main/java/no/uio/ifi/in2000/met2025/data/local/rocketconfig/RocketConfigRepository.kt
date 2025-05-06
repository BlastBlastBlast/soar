package no.uio.ifi.in2000.met2025.data.local.rocketconfig

import kotlinx.coroutines.flow.Flow
import no.uio.ifi.in2000.met2025.data.local.database.RocketConfig
import no.uio.ifi.in2000.met2025.data.local.database.RocketConfigDao

class RocketConfigRepository(private val rocketConfigDao: RocketConfigDao) {

    suspend fun insertRocketConfig(rocketConfig: RocketConfig) {
        rocketConfigDao.insertRocketConfig(rocketConfig)
    }

    suspend fun updateRocketConfig(rocketConfig: RocketConfig) {
        rocketConfigDao.updateRocketConfig(rocketConfig)
    }

    suspend fun deleteRocketConfig(rocketConfig: RocketConfig) {
        rocketConfigDao.deleteRocketConfig(rocketConfig)
    }

    fun getAllRocketConfigs(): Flow<List<RocketConfig>> {
        return rocketConfigDao.getAllRocketConfigs()
    }

    fun getDefaultRocketConfig(): Flow<RocketConfig?> {
        return rocketConfigDao.getDefaultRocketConfig()
    }

    fun getRocketConfig(rocketId: Int): Flow<RocketConfig?> {
        return rocketConfigDao.getRocketConfig(rocketId)
    }

    suspend fun setDefaultRocketConfig(rocketId: Int) {
        rocketConfigDao.setDefaultRocketConfig(rocketId)
    }
    fun getAllRocketConfigNames(): Flow<List<String>> =
        rocketConfigDao.getAllRocketConfigNames()
}