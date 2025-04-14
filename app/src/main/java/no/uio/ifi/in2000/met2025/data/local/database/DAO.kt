package no.uio.ifi.in2000.met2025.data.local.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow


@Dao
interface LaunchSiteDAO {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(vararg sites: LaunchSite)

    @Delete
    suspend fun delete(site: LaunchSite)

    @Query("SELECT * FROM LaunchSite")
    fun getAll(): Flow<List<LaunchSite>>

    @Update
    suspend fun updateSites(vararg sites: LaunchSite)

    // Existing temporary site (Last Visited)
    @Query("SELECT * FROM LaunchSite WHERE name = :tempName LIMIT 1")
    fun getTempSite(tempName: String = "Last Visited"): Flow<LaunchSite?>

    // New temporary site (New Marker)
    @Query("SELECT * FROM LaunchSite WHERE name = :tempName LIMIT 1")
    fun getNewMarkerTempSite(tempName: String = "New Marker"): Flow<LaunchSite?>
}



@Dao
interface GribDataDAO {
    @Insert
    suspend fun insert(gribFile: GribData)

    @Query("SELECT * FROM grib_files WHERE timestamp = :timestamp LIMIT 1")
    suspend fun getByTimestamp(timestamp: String): GribData?

    @Query("DELETE FROM grib_files")
    suspend fun clearAll()
}

@Dao
interface GribUpdatedDAO {
    @Insert
    suspend fun insert(vararg gribUpdated: GribUpdated)

    @Query("DELETE FROM GribUpdated")
    suspend fun delete()

    @Query("SELECT * FROM GribUpdated LIMIT 1")
    suspend fun getUpdated(): String?

}

@Dao
interface ConfigProfileDAO {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertConfigProfile(configProfile: ConfigProfile)

    @Update
    suspend fun updateConfigProfile(configProfile: ConfigProfile)

    @Delete
    suspend fun deleteConfigProfile(configProfile: ConfigProfile)

    @Query("SELECT * FROM config_profiles")
    fun getAllConfigProfiles(): Flow<List<ConfigProfile>>

    @Query("SELECT * FROM config_profiles WHERE is_default = 1 LIMIT 1")
    fun getDefaultConfigProfile(): Flow<ConfigProfile?>

    @Query("SELECT * FROM config_profiles WHERE id = :configId LIMIT 1")
    fun getConfigProfile(configId: Int): Flow<ConfigProfile?>
}

@Dao
interface RocketSpecsDAO {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRocketSpecs(rocketSpecs: RocketParameters)

    @Update
    suspend fun updateRocketSpecs(rocketSpecs: RocketParameters)

    @Delete
    suspend fun deleteRocketSpecs(rocketSpecs: RocketParameters)

    @Query("SELECT * FROM rocket_parameters")
    fun getAllRocketSpecs(): Flow<List<RocketParameters>>

    @Query("SELECT * FROM rocket_parameters WHERE isDefault = 1 LIMIT 1")
    fun getDefaultRocketSpecs(): Flow<RocketParameters?>

    @Query("SELECT * FROM rocket_parameters WHERE id = :rocketId LIMIT 1")
    fun getRocketSpecs(rocketId: Int): Flow<RocketParameters?>
}