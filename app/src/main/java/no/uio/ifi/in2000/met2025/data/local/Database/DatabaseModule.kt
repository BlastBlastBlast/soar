//package no.uio.ifi.in2000.met2025.data.local.Database
//
//import android.content.Context
//import androidx.room.Room
//import dagger.Module
//import dagger.Provides
//import dagger.hilt.InstallIn
//import dagger.hilt.components.SingletonComponent
//import javax.inject.Singleton
//
//
//@Module
//@InstallIn(SingletonComponent::class)
//object DatabaseModule {
//
//    @Provides
//    @Singleton
//    fun provideDatabase(appContext: Context): AppDatabase {
//        return Room.databaseBuilder(
//            appContext,
//            AppDatabase::class.java,
//            "launch_site_db"
//        ).build()
//    }
//
//    @Provides
//    fun provideLaunchSiteDao(db: AppDatabase): LaunchSiteDAO {
//        return db.launchSiteDao()
//    }
//}
//// Vi bruker Hilt til å håndtere Room-databasen som en singleton.
//// Dette gjør at vi slipper å opprette databasen manuelt med Room.databaseBuilder(...),
//// og Hilt sørger for at både databasen og DAO-en blir tilgjengelig overalt i appen.
