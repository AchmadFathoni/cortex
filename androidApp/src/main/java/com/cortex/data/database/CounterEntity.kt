package com.cortex.data.database

import androidx.room3.Entity
import androidx.room3.PrimaryKey

@Entity(tableName = "counter")
data class CounterEntity(
    @PrimaryKey val id: Int = 1,
    val title: String,
    val startEpochMillis: Long,
)
