package no.uio.ifi.in2000.met2025.data.local.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

/**
 * LaunchSiteDAO
 * Defines CRUD (create, read, update, delete) operations and queries for LaunchSite entities.
 * Includes special handling for placeholder sites ("Last Visited" and "New Marker")
 * and a lookup by exact coordinates excluding those placeholders.
 */

@Dao
interface LaunchSiteDAO {
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(sites: LaunchSite)

    @Delete
    suspend fun delete(site: LaunchSite)

    @Query("SELECT * FROM LaunchSite ORDER BY name")
    fun findAll(): Flow<List<LaunchSite>>

    @Query("SELECT * FROM LaunchSite WHERE uid = :id LIMIT 1")
    suspend fun findSiteById(id: Int): LaunchSite?

    @Query("SELECT * FROM LaunchSite WHERE name = :name LIMIT 1")
    suspend fun findSiteByName(name: String): LaunchSite?

    @Update
    suspend fun update(sites: LaunchSite)

    // Existing temporary site (Last Visited)
    @Query("SELECT * FROM LaunchSite WHERE name = :tempName LIMIT 1")
    fun findLastVisitedTempSite(tempName: String = "Last Visited"): Flow<LaunchSite?>

    // New temporary site (New Marker)
    @Query("SELECT * FROM LaunchSite WHERE name = :tempName LIMIT 1")
    fun findNewMarkerTempSite(tempName: String = "New Marker"): Flow<LaunchSite?>

    @Query("SELECT * FROM LaunchSite WHERE name = :name LIMIT 1")
    suspend fun checkIfSiteExists(name: String): LaunchSite?

    @Query("SELECT name FROM LaunchSite ORDER BY name")
    fun findAllLaunchSiteNames(): Flow<List<String>>

    @Query("UPDATE LaunchSite SET elevation = :elevation WHERE uid = :uid")
    suspend fun updateElevation(uid: Int, elevation: Double)

    /** Return the “real” site matching these coords,
     *  ignoring both placeholder rows. */
    @Query(
        """
    SELECT * FROM LaunchSite
    WHERE latitude  = :lat
      AND longitude = :lon
      AND name NOT IN ('Last Visited')
    LIMIT 1
  """
    )
    suspend fun findSiteByCoordinates(lat: Double, lon: Double): LaunchSite?
}

/**
 * GribDataDAO
 * Defines CRUD (create, read, update, delete) operations and queries for GribData entities.
 */
@Dao
interface GribDataDAO {
    @Insert
    suspend fun insert(gribFile: GribData)

    @Query("SELECT * FROM grib_files WHERE timestamp = :timestamp LIMIT 1")
    suspend fun findByTimestamp(timestamp: String): GribData?

    @Query("DELETE FROM grib_files")
    suspend fun clearAll()
}

/**
 * GribUpdatedDAO
 * Defines CRUD (create, read, update, delete) operations and queries for GribUpdated entities.
 */
@Dao
interface GribUpdatedDAO {
    @Insert
    suspend fun insert(vararg gribUpdated: GribUpdated)

    @Query("DELETE FROM GribUpdated")
    suspend fun delete()

    @Query("SELECT time FROM GribUpdated LIMIT 1")
    suspend fun findUpdated(): String?
}

/**
 * WeatherConfigDao
 * Defines CRUD operations and queries for WeatherConfig entities.
 */
@Dao
interface WeatherConfigDao {
    /** OnConflictStrategy.ABORT added as an extra safety layer
    * to avoid duplicate names in the database.
    */
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertWeatherConfig(cfg: WeatherConfig)

    @Update
    suspend fun updateWeatherConfig(cfg: WeatherConfig)

    @Delete
    suspend fun deleteWeatherConfig(cfg: WeatherConfig)

    @Query("SELECT * FROM weather_config ORDER BY name")
    fun findAllWeatherConfigs(): Flow<List<WeatherConfig>>

    @Query("SELECT * FROM weather_config WHERE is_default = 1 LIMIT 1")
    fun findDefaultWeatherConfig(): Flow<WeatherConfig?>

    @Query("SELECT * FROM weather_config WHERE id = :weatherId LIMIT 1")
    fun findWeatherConfig(weatherId: Int): Flow<WeatherConfig?>

    /**
     * Using ORDER BY name for simpler indexing of weather configs
     * in the UI.
     */
    @Query("SELECT name FROM weather_config ORDER BY name")
    fun findAllWeatherConfigNames(): Flow<List<String>>
}

/**
 * RocketConfigDao
 * Defines CRUD (create, read, update, delete) operations and queries for RocketConfig entities.
 */

@Dao
interface RocketConfigDao {

    /**
     * Using ORDER BY name for simpler indexing of rocket configs
     * in the UI.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRocketConfig(rc: RocketConfig)

    @Update
    suspend fun updateRocketConfig(rc: RocketConfig)

    @Delete
    suspend fun deleteRocketConfig(rc: RocketConfig)

    @Query("SELECT * FROM rocket_config ORDER BY name")
    fun findAllRocketConfigs(): Flow<List<RocketConfig>>

    @Query("SELECT name FROM rocket_config")
    fun findAllRocketConfigNames(): Flow<List<String>>

    @Query("SELECT * FROM rocket_config WHERE id = :rocketId LIMIT 1")
    fun findRocketConfig(rocketId: Int): Flow<RocketConfig?>

    @Query("SELECT * FROM rocket_config WHERE is_default = 1 LIMIT 1")
    fun findDefaultRocketConfig(): Flow<RocketConfig?>

    @Query("SELECT * FROM rocket_config WHERE name = :name LIMIT 1")
    fun findRocketConfigByName(name: String): Flow<RocketConfig?>

    @Query("UPDATE rocket_config SET is_default = 0")
    suspend fun clearDefaultFlags()

    @Query("UPDATE rocket_config SET is_default = 1 WHERE id = :rocketId")
    suspend fun setDefaultFlag(rocketId: Int)

    /**
     * Set a rocket config as default, clearing all other flags to avoid
     * multiple default configs.
     */
    @Transaction
    suspend fun setDefaultRocketConfig(rocketId: Int) {
        clearDefaultFlags()
        setDefaultFlag(rocketId)
    }
}