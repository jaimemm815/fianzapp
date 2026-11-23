package com.example.fianzapp.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

/**
 * The one database of the app (S7 Guide, section 8). Screens and the A7 worker all get it from
 * [getInstance], never build a second copy.
 *
 * Raise [version] whenever a table changes. fallbackToDestructiveMigration empties the tables
 * instead of crashing, which is fine for a class project.
 */
@Database(
    entities = [LeaseEntity::class, RoomEntity::class, PhotoEntity::class, ReportEntity::class],
    version = 1,
    exportSchema = false
)
abstract class FianzappDatabase : RoomDatabase() {
    abstract fun leaseDao(): LeaseDao
    abstract fun roomDao(): RoomDao
    abstract fun photoDao(): PhotoDao
    abstract fun reportDao(): ReportDao

    companion object {
        @Volatile
        private var instance: FianzappDatabase? = null

        fun getInstance(context: Context): FianzappDatabase =
            instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    FianzappDatabase::class.java,
                    "fianzapp.db"
                )
                    .fallbackToDestructiveMigration(dropAllTables = true)
                    .build()
                    .also { instance = it }
            }
    }
}
