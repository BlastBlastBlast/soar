package no.uio.ifi.in2000.met2025.data.local.rocketconfig

import kotlinx.coroutines.flow.Flow
import no.uio.ifi.in2000.met2025.data.local.database.RocketParameters
import no.uio.ifi.in2000.met2025.data.local.database.RocketSpecsDAO

class RocketConfigRepository(private val rocketSpecsDAO: RocketSpecsDAO) {

    suspend fun insertRocketSpecs(rocketSpecs: RocketParameters) {
        rocketSpecsDAO.insertRocketSpecs(rocketSpecs)
    }

    suspend fun updateRocketSpecs(rocketSpecs: RocketParameters) {
        rocketSpecsDAO.updateRocketSpecs(rocketSpecs)
    }

    suspend fun deleteRocketSpecs(rocketSpecs: RocketParameters) {
        rocketSpecsDAO.deleteRocketSpecs(rocketSpecs)
    }

    fun getAllRocketSpecs(): Flow<List<RocketParameters>> {
        return rocketSpecsDAO.getAllRocketSpecs()
    }

    fun getDefaultRocketSpecs(): Flow<RocketParameters?> {
        return rocketSpecsDAO.getDefaultRocketSpecs()
    }

    fun getRocketSpecs(rocketId: Int): Flow<RocketParameters?> {
        return rocketSpecsDAO.getRocketSpecs(rocketId)
    }
}