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

/**
 * Fixed GMT zone whose clock reads elapsed time mod 24 hours.
 * TextClock has minute-granularity zones, so the start must be minute-aligned (the picker is).
 */
fun elapsedClockZoneId(startMillis: Long): String {
    var offsetMinutes = (DAY_MILLIS - startMillis % DAY_MILLIS) % DAY_MILLIS / 60_000L
    // TextClock validates the zone with java.time, which rejects offsets past +/-18h
    // and falls back to the device zone. Use the negative equivalent; it reads the same.
    if (offsetMinutes > 18 * 60) offsetMinutes -= DAY_MILLIS / 60_000L
    val sign = if (offsetMinutes < 0) "-" else "+"
    val abs = kotlin.math.abs(offsetMinutes)
    return "GMT$sign" +
        (abs / 60).toString().padStart(2, '0') +
        ":" +
        (abs % 60).toString().padStart(2, '0')
}

/** Wall-clock time of the next elapsed-hour boundary, when the displayed days/hours change. */
fun nextHourBoundaryAt(startMillis: Long, nowMillis: Long): Long =
    startMillis + (elapsedMillis(startMillis, nowMillis) / HOUR_MILLIS + 1) * HOUR_MILLIS
