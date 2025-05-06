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
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(sites: LaunchSite)

    @Delete
    suspend fun delete(site: LaunchSite)

    @Query("SELECT * FROM LaunchSite")
    fun getAll(): Flow<List<LaunchSite>>

    @Query("SELECT * FROM LaunchSite WHERE name = :name LIMIT 1")
    suspend fun getSiteByName(name: String): LaunchSite?

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
    suspend fun insertConfigProfile(cfg: ConfigProfile)

    @Update
    suspend fun updateConfigProfile(cfg: ConfigProfile)

    @Delete
    suspend fun deleteConfigProfile(cfg: ConfigProfile)

    @Query("SELECT * FROM config_profiles ORDER BY name")
    fun getAllConfigProfiles(): Flow<List<ConfigProfile>>

    @Query("SELECT * FROM config_profiles WHERE is_default = 1 LIMIT 1")
    fun getDefaultConfigProfile(): Flow<ConfigProfile?>

    @Query("SELECT * FROM config_profiles WHERE id = :configId LIMIT 1")
    fun getConfigProfile(configId: Int): Flow<ConfigProfile?>

    /**
     * Room will map each row’s single “name” column into a String in the list.
     * Adding ORDER BY guarantees a stable sort if you care.
     */
    @Query("SELECT name FROM config_profiles ORDER BY name")
    fun getAllConfigProfileNames(): Flow<List<String>>
}

@Dao
interface RocketConfigDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRocketConfig(rc: RocketConfig)

    @Update
    suspend fun updateRocketConfig(rc: RocketConfig)

    @Delete
    suspend fun deleteRocketConfig(rc: RocketConfig)

    @Query("SELECT * FROM rocket_configurations ORDER BY name")
    fun getAllRocketConfigs(): Flow<List<RocketConfig>>

    @Query("SELECT name FROM rocket_configurations")
    fun getAllRocketConfigNames(): Flow<List<String>>

    @Query("SELECT * FROM rocket_configurations WHERE id = :rocketId LIMIT 1")
    fun getRocketConfig(rocketId: Int): Flow<RocketConfig?>

    @Query("SELECT * FROM rocket_configurations WHERE is_default = 1 LIMIT 1")
    fun getDefaultRocketConfig(): Flow<RocketConfig?>
}