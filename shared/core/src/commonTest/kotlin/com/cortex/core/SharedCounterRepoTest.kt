package com.cortex.core

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

class SharedCounterRepoTest {
    private class FakeStorage(initial: Counter? = null) : CounterStorage {
        val state = MutableStateFlow(initial)

        override fun observeCounter(): Flow<Counter?> = state

        override suspend fun getCounter(): Counter? = state.value

        override suspend fun saveCounter(counter: Counter) {
            state.value = counter
        }
    }

    private class FakeWidgetUpdater : WidgetUpdater {
        var calls = 0

        override fun notifyDataChanged() {
            calls++
        }
    }

    @Test
    fun startCapturesCurrentClockAndRefreshesWidget() = runTest {
        val storage = FakeStorage()
        val widget = FakeWidgetUpdater()
        val repo = SharedCounterRepo(storage, widget, clock = { 12_345L })

        repo.startCounter("  My recovery  ")

        assertEquals(Counter("My recovery", 12_345L), storage.state.value)
        assertEquals(1, widget.calls)
    }

    @Test
    fun resetCapturesNewClockAndKeepsTitle() = runTest {
        val storage = FakeStorage(Counter("Sober", 1_000L))
        val widget = FakeWidgetUpdater()
        var now = 50_000L
        val repo = SharedCounterRepo(storage, widget, clock = { now })

        repo.reset()
        assertEquals(Counter("Sober", 50_000L), storage.state.value)

        now = 90_000L
        repo.reset()
        assertEquals(Counter("Sober", 90_000L), storage.state.value)
    }

    @Test
    fun updateTitleIgnoresBlankTitle() = runTest {
        val storage = FakeStorage(Counter("Sober", 1_000L))
        val repo = SharedCounterRepo(storage, FakeWidgetUpdater(), clock = { 2_000L })

        repo.updateTitle("   ")
        assertEquals(Counter("Sober", 1_000L), storage.state.value)

        repo.updateTitle("  New name ")
        assertEquals(Counter("New name", 1_000L), storage.state.value)
    }
}
