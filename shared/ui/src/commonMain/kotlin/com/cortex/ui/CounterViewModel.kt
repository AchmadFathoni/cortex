package com.cortex.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cortex.core.Counter
import com.cortex.core.CounterRepo
import com.cortex.core.WidgetBackgroundController
import com.cortex.core.nowMillis
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.koin.core.annotation.KoinViewModel
import org.koin.core.annotation.Provided

@OptIn(ExperimentalCoroutinesApi::class)
@KoinViewModel
class CounterViewModel(
    @Provided private val repo: CounterRepo,
    @Provided private val backgroundController: WidgetBackgroundController,
) : ViewModel() {
    private val counter = MutableStateFlow<Counter?>(null)
    private val loading = MutableStateFlow(true)

    private val clockFlow = counter.flatMapLatest { current ->
        if (current == null) {
            flowOf(nowMillis())
        } else {
            flow {
                while (true) {
                    emit(nowMillis())
                    delay(16)
                }
            }
        }
    }

    val state = combine(counter, loading, clockFlow) { current, isLoading, now ->
        CounterUiState(
            counter = current,
            nowMillis = now,
            isLoading = isLoading,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = CounterUiState(),
    )

    init {
        viewModelScope.launch {
            repo.observeCounter().collect { current ->
                counter.value = current
                loading.value = false
            }
        }
    }

    fun onAction(action: CounterAction) {
        when (action) {
            is CounterAction.Start -> {
                if (action.title.isNotBlank()) {
                    viewModelScope.launch {
                        repo.startCounter(action.title, action.startEpochMillis)
                    }
                }
            }

            is CounterAction.UpdateTitle -> {
                if (action.title.isNotBlank()) {
                    viewModelScope.launch { repo.updateTitle(action.title) }
                }
            }

            is CounterAction.Reset -> viewModelScope.launch {
                repo.reset(action.startEpochMillis)
            }

            CounterAction.ChooseBackground -> backgroundController.chooseBackground()
            CounterAction.ClearBackground -> backgroundController.clearBackground()
        }
    }
}

data class CounterUiState(
    val counter: Counter? = null,
    val nowMillis: Long = 0,
    val isLoading: Boolean = true,
)

sealed interface CounterAction {
    data class Start(val title: String, val startEpochMillis: Long) : CounterAction
    data class UpdateTitle(val title: String) : CounterAction
    data class Reset(val startEpochMillis: Long) : CounterAction
    data object ChooseBackground : CounterAction
    data object ClearBackground : CounterAction
}
