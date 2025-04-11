package no.uio.ifi.in2000.met2025.data.local.launchsites

import kotlinx.coroutines.flow.Flow
import no.uio.ifi.in2000.met2025.data.local.database.LaunchSite
import no.uio.ifi.in2000.met2025.data.local.database.LaunchSiteDAO
import javax.inject.Inject

class LaunchSitesRepository @Inject constructor(
    private val launchSiteDAO: LaunchSiteDAO
){
    suspend fun insertAll(vararg sites: LaunchSite) {
        launchSiteDAO.insertAll(*sites)
    }

    suspend fun deleteSite(site: LaunchSite) {
        launchSiteDAO.delete(site)
    }

    fun getAll(): Flow<List<LaunchSite>> {
        return launchSiteDAO.getAll()
    }

    suspend fun updateSites(vararg sites: LaunchSite) {
        launchSiteDAO.updateSites(*sites)
    }


    fun getTempSite(tempName: String = "Last Visited"): Flow<LaunchSite?> {
        return launchSiteDAO.getTempSite(tempName)
    }


    fun getNewMarkerTempSite(tempName: String = "New Marker"): Flow<LaunchSite?> {
        return launchSiteDAO.getNewMarkerTempSite(tempName)
    }
}