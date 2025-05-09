package no.uio.ifi.in2000.met2025.fakes

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import no.uio.ifi.in2000.met2025.data.local.database.WeatherConfig
import no.uio.ifi.in2000.met2025.data.local.database.ConfigProfileDAO
import no.uio.ifi.in2000.met2025.data.local.database.GribData
import no.uio.ifi.in2000.met2025.data.local.database.GribDataDAO
import no.uio.ifi.in2000.met2025.data.local.database.GribUpdated
import no.uio.ifi.in2000.met2025.data.local.database.GribUpdatedDAO
import no.uio.ifi.in2000.met2025.data.local.database.LaunchSite
import no.uio.ifi.in2000.met2025.data.local.database.LaunchSiteDAO

class FakeConfigProfileDao : ConfigProfileDAO {
    // Implement only needed DAO functions with dummy returns
    override suspend fun insertWeatherConfig(weatherConfig: WeatherConfig) {
    }

    override suspend fun updateWeatherConfig(weatherConfig: WeatherConfig) {
    }

    override suspend fun deleteWeatherConfig(weatherConfig: WeatherConfig) {
    }

    override fun getAllWeatherConfigs(): Flow<List<WeatherConfig>> {
        return flowOf(emptyList())
    }

    override fun getDefaultWeatherConfig(): Flow<WeatherConfig?> {
        return flowOf(null)
    }

    override fun getWeatherConfig(configId: Int): Flow<WeatherConfig?> {
        return flowOf(null)
    }

    override fun getAllWeatherConfigNames(): Flow<List<String>> {
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
