package no.uio.ifi.in2000.met2025.data.local.Database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.RoomDatabase
import androidx.room.Database
import no.uio.ifi.in2000.met2025.data.models.GribDataMap
import java.time.Instant


@Entity
data class LaunchSite(
    @PrimaryKey(autoGenerate = true) val uid: Int = 0 ,
    @ColumnInfo(name = "latitude") val latitude: Double,
    @ColumnInfo(name = "longitude") val longitude: Double,
    @ColumnInfo(name = "name") val name: String
)

/*
dataklassen holder på verdiene til oppskytningspunkt
gir også bruker mulighet til å gi et navn til oppskytningspunktet
*/

@Entity
data class GribData(
    @PrimaryKey() val time: Instant,
    @ColumnInfo(name = "GribDataMap") val gribDataMap: GribDataMap,
)

@Entity
data class GribUpdated(
    @PrimaryKey() val time: Instant,
)



@Database(entities = [LaunchSite::class, GribData::class, GribUpdated::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun launchSiteDao(): LaunchSiteDAO
    abstract fun gribDataDao(): GribDataDAO
    abstract fun gribUpdatedDao(): GribUpdatedDAO
}
