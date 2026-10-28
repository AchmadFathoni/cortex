package com.cortex.widget

import android.content.Context
import androidx.room3.Room
import com.cortex.data.database.AppDatabase

@Volatile
private var dbInstance: AppDatabase? = null

@Synchronized
fun getDatabase(context: Context): AppDatabase {
    return dbInstance ?: Room.databaseBuilder(
        context.applicationContext,
        AppDatabase::class.java,
        AppDatabase.DB_NAME,
    ).build().also { dbInstance = it }
}
