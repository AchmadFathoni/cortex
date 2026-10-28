package com.cortex.desktop

import com.cortex.core.Counter
import com.cortex.core.CounterStorage
import java.io.File
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@Serializable
private data class StoredCounter(
    val title: String,
    val startEpochMillis: Long,
)

class JsonCounterStorage : CounterStorage {
    private val json = Json {
        prettyPrint = true
        ignoreUnknownKeys = true
    }
    private val file = File(File(System.getProperty("user.home"), ".cortex"), "counter.json")
    private val state = MutableStateFlow(load())

    override fun observeCounter(): Flow<Counter?> = state

    override suspend fun getCounter(): Counter? = state.value

    override suspend fun saveCounter(counter: Counter) {
        state.value = counter
        file.parentFile?.mkdirs()
        file.writeText(
            json.encodeToString(
                StoredCounter(
                    title = counter.title,
                    startEpochMillis = counter.startEpochMillis,
                ),
            ),
        )
    }

    private fun load(): Counter? {
        if (!file.exists()) return null
        return runCatching {
            val stored = json.decodeFromString<StoredCounter>(file.readText())
            Counter(stored.title, stored.startEpochMillis)
        }.getOrNull()
    }
}
