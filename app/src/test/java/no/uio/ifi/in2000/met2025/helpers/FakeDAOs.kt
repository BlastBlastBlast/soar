package no.uio.ifi.in2000.met2025.fakes

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import no.uio.ifi.in2000.met2025.data.local.database.WeatherConfig
import no.uio.ifi.in2000.met2025.data.local.database.WeatherConfigDao
import no.uio.ifi.in2000.met2025.data.local.database.GribData
import no.uio.ifi.in2000.met2025.data.local.database.GribDataDAO
import no.uio.ifi.in2000.met2025.data.local.database.GribUpdated
import no.uio.ifi.in2000.met2025.data.local.database.GribUpdatedDAO
import no.uio.ifi.in2000.met2025.data.local.database.LaunchSite
import no.uio.ifi.in2000.met2025.data.local.database.LaunchSiteDAO

class FakeWeatherConfigDao : WeatherConfigDao {
    // Implement only needed DAO functions with dummy returns
    override suspend fun insertWeatherConfig(weatherConfig: WeatherConfig) {
    }

    override suspend fun updateWeatherConfig(weatherConfig: WeatherConfig) {
    }

    override suspend fun deleteWeatherConfig(weatherConfig: WeatherConfig) {
    }

    override fun findAllWeatherConfigs(): Flow<List<WeatherConfig>> {
        return flowOf(emptyList())
    }

    override fun findDefaultWeatherConfig(): Flow<WeatherConfig?> {
        return flowOf(null)
    }

    override fun findWeatherConfig(configId: Int): Flow<WeatherConfig?> {
        return flowOf(null)
    }

    override fun findAllWeatherConfigNames(): Flow<List<String>> {
        return flowOf(emptyList())
    }
}

class FakeLaunchSiteDAO : LaunchSiteDAO {
    override suspend fun insert(sites: LaunchSite) {
    }

    override suspend fun delete(site: LaunchSite) {
    }

    override fun findAll(): Flow<List<LaunchSite>> {
        return flowOf(emptyList())
    }

    override suspend fun findSiteByName(name: String): LaunchSite? {
        return null
    }

    override suspend fun findSiteById(id: Int): LaunchSite? {
        return null
    }

    override suspend fun findSiteByCoordinates(lat: Double, lon: Double): LaunchSite? {
        return null
    }

    override suspend fun update(sites: LaunchSite) {
    }

    override fun findLastVisitedTempSite(tempName: String): Flow<LaunchSite?> {
        return flowOf(null)
    }

    override fun findNewMarkerTempSite(tempName: String): Flow<LaunchSite?> {
        return flowOf(null)
    }

    override suspend fun checkIfSiteExists(name: String): LaunchSite? {
        return null
    }

    override fun findAllLaunchSiteNames(): Flow<List<String>> {
        return flowOf(emptyList())
    }

    override suspend fun updateElevation(uid: Int, elevation: Double) {
    }
}

class FakeGribDataDAO : GribDataDAO {
    override suspend fun insert(gribFile: GribData) {
    }

    override suspend fun findByTimestamp(timestamp: String): GribData? {
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

    override suspend fun findUpdated(): String? {
        return null
    }

    override suspend fun findLatest(): String? {
        return null
    }
}
