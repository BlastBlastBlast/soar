package no.uio.ifi.in2000.met2025.data.local.Database

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

    @Query("SELECT * FROM LaunchSite WHERE name = :tempName LIMIT 1")
    fun getTempSite(tempName: String = "Last Visited"): Flow<LaunchSite?>
}
