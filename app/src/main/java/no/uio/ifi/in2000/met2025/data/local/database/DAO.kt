package no.uio.ifi.in2000.met2025.data.local.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import kotlinx.coroutines.flow.Flow


@Dao
interface LaunchSiteDAO {
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(sites: LaunchSite)

    @Delete
    suspend fun delete(site: LaunchSite)

    @Query("SELECT * FROM LaunchSite")
    fun getAll(): Flow<List<LaunchSite>>

    @Query("SELECT * FROM LaunchSite WHERE uid = :id LIMIT 1")
    suspend fun getSiteById(id: Int): LaunchSite?

    @Update
    suspend fun update(sites: LaunchSite)

    // Existing temporary site (Last Visited)
    @Query("SELECT * FROM LaunchSite WHERE name = :tempName LIMIT 1")
    fun getLastVisitedTempSite(tempName: String = "Last Visited"): Flow<LaunchSite?>

    // New temporary site (New Marker)
    @Query("SELECT * FROM LaunchSite WHERE name = :tempName LIMIT 1")
    fun getNewMarkerTempSite(tempName: String = "New Marker"): Flow<LaunchSite?>

    @Query("SELECT * FROM LaunchSite WHERE name = :name LIMIT 1")
    suspend fun checkIfSiteExists(name: String): LaunchSite?

    @Query("SELECT name FROM LaunchSite")
    fun getAllLaunchSiteNames(): Flow<List<String>>

    @Query("UPDATE LaunchSite SET elevation = :elevation WHERE uid = :uid")
    suspend fun updateElevation(uid: Int, elevation: Double)

    /** Return the “real” site matching these coords,
     *  ignoring both placeholder rows. */
    @Query(
        """
    SELECT * FROM LaunchSite
    WHERE latitude  = :lat
      AND longitude = :lon
      AND name NOT IN ('Last Visited', 'New Marker')
    LIMIT 1
  """
    )
    suspend fun getSiteByCoordinates(lat: Double, lon: Double): LaunchSite?
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

    @Insert(onConflict = OnConflictStrategy.ABORT)
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

    @Query("SELECT name FROM config_profiles")
    fun getAllConfigProfileNames(): Flow<List<String>>
}

@Dao
interface RocketConfigDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertRocketConfig(rocketConfig: RocketConfig): Long

    @Update
    suspend fun updateRocketConfig(rocketConfig: RocketConfig)

    @Delete
    suspend fun deleteRocketConfig(rocketConfig: RocketConfig)

    @Query("SELECT * FROM rocket_configurations ORDER BY is_default DESC, name ASC")
    fun getAllRocketConfigs(): Flow<List<RocketConfig>>

    @Query("SELECT * FROM rocket_configurations WHERE is_default = 1 LIMIT 1")
    fun getDefaultRocketConfig(): Flow<RocketConfig?>

    @Query("SELECT * FROM rocket_configurations WHERE id = :rocketId LIMIT 1")
    fun getRocketConfig(rocketId: Int): Flow<RocketConfig?>

    @Query("SELECT * FROM rocket_configurations WHERE name = :name LIMIT 1")
    fun getRocketConfigByName(name: String): Flow<RocketConfig?>

    @Query("UPDATE rocket_configurations SET is_default = 0")
    suspend fun clearDefaultFlags()

    @Query("UPDATE rocket_configurations SET is_default = 1 WHERE id = :rocketId")
    suspend fun setDefaultFlag(rocketId: Int)

    @Transaction
    suspend fun setDefaultRocketConfig(rocketId: Int) {
        clearDefaultFlags()
        setDefaultFlag(rocketId)
    }
}