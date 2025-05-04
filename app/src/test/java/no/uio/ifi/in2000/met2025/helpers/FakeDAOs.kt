package no.uio.ifi.in2000.met2025.fakes

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import no.uio.ifi.in2000.met2025.data.local.database.ConfigProfile
import no.uio.ifi.in2000.met2025.data.local.database.ConfigProfileDAO
import no.uio.ifi.in2000.met2025.data.local.database.GribData
import no.uio.ifi.in2000.met2025.data.local.database.GribDataDAO
import no.uio.ifi.in2000.met2025.data.local.database.GribUpdated
import no.uio.ifi.in2000.met2025.data.local.database.GribUpdatedDAO
import no.uio.ifi.in2000.met2025.data.local.database.LaunchSite
import no.uio.ifi.in2000.met2025.data.local.database.LaunchSiteDAO

class FakeConfigProfileDao : ConfigProfileDAO {
    // Implement only needed DAO functions with dummy returns
    override suspend fun insertConfigProfile(configProfile: ConfigProfile) {
    }

    override suspend fun updateConfigProfile(configProfile: ConfigProfile) {
    }

    override suspend fun deleteConfigProfile(configProfile: ConfigProfile) {
    }

    override fun getAllConfigProfiles(): Flow<List<ConfigProfile>> {
        return flowOf(emptyList())
    }

    override fun getDefaultConfigProfile(): Flow<ConfigProfile?> {
        return flowOf(null)
    }

    override fun getConfigProfile(configId: Int): Flow<ConfigProfile?> {
        return flowOf(null)
    }

    override fun getAllConfigProfileNames(): Flow<List<String>> {
        return flowOf(emptyList())
    }
}

class FakeLaunchSiteDAO : LaunchSiteDAO {
    override suspend fun insert(sites: LaunchSite) {
    }

    override suspend fun delete(site: LaunchSite) {
    }

    override fun getAll(): Flow<List<LaunchSite>> {
        return flowOf(emptyList())
    }

    override suspend fun getSiteByName(name: String): LaunchSite? {
        return null
    }

    override suspend fun update(sites: LaunchSite) {
    }

    override fun getLastVisitedTempSite(tempName: String): Flow<LaunchSite?> {
        return flowOf(null)
    }

    override fun getNewMarkerTempSite(tempName: String): Flow<LaunchSite?> {
        return flowOf(null)
    }

    override suspend fun checkIfSiteExists(name: String): LaunchSite? {
        return null
    }

    override fun getAllLaunchSiteNames(): Flow<List<String>> {
        return flowOf(emptyList())
    }

    override suspend fun updateElevation(uid: Int, elevation: Double) {
    }
}

class FakeGribDataDAO : GribDataDAO {
    override suspend fun insert(gribFile: GribData) {
    }

    override suspend fun getByTimestamp(timestamp: String): GribData? {
        return null
    }

    override suspend fun clearAll() {
    }
}

class FakeGribUpdatedDAO : GribUpdatedDAO {
    override suspend fun insert(vararg gribUpdated: GribUpdated) {
    }

    override suspend fun delete() {
    }

    override suspend fun getUpdated(): String? {
        return null
    }
}
