package no.uio.ifi.in2000.met2025.data.local.rocketconfig

import kotlinx.coroutines.flow.Flow
import no.uio.ifi.in2000.met2025.data.local.database.RocketParameters
import no.uio.ifi.in2000.met2025.data.local.database.RocketParametersDao

class RocketConfigRepository(private val rocketParametersDAO: RocketParametersDao) {

    suspend fun insertRocketParameters(rocketParameters: RocketParameters) {
        rocketParametersDAO.insertRocketParameters(rocketParameters)
    }

    suspend fun updateRocketParameters(rocketParameters: RocketParameters) {
        rocketParametersDAO.updateRocketParameters(rocketParameters)
    }

    suspend fun deleteRocketParameters(rocketParameters: RocketParameters) {
        rocketParametersDAO.deleteRocketParameters(rocketParameters)
    }

    fun getAllRocketParameters(): Flow<List<RocketParameters>> {
        return rocketParametersDAO.getAllRocketParameters()
    }

    fun getDefaultRocketParameters(): Flow<RocketParameters?> {
        return rocketParametersDAO.getDefaultRocketParameters()
    }

    fun getRocketParameters(rocketId: Int): Flow<RocketParameters?> {
        return rocketParametersDAO.getRocketParameters(rocketId)
    }
}