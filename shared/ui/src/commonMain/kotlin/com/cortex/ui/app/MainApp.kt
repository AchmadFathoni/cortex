package com.cortex.ui.app

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cortex.core.Counter
import com.cortex.core.ElapsedBreakdown
import com.cortex.core.elapsedBreakdown
import com.cortex.core.nowMillis
import com.cortex.ui.CounterAction
import com.cortex.ui.CounterUiState
import com.cortex.ui.CounterViewModel
import kotlin.time.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun MainApp() {
    val viewModel = koinViewModel<CounterViewModel>()
    val state by viewModel.state.collectAsState()
    CortexTheme {
        CounterScreen(state = state, onAction = viewModel::onAction)
    }
}

@Composable
private fun CounterScreen(
    state: CounterUiState,
    onAction: (CounterAction) -> Unit,
) {
    val counter = state.counter
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background,
    ) {
        Box(
            modifier = Modifier.fillMaxSize().padding(24.dp),
            contentAlignment = Alignment.Center,
        ) {
            when {
                state.isLoading -> CircularProgressIndicator(color = CortexAccent)
                counter == null -> StartCounter(
                    onStart = { onAction(CounterAction.Start(it)) },
                )
                else -> ActiveCounter(
                    counter = counter,
                    nowMillis = state.nowMillis,
                    onAction = onAction,
                )
            }
        }
    }
}

@Composable
private fun StartCounter(onStart: (String) -> Unit) {
    var title by rememberSaveable { mutableStateOf("My recovery") }
    val startsAt = remember { formatTimestamp(nowMillis()) }

    Column(
        modifier = Modifier.fillMaxWidth().widthIn(max = 460.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(18.dp),
    ) {
        Text(
            text = "CORTEX",
            color = CortexAccent,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 4.sp,
        )
        Text(
            text = "Recovery Counter",
            color = CortexText,
            fontSize = 30.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
        )
        Text(
            text = "Give this chapter a name. Your count starts from now.",
            color = CortexMuted,
            fontSize = 15.sp,
            textAlign = TextAlign.Center,
        )
        OutlinedTextField(
            value = title,
            onValueChange = { title = it },
            label = { Text("Title") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
        )
        Text(
            text = "Starts $startsAt",
            color = CortexMuted,
            fontSize = 13.sp,
        )
        Button(
            onClick = { onStart(title.trim()) },
            enabled = title.isNotBlank(),
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text("Start counting", fontWeight = FontWeight.Bold)
        }
        Text(
            text = "Everything stays on this device.",
            color = CortexMuted,
            fontSize = 12.sp,
        )
    }
}

@Composable
private fun ActiveCounter(
    counter: Counter,
    nowMillis: Long,
    onAction: (CounterAction) -> Unit,
) {
    val breakdown = elapsedBreakdown(counter.startEpochMillis, nowMillis)
    val startedAt = remember(counter.startEpochMillis) {
        formatTimestamp(counter.startEpochMillis)
    }
    var showEdit by rememberSaveable { mutableStateOf(false) }
    var showReset by rememberSaveable { mutableStateOf(false) }

    Column(
        modifier = Modifier.fillMaxWidth().widthIn(max = 520.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = "RECOVERY COUNTER",
            color = CortexAccent,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 4.sp,
        )
        Spacer(Modifier.height(10.dp))
        Text(
            text = counter.title,
            color = CortexText,
            fontSize = 30.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            maxLines = 2,
        )
        Spacer(Modifier.height(6.dp))
        Text(
            text = "Started $startedAt",
            color = CortexMuted,
            fontSize = 13.sp,
        )
        Spacer(Modifier.height(34.dp))
        CounterDigits(breakdown)
        Spacer(Modifier.height(22.dp))
        SecondProgress(breakdown)
        Spacer(Modifier.height(36.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            OutlinedButton(onClick = { showEdit = true }) {
                Text("Edit title")
            }
            OutlinedButton(
                onClick = { showReset = true },
                colors = ButtonDefaults.outlinedButtonColors(contentColor = CortexDanger),
            ) {
                Text("Reset start")
            }
        }
    }

    if (showEdit) {
        EditTitleDialog(
            initialTitle = counter.title,
            onDismiss = { showEdit = false },
            onSave = {
                onAction(CounterAction.UpdateTitle(it))
                showEdit = false
            },
        )
    }

    if (showReset) {
        ResetDialog(
            onDismiss = { showReset = false },
            onConfirm = {
                onAction(CounterAction.Reset)
                showReset = false
            },
        )
    }
}

@Composable
private fun CounterDigits(breakdown: ElapsedBreakdown) {
    val description =
        "${breakdown.days} days, ${breakdown.hours} hours, " +
            "${breakdown.minutes} minutes, ${breakdown.seconds} seconds, " +
            "${breakdown.centiseconds} centiseconds"

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.semantics { contentDescription = description },
    ) {
        Row(verticalAlignment = Alignment.Bottom) {
            Text(
                text = breakdown.days.toString(),
                color = CortexAccent,
                fontSize = 72.sp,
                lineHeight = 72.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = if (breakdown.days == 1L) "DAY" else "DAYS",
                color = CortexMuted,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 2.sp,
                modifier = Modifier.padding(start = 10.dp, bottom = 12.dp),
            )
        }
        Spacer(Modifier.height(4.dp))
        Row(verticalAlignment = Alignment.Bottom) {
            Text(
                text = "${breakdown.hours.pad2()}:${breakdown.minutes.pad2()}:${breakdown.seconds.pad2()}",
                color = CortexText,
                fontSize = 44.sp,
                lineHeight = 48.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                letterSpacing = 2.sp,
            )
            Text(
                text = ".${breakdown.centiseconds.pad2()}",
                color = CortexAccent,
                fontSize = 24.sp,
                lineHeight = 28.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(start = 6.dp, bottom = 4.dp),
            )
        }
    }
}

@Composable
private fun SecondProgress(breakdown: ElapsedBreakdown) {
    val fraction = breakdown.milliseconds / 1_000f
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(6.dp)
            .clip(RoundedCornerShape(3.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(fraction)
                .fillMaxHeight()
                .background(CortexAccent),
        )
    }
}

@Composable
private fun EditTitleDialog(
    initialTitle: String,
    onDismiss: () -> Unit,
    onSave: (String) -> Unit,
) {
    var title by remember(initialTitle) { mutableStateOf(initialTitle) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit title") },
        text = {
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Title") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )
        },
        confirmButton = {
            TextButton(
                onClick = { onSave(title.trim()) },
                enabled = title.isNotBlank(),
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        },
    )
}

@Composable
private fun ResetDialog(
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Reset start time?") },
        text = {
            Text("The counter will restart from now. Your current elapsed time will be lost.")
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text("Reset", color = CortexDanger)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        },
    )
}

private fun formatTimestamp(epochMillis: Long): String {
    val dateTime = Instant.fromEpochMilliseconds(epochMillis)
        .toLocalDateTime(TimeZone.currentSystemDefault())
    val date = dateTime.date
    val time = dateTime.time
    val month = (date.month.ordinal + 1).pad2()
    return "${date.year}-$month-${date.day.pad2()} ${time.hour.pad2()}:${time.minute.pad2()}"
}

private fun Int.pad2(): String = toString().padStart(2, '0')
