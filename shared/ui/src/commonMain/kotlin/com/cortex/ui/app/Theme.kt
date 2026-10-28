package com.cortex.ui.app

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val CortexAccent = Color(0xFF00E5FF)
val CortexBackground = Color(0xFF07080A)
val CortexSurface = Color(0xFF0D1117)
val CortexText = Color(0xFFF2F5F7)
val CortexMuted = Color(0xFF8A8F98)
val CortexDanger = Color(0xFFFF5A67)

private val CortexColors = darkColorScheme(
    primary = CortexAccent,
    onPrimary = Color(0xFF001014),
    primaryContainer = Color(0xFF003640),
    onPrimaryContainer = Color(0xFF9DEEFF),
    background = CortexBackground,
    onBackground = CortexText,
    surface = CortexSurface,
    onSurface = CortexText,
    surfaceVariant = Color(0xFF151B23),
    onSurfaceVariant = CortexMuted,
    outline = Color(0xFF30363D),
    error = CortexDanger,
    onError = Color(0xFF1A0003),
)

@Composable
fun CortexTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = CortexColors,
        content = content,
    )
}
