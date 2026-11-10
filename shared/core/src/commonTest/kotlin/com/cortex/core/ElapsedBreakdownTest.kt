package com.cortex.core

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

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
    fun elapsedClockZoneIsOffsetFromStart() {
        // start at 20:26 -> clock must read 00:00 when elapsed is 0 -> GMT+03:34
        val start = 20 * HOUR_MILLIS + 26 * 60_000L
        assertEquals("GMT+03:34", elapsedClockZoneId(start))
    }

    @Test
    fun elapsedClockZoneWrapsAtDay() {
        assertEquals("GMT+00:00", elapsedClockZoneId(0L))
        assertEquals("GMT+00:01", elapsedClockZoneId(DAY_MILLIS - 60_000L))
        assertEquals("GMT-00:59", elapsedClockZoneId(59 * 60_000L))
        assertEquals(
            "GMT+03:34",
            elapsedClockZoneId(5 * DAY_MILLIS + 20 * HOUR_MILLIS + 26 * 60_000L),
        )
    }

    @Test
    fun elapsedClockZoneStaysWithinTextClockOffsetLimit() {
        // TextClock rejects java.time offsets beyond +/-18:00 and shows the device time.
        var minute = 0L
        while (minute < DAY_MILLIS / 60_000L) {
            val zone = elapsedClockZoneId(minute * 60_000L)
            val offset = zone.removePrefix("GMT").replace(":", "").toLong()
            assertTrue(offset in -1800..1800, "offset out of range: $zone")
            minute++
        }
    }

    @Test
    fun nextHourBoundaryIsElapsedNotWallClock() {
        // Start at :37 past the hour; the digits change at :37, not at the top of the hour.
        val start = 37 * 60_000L
        assertEquals(
            start + HOUR_MILLIS,
            nextHourBoundaryAt(startMillis = start, nowMillis = start + 5 * 60_000L),
        )
    }

    @Test
    fun nextHourBoundarySkipsCurrentBoundary() {
        assertEquals(2 * HOUR_MILLIS, nextHourBoundaryAt(startMillis = 0L, nowMillis = HOUR_MILLIS))
        assertEquals(3 * HOUR_MILLIS, nextHourBoundaryAt(0L, 2 * HOUR_MILLIS + 1))
    }

    @Test
    fun nextHourBoundaryWhenClockRolledBack() {
        assertEquals(10_000L + HOUR_MILLIS, nextHourBoundaryAt(startMillis = 10_000L, nowMillis = 5_000L))
    }
}
