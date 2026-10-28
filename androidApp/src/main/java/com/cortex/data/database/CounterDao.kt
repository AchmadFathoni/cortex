package com.cortex.data.database

import androidx.room3.Dao
import androidx.room3.Query
import androidx.room3.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface CounterDao {
    @Query("SELECT * FROM counter WHERE id = 1")
    fun observeCounter(): Flow<CounterEntity?>

    @Query("SELECT * FROM counter WHERE id = 1")
    suspend fun getCounter(): CounterEntity?

    @Upsert
    suspend fun upsert(counter: CounterEntity)
}
