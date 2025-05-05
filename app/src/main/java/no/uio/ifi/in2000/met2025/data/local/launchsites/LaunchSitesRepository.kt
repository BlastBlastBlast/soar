package no.uio.ifi.in2000.met2025.data.local.launchsites

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import no.uio.ifi.in2000.met2025.data.local.database.LaunchSite
import no.uio.ifi.in2000.met2025.data.local.database.LaunchSiteDAO
import javax.inject.Inject

class LaunchSitesRepository @Inject constructor(
    private val launchSiteDAO: LaunchSiteDAO
){
    fun getCurrentCoordinates(
        defaultCoordinates: Pair<Double, Double> = Pair(59.942, 10.726)
    ): Flow<Pair<Double, Double>> {
        return getLastVisitedTempSite().map { launchSite ->
            launchSite?.let { Pair(it.latitude, it.longitude) } ?: defaultCoordinates
        }
    }

    suspend fun insert(sites: LaunchSite) {
        launchSiteDAO.insert(sites)
    }

    suspend fun deleteSite(site: LaunchSite) {
        launchSiteDAO.delete(site)
    }

    suspend fun getSiteByName(name: String): LaunchSite? {
        return launchSiteDAO.getSiteByName(name)
    }

    fun getAll(): Flow<List<LaunchSite>> {
        return launchSiteDAO.getAll()
    }

    suspend fun update(sites: LaunchSite) {
        launchSiteDAO.update(sites)
    }

    fun getLastVisitedTempSite(): Flow<LaunchSite?> {
        return launchSiteDAO.getLastVisitedTempSite()
    }

    suspend fun getLastVisitedElevation(): Double {
        return launchSiteDAO.getLastVisitedTempSite().firstOrNull()?.elevation ?: 0.0
    }

    fun getNewMarkerTempSite(): Flow<LaunchSite?> {
        return launchSiteDAO.getNewMarkerTempSite()
    }

    suspend fun checkIfSiteExists(name : String) : Boolean {
        return launchSiteDAO.checkIfSiteExists(name) != null
    }

    fun getAllLaunchSiteNames() : Flow<List<String>> {
        return launchSiteDAO.getAllLaunchSiteNames()
    }

    suspend fun updateElevation(uid: Int, elevation: Double) =
        launchSiteDAO.updateElevation(uid, elevation)
}