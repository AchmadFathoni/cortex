package com.cortex.data.database

import androidx.room3.Database
import androidx.room3.RoomDatabase

@Database(
    entities = [CounterEntity::class],
    version = 1,
    exportSchema = false,
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun counterDao(): CounterDao

    companion object {
        const val DB_NAME = "cortex_database"
    }
}
