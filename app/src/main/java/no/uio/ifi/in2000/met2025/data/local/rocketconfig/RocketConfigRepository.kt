package no.uio.ifi.in2000.met2025.data.local.rocketconfig

import kotlinx.coroutines.flow.Flow
import no.uio.ifi.in2000.met2025.data.local.database.RocketParameters
import no.uio.ifi.in2000.met2025.data.local.database.RocketParametersDao

class RocketConfigRepository(private val rocketParametersDAO: RocketParametersDao) {

    suspend fun insertRocketSpecs(rocketParameters: RocketParameters) {
        rocketParametersDAO.insertRocketSpecs(rocketParameters)
    }

    suspend fun updateRocketSpecs(rocketParameters: RocketParameters) {
        rocketParametersDAO.updateRocketSpecs(rocketParameters)
    }

    suspend fun deleteRocketSpecs(rocketParameters: RocketParameters) {
        rocketParametersDAO.deleteRocketSpecs(rocketParameters)
    }

    fun getAllRocketSpecs(): Flow<List<RocketParameters>> {
        return rocketParametersDAO.getAllRocketSpecs()
    }

    fun getDefaultRocketSpecs(): Flow<RocketParameters?> {
        return rocketParametersDAO.getDefaultRocketSpecs()
    }

    fun getRocketSpecs(rocketId: Int): Flow<RocketParameters?> {
        return rocketParametersDAO.getRocketSpecs(rocketId)
    }
}