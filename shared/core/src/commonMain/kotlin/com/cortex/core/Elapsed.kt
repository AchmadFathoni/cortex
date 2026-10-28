package com.cortex.core

const val DAY_MILLIS: Long = 86_400_000L
const val HOUR_MILLIS: Long = 3_600_000L

data class ElapsedBreakdown(
    val days: Long,
    val hours: Int,
    val minutes: Int,
    val seconds: Int,
    val milliseconds: Int,
) {
    val centiseconds: Int get() = milliseconds / 10

    companion object {
        val ZERO = ElapsedBreakdown(0, 0, 0, 0, 0)
    }
}

fun elapsedMillis(startMillis: Long, nowMillis: Long): Long =
    if (nowMillis <= startMillis) 0 else nowMillis - startMillis

fun elapsedBreakdown(startMillis: Long, nowMillis: Long): ElapsedBreakdown {
    val elapsed = elapsedMillis(startMillis, nowMillis)
    val days = elapsed / DAY_MILLIS
    val afterDays = elapsed % DAY_MILLIS
    val hours = (afterDays / HOUR_MILLIS).toInt()
    val afterHours = afterDays % HOUR_MILLIS
    val minutes = (afterHours / 60_000L).toInt()
    val afterMinutes = afterHours % 60_000L
    val seconds = (afterMinutes / 1_000L).toInt()
    val milliseconds = (afterMinutes % 1_000L).toInt()
    return ElapsedBreakdown(days, hours, minutes, seconds, milliseconds)
}

fun minuteChronometerBase(
    startMillis: Long,
    nowMillis: Long,
    elapsedRealtimeMillis: Long,
): Long {
    val remainder = elapsedMillis(startMillis, nowMillis) % HOUR_MILLIS
    return elapsedRealtimeMillis - remainder
}
