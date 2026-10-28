package com.cortex.core

import kotlin.test.Test
import kotlin.test.assertEquals

class ElapsedBreakdownTest {
    @Test
    fun zeroAtStart() {
        assertEquals(ElapsedBreakdown.ZERO, elapsedBreakdown(1_000L, 1_000L))
    }

    @Test
    fun subsecond() {
        assertEquals(ElapsedBreakdown(0, 0, 0, 0, 999), elapsedBreakdown(0L, 999L))
    }

    @Test
    fun oneSecond() {
        assertEquals(ElapsedBreakdown(0, 0, 0, 1, 0), elapsedBreakdown(0L, 1_000L))
    }

    @Test
    fun fiftyNineSeconds() {
        assertEquals(ElapsedBreakdown(0, 0, 0, 59, 0), elapsedBreakdown(0L, 59_000L))
    }

    @Test
    fun oneMinute() {
        assertEquals(ElapsedBreakdown(0, 0, 1, 0, 0), elapsedBreakdown(0L, 60_000L))
    }

    @Test
    fun oneHour() {
        assertEquals(ElapsedBreakdown(0, 1, 0, 0, 0), elapsedBreakdown(0L, 3_600_000L))
    }

    @Test
    fun oneDay() {
        assertEquals(ElapsedBreakdown(1, 0, 0, 0, 0), elapsedBreakdown(0L, DAY_MILLIS))
    }

    @Test
    fun multiDay() {
        val elapsed = 2 * DAY_MILLIS + 3 * 3_600_000L + 4 * 60_000L + 5_000L + 6
        assertEquals(ElapsedBreakdown(2, 3, 4, 5, 6), elapsedBreakdown(0L, elapsed))
    }

    @Test
    fun clockRollbackClampsToZero() {
        assertEquals(ElapsedBreakdown.ZERO, elapsedBreakdown(10_000L, 5_000L))
    }

    @Test
    fun chronometerBaseUsesElapsedRealtime() {
        assertEquals(95_000L, chronometerBase(startMillis = 0L, nowMillis = 5_000L, elapsedRealtimeMillis = 100_000L))
    }

    @Test
    fun chronometerBaseDropsWholeDays() {
        val now = 2 * DAY_MILLIS + 7_000L
        assertEquals(93_000L, chronometerBase(startMillis = 0L, nowMillis = now, elapsedRealtimeMillis = 100_000L))
    }
}
