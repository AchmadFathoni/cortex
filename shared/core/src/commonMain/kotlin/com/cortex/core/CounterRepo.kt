package com.cortex.core

import kotlinx.coroutines.flow.Flow

interface CounterRepo {
    fun observeCounter(): Flow<Counter?>
    suspend fun getCounter(): Counter?
    suspend fun startCounter(title: String): Counter
    suspend fun updateTitle(title: String)
    suspend fun reset()
}
