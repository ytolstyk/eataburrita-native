package com.tolstykh.eatABurrita

import org.junit.Assert.assertEquals
import org.junit.Test

class FormatTest {

    @Test
    fun padWithZeros_singleDigit() = assertEquals("05", padWithZeros(5))

    @Test
    fun padWithZeros_doubleDigit() = assertEquals("42", padWithZeros(42))

    @Test
    fun padWithZeros_zero() = assertEquals("00", padWithZeros(0))

    @Test
    fun padWithZeros_customLength() = assertEquals("005", padWithZeros(5, 3))

    @Test
    fun padWithZeros_exactLength() = assertEquals("99", padWithZeros(99))

    @Test
    fun formatDuration_zero() = assertEquals("0 days, 00:00:00", formatDuration(0L))

    @Test
    fun formatDuration_oneSecond() = assertEquals("0 days, 00:00:01", formatDuration(1_000L))

    @Test
    fun formatDuration_59Seconds() = assertEquals("0 days, 00:00:59", formatDuration(59_000L))

    @Test
    fun formatDuration_oneMinute() = assertEquals("0 days, 00:01:00", formatDuration(60_000L))

    @Test
    fun formatDuration_oneHour() = assertEquals("0 days, 01:00:00", formatDuration(3_600_000L))

    @Test
    fun formatDuration_oneDay_singular() = assertEquals("1 day, 00:00:00", formatDuration(86_400_000L))

    @Test
    fun formatDuration_twoDays_plural() = assertEquals("2 days, 00:00:00", formatDuration(2 * 86_400_000L))

    @Test
    fun formatDuration_complex() {
        // 1 day + 2 hours + 3 minutes + 4 seconds
        val millis = (86_400 + 7_200 + 180 + 4) * 1_000L
        assertEquals("1 day, 02:03:04", formatDuration(millis))
    }

    @Test
    fun formatDuration_23Hours59MinutesRemainsZeroDays() =
        assertEquals("0 days, 23:59:00", formatDuration((23 * 3_600 + 59 * 60) * 1_000L))
}
