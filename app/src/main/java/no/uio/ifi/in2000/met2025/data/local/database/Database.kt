package no.uio.ifi.in2000.met2025.data.local.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.RoomDatabase
import androidx.room.Database
import androidx.room.Index
import no.uio.ifi.in2000.met2025.data.models.Angle
import java.sql.Types.NULL

@Database(
    entities = [LaunchSite::class, GribData::class, GribUpdated::class, ConfigProfile::class, RocketConfig::class],
    version = 9
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun launchSiteDao(): LaunchSiteDAO
    abstract fun gribDataDao(): GribDataDAO
    abstract fun gribUpdatedDao(): GribUpdatedDAO
    abstract fun configProfileDao(): ConfigProfileDAO
    abstract fun rocketConfigDao(): RocketConfigDao
}

@Entity(tableName = "LaunchSite", indices = [Index(value = ["name"], unique = true)])
data class LaunchSite(
    @PrimaryKey(autoGenerate = true) val uid: Int = 0,
    @ColumnInfo(name = "latitude") val latitude: Double,
    @ColumnInfo(name = "longitude") val longitude: Double,
    @ColumnInfo(name = "name") val name: String,
    @ColumnInfo(name = "elevation") val elevation: Double? = null
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

@Entity(tableName = "config_profiles")
data class ConfigProfile(
    @PrimaryKey(autoGenerate = true) val id: Int = 0, val name: String,
    @ColumnInfo(name = "ground_wind_threshold") val groundWindThreshold: Double = 8.6, // also threshold for windSpeedOfGust
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
    @ColumnInfo(name = "is_enabled_wind_direction") val isEnabledWindDirection: Boolean = true,
    @ColumnInfo(name = "fog_threshold") val fogThreshold: Double = 0.0,
    @ColumnInfo(name = "is_enabled_fog") val isEnabledFog: Boolean = true,
    @ColumnInfo(name = "precipitation_threshold") val precipitationThreshold: Double = 0.0,
    @ColumnInfo(name = "is_enabled_precipitation") val isEnabledPrecipitation: Boolean = true,
    @ColumnInfo(name = "probability_of_thunder_threshold") val probabilityOfThunderThreshold: Double = 0.0,
    @ColumnInfo(name = "is_enabled_probability_of_thunder") val isEnabledProbabilityOfThunder: Boolean = true,
    @ColumnInfo(name = "is_default") val isDefault: Boolean = false,
    @ColumnInfo(name = "altitude_upper_bound") val altitudeUpperBound: Double = 5000.0,
    @ColumnInfo(name = "is_enabled_altitude_upper_bound") val isEnabledAltitudeUpperBound: Boolean = true,
    @ColumnInfo(name = "wind_shear_speed_threshold") val windShearSpeedThreshold: Double = 24.5,
    @ColumnInfo(name = "is_enabled_wind_shear") val isEnabledWindShear: Boolean = true,
)

@Entity(
    tableName = "rocket_configurations",
    indices = [Index(value = ["name"], unique = true)]
)
data class RocketConfig(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val name: String,
    @ColumnInfo(name = "launch_azimuth")               val launchAzimuth: Double,
    @ColumnInfo(name = "launch_pitch")                 val launchPitch: Double,
    @ColumnInfo(name = "launch_rail_length")           val launchRailLength: Double,
    @ColumnInfo(name = "wet_mass")                     val wetMass: Double,
    @ColumnInfo(name = "dry_mass")                     val dryMass: Double,
    @ColumnInfo(name = "burn_time")                    val burnTime: Double,
    @ColumnInfo(name = "thrust")                       val thrust: Double,
    @ColumnInfo(name = "step_size")                    val stepSize: Double,
    @ColumnInfo(name = "cross_sectional_area")         val crossSectionalArea: Double,
    @ColumnInfo(name = "drag_coefficient")             val dragCoefficient: Double,
    @ColumnInfo(name = "parachute_cross_sectional_area") val parachuteCrossSectionalArea: Double,
    @ColumnInfo(name = "parachute_drag_coefficient")   val parachuteDragCoefficient: Double,
    @ColumnInfo(name = "is_default")                   val isDefault: Boolean = false
)

