package com.cortex.data.storage

import com.cortex.core.Counter
import com.cortex.core.CounterStorage
import com.cortex.data.database.CounterDao
import com.cortex.data.database.CounterEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class RoomCounterStorage(private val dao: CounterDao) : CounterStorage {
    override fun observeCounter(): Flow<Counter?> =
        dao.observeCounter().map { it?.toDomain() }

    override suspend fun getCounter(): Counter? = dao.getCounter()?.toDomain()

    override suspend fun saveCounter(counter: Counter) {
        dao.upsert(counter.toEntity())
    }
}

private fun CounterEntity.toDomain() = Counter(
    title = title,
    startEpochMillis = startEpochMillis,
)

private fun Counter.toEntity() = CounterEntity(
    title = title,
    startEpochMillis = startEpochMillis,
)
