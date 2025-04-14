package no.uio.ifi.in2000.met2025.data.local.rocketconfig

import kotlinx.coroutines.flow.Flow
import no.uio.ifi.in2000.met2025.data.local.database.RocketParameters
import no.uio.ifi.in2000.met2025.data.local.database.RocketParametersDao
import no.uio.ifi.in2000.met2025.data.models.RocketConfig

class RocketConfigRepository(private val rocketParametersDAO: RocketParametersDao) {

    suspend fun insertRocketParameters(rocketConfig: RocketConfig) {
        rocketParametersDAO.insertRocketParameters(rocketConfig)
    }

    suspend fun updateRocketParameters(rocketConfig: RocketConfig) {
        rocketParametersDAO.updateRocketParameters(rocketConfig)
    }

    suspend fun deleteRocketParameters(rocketConfig: RocketConfig) {
        rocketParametersDAO.deleteRocketParameters(rocketConfig)
    }

    fun getAllRocketParameters(): Flow<List<RocketConfig>> {
        return rocketParametersDAO.getAllRocketParameters()
    }

    fun getDefaultRocketParameters(): Flow<RocketConfig?> {
        return rocketParametersDAO.getDefaultRocketParameters()
    }

    fun getRocketParameters(rocketId: Int): Flow<RocketConfig?> {
        return rocketParametersDAO.getRocketParameters(rocketId)
    }
}