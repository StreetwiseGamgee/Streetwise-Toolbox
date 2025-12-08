package com.cturner56.cooperative_demo_3.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.cturner56.cooperative_demo_3.api.model.GithubRepository
import com.cturner56.cooperative_demo_3.api.model.RepositoryReleaseVersion

/**
 * Primary database for application, built using Room-db.
 * The abstract class acts as the access point for repository data which is stored locally.
 * Referenced implementation via in-class demo: MovieHubFall2025
 */
@Database(
    entities = [GithubRepository::class, RepositoryReleaseVersion::class],
    version = 1,
    exportSchema = false)
abstract class AppDatabase : RoomDatabase() {

    abstract fun githubDao(): GithubDao
    companion object {

    @Volatile // Prevents potential race condition.
    private var INSTANCE: AppDatabase? = null

        /**
         * A function which gets a singleton instance of the [AppDatabase].
         *
         * If an instance is present, it's returned.
         * Otherwise, an instance of the database is created.
         *
         * @param context The application context used by Room in locating the database file.
         * @return The singleton [AppDatabase] instance.
         */
        fun getInstance(context: Context): AppDatabase{
            return INSTANCE ?: synchronized(this){
                val instance  = Room.databaseBuilder(
                    context = context.applicationContext,
                    AppDatabase::class.java,
                    "Local Repository Data"
                ).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                instance
            }
        }
    }
}