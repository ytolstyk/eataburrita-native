package com.tolstykh.eatABurrita.helpers

import org.junit.Assert.assertTrue
import org.junit.Test

class ShareOptionsTest {

    @Test
    fun getRandomMessage_returnsNonEmpty() {
        val message = getRandomMessageWithStats(5, System.currentTimeMillis(), listOf(1, 2, 3))
        assertTrue(message.isNotEmpty())
    }

    @Test
    fun getRandomMessage_containsBurritoEmoji() {
        // Run several times since the output is random
        repeat(30) {
            val message = getRandomMessageWithStats(10, System.currentTimeMillis(), listOf(2, 3))
            assertTrue(message.startsWith("🌯"))
        }
    }

    @Test
    fun getRandomMessage_withZeroCount() {
        val message = getRandomMessageWithStats(0, 0L, emptyList())
        assertTrue(message.isNotEmpty())
    }

    @Test
    fun getRandomMessage_withEmptyDailyCounts_doesNotCrash() {
        repeat(20) {
            val message = getRandomMessageWithStats(5, System.currentTimeMillis(), emptyList())
            assertTrue(message.isNotEmpty())
        }
    }

    @Test
    fun getRandomMessage_highCount_appearsInSomeMessages() {
        val messages = (1..50).map {
            getRandomMessageWithStats(42, System.currentTimeMillis(), listOf(1, 2, 3))
        }
        // At least one of the messages should mention "42"
        assertTrue(messages.any { it.contains("42") })
    }

    @Test
    fun getRandomMessage_maxDailyCount_appearsInSomeMessages() {
        val messages = (1..50).map {
            getRandomMessageWithStats(10, System.currentTimeMillis(), listOf(5, 10, 3))
        }
        // "My best day: 10 burritos!" should appear in some runs
        assertTrue(messages.any { it.contains("10") })
    }
}
