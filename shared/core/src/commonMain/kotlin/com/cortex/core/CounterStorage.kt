package com.cortex.core

import kotlinx.coroutines.flow.Flow

interface CounterStorage {
    fun observeCounter(): Flow<Counter?>
    suspend fun getCounter(): Counter?
    suspend fun saveCounter(counter: Counter)
}
