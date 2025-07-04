package no.uio.ifi.in2000.met2025.data.local.launchsites

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.withContext
import no.uio.ifi.in2000.met2025.data.local.database.LaunchSite
import no.uio.ifi.in2000.met2025.data.local.database.LaunchSiteDAO
import javax.inject.Inject

class LaunchSiteRepository @Inject constructor(
    private val launchSiteDAO: LaunchSiteDAO
){
    fun getCurrentCoordinates(
        defaultCoordinates: Pair<Double, Double> = Pair(59.942, 10.726)
    ): Flow<Pair<Double, Double>> {
        return getLastVisitedTempSite().map { launchSite ->
            launchSite?.let { Pair(it.latitude, it.longitude) } ?: defaultCoordinates
        }
    }

    /**
     * The full LaunchSite row flagged as “Last Visited”,
     * or null if the user has never picked a site.
     */
    @OptIn(ExperimentalCoroutinesApi::class)
    fun getActiveSite(): Flow<LaunchSite?> =
        getLastVisitedTempSite()
            .mapLatest { placeholder ->
                placeholder?.let {
                    withContext(Dispatchers.IO) {
                        launchSiteDAO.findSiteByCoordinates(it.latitude, it.longitude)
                    }
                }
            }
            .flowOn(Dispatchers.IO)

    suspend fun insert(sites: LaunchSite) {
        launchSiteDAO.insert(sites)
    }

    suspend fun deleteSite(site: LaunchSite) {
        launchSiteDAO.delete(site)
    }

    suspend fun getSiteById(id: Int): LaunchSite? {
        return launchSiteDAO.findSiteById(id)
    }

    suspend fun getSiteByName(name: String): LaunchSite? {
        return launchSiteDAO.findSiteByName(name)
    }

    fun getAll(): Flow<List<LaunchSite>> {
        return launchSiteDAO.findAll()
    }

    suspend fun update(sites: LaunchSite) {
        launchSiteDAO.update(sites)
    }

    fun getLastVisitedTempSite(): Flow<LaunchSite?> {
        return launchSiteDAO.findLastVisitedTempSite()
    }

    suspend fun getLastVisitedElevation(): Double {
        return launchSiteDAO.findLastVisitedTempSite().firstOrNull()?.elevation ?: 0.0
    }

    fun getNewMarkerTempSite(): Flow<LaunchSite?> {
        return launchSiteDAO.findNewMarkerTempSite()
    }

    suspend fun checkIfSiteExists(name : String) : Boolean {
        return launchSiteDAO.checkIfSiteExists(name) != null
    }

    fun getAllLaunchSiteNames() : Flow<List<String>> {
        return launchSiteDAO.findAllLaunchSiteNames()
    }

    suspend fun updateElevation(uid: Int, elevation: Double) =
        launchSiteDAO.updateElevation(uid, elevation)


}