package com.cortex.core

import kotlinx.coroutines.flow.Flow

class SharedCounterRepo(
    private val storage: CounterStorage,
    private val widgetUpdater: WidgetUpdater,
    private val clock: () -> Long = ::nowMillis,
) : CounterRepo {
    override fun observeCounter(): Flow<Counter?> = storage.observeCounter()

    override suspend fun getCounter(): Counter? = storage.getCounter()

    override suspend fun startCounter(title: String): Counter {
        val trimmed = title.trim()
        require(trimmed.isNotEmpty()) { "Counter title must not be blank" }
        val counter = Counter(title = trimmed, startEpochMillis = clock())
        storage.saveCounter(counter)
        widgetUpdater.notifyDataChanged()
        return counter
    }

    override suspend fun updateTitle(title: String) {
        val trimmed = title.trim()
        if (trimmed.isEmpty()) return
        val current = storage.getCounter() ?: return
        storage.saveCounter(current.copy(title = trimmed))
        widgetUpdater.notifyDataChanged()
    }

    override suspend fun reset() {
        val current = storage.getCounter() ?: return
        storage.saveCounter(current.copy(startEpochMillis = clock()))
        widgetUpdater.notifyDataChanged()
    }
}
