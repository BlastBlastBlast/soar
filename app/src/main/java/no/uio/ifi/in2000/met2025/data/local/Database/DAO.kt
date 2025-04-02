package no.uio.ifi.in2000.met2025.data.local.Database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow
import no.uio.ifi.in2000.met2025.data.models.GribDataMap
import java.time.Instant


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
    suspend fun insertAll(vararg gribData: GribData)

    @Query("DELETE FROM GribData")
    suspend fun deleteAll()

    @Query("SELECT gribDataMap FROM GribData WHERE time = :queryTime")
    suspend fun getGribData(queryTime: Instant): GribDataMap?
}

@Dao
interface GribUpdatedDAO {
    @Insert
    suspend fun insert(vararg gribUpdated: Instant)

    @Delete
    suspend fun delete(vararg gribUpdated: Instant)

    @Query("SELECT * FROM GribUpdated LIMIT 1")
    suspend fun getUpdated(): Instant?

}