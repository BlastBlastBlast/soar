package no.uio.ifi.in2000.met2025.data.local.Database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow


@Dao
interface LaunchSiteDAO {
    @Insert
    suspend fun insertAll(vararg sites: LaunchSite)

    @Delete
    suspend fun delete(site: LaunchSite)

    @Query("SELECT * FROM LaunchSite")
    fun getAll(): Flow<List<LaunchSite>>

    @Update
    suspend fun updateSites(vararg sites : LaunchSite)
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

    @Delete
    suspend fun delete(vararg gribUpdated: GribUpdated)

    @Query("SELECT * FROM GribUpdated LIMIT 1")
    suspend fun getUpdated(): String?

}