package no.uio.ifi.in2000.met2025.data.local.rocketconfig

import kotlinx.coroutines.flow.Flow
import no.uio.ifi.in2000.met2025.data.local.database.RocketConfig
import no.uio.ifi.in2000.met2025.data.local.database.RocketConfigDao

/**
 * RocketConfigRepository
 *
 * Provides a repository layer for managing RocketConfig entities.
 * Delegates CRUD operations and default-selection logic to the RocketConfigDao.
 *
 * Special notes:
 * - All reads return reactive Flows.
 */

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
        return rocketConfigDao.findAllRocketConfigs()
    }

    fun getDefaultRocketConfig(): Flow<RocketConfig?> {
        return rocketConfigDao.findDefaultRocketConfig()
    }

    fun getRocketConfig(rocketId: Int): Flow<RocketConfig?> {
        return rocketConfigDao.findRocketConfig(rocketId)
    }

    fun getAllRocketConfigNames(): Flow<List<String>> =
        rocketConfigDao.findAllRocketConfigNames()
}