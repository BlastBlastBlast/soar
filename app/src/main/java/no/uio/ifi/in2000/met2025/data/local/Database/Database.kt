package no.uio.ifi.in2000.met2025.data.local.Database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.RoomDatabase
import androidx.room.Database

@Database(entities = [LaunchSite::class, GribData::class, GribUpdated::class, ConfigProfile::class], version = 3)
abstract class AppDatabase : RoomDatabase() {
    abstract fun launchSiteDao(): LaunchSiteDAO
    abstract fun gribDataDao(): GribDataDAO
    abstract fun gribUpdatedDao(): GribUpdatedDAO
    abstract fun configProfileDao(): ConfigProfileDAO
}

@Entity
data class LaunchSite(
    @PrimaryKey(autoGenerate = true) val uid: Int = 0 ,
    @ColumnInfo(name = "latitude") val latitude: Double,
    @ColumnInfo(name = "longitude") val longitude: Double,
    @ColumnInfo(name = "name") val name: String
)

@Entity(tableName = "grib_files")
data class GribData(
    @PrimaryKey val timestamp: String,
    val data: ByteArray,
) {
    //IDE generert oppsett for korrekt funksjon av ByteArray i database
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as GribData

        return timestamp == other.timestamp
    }

    override fun hashCode(): Int {
        return timestamp.hashCode()
    }
}

@Entity
data class GribUpdated(
    @PrimaryKey() val time: String,
)


/*
@Entity
data class GribData(
    @PrimaryKey() val time: Instant,
    @ColumnInfo(name = "GribDataMap") val gribDataMap: GribDataMap,
)
 */

@Entity(tableName = "config_profiles")
data class ConfigProfile(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    @ColumnInfo(name = "ground_wind_threshold") val groundWindThreshold: Double = 8.6,
    @ColumnInfo(name = "air_wind_threshold") val airWindThreshold: Double = 17.2,
    @ColumnInfo(name = "humidity_threshold") val humidityThreshold: Double = 75.0,
    @ColumnInfo(name = "dew_point_threshold") val dewPointThreshold: Double = 15.0,
    @ColumnInfo(name = "cloud_cover_threshold") val cloudCoverThreshold: Double = 15.0,
    @ColumnInfo(name = "is_enabled_cloud_cover") val isEnabledCloudCover: Boolean = true,
    @ColumnInfo(name = "cloud_cover_high_threshold") val cloudCoverHighThreshold: Double = 15.0,
    @ColumnInfo(name = "cloud_cover_medium_threshold") val cloudCoverMediumThreshold: Double = 15.0,
    @ColumnInfo(name = "cloud_cover_low_threshold") val cloudCoverLowThreshold: Double = 15.0,
    @ColumnInfo(name = "is_enabled_cloud_cover_high") val isEnabledCloudCoverHigh: Boolean = true,
    @ColumnInfo(name = "is_enabled_cloud_cover_medium") val isEnabledCloudCoverMedium: Boolean = true,
    @ColumnInfo(name = "is_enabled_cloud_cover_low") val isEnabledCloudCoverLow: Boolean = true,
    @ColumnInfo(name = "is_enabled_ground_wind") val isEnabledGroundWind: Boolean = true,
    @ColumnInfo(name = "is_enabled_air_wind") val isEnabledAirWind: Boolean = true,
    @ColumnInfo(name = "is_enabled_humidity") val isEnabledHumidity: Boolean = true,
    @ColumnInfo(name = "is_enabled_dew_point") val isEnabledDewPoint: Boolean = true,
    @ColumnInfo(name = "is_default") val isDefault: Boolean = false
)

