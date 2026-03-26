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

    @Test
    fun getRandomMessage_withFavoritePlace_appearsInSomeMessages() {
        val messages = (1..100).map {
            getRandomMessageWithStats(
                10, System.currentTimeMillis(), listOf(1, 2),
                favoritePlaceName = "Chipotle",
                favoritePlaceLat = 37.7749,
                favoritePlaceLng = -122.4194,
            )
        }
        assertTrue(messages.any { it.contains("Chipotle") })
    }

    @Test
    fun getRandomMessage_withLastPlace_appearsInSomeMessages() {
        val messages = (1..100).map {
            getRandomMessageWithStats(
                5, System.currentTimeMillis(), listOf(1),
                lastPlaceName = "Taco Bell",
                lastPlaceLat = 34.0522,
                lastPlaceLng = -118.2437,
            )
        }
        assertTrue(messages.any { it.contains("Taco Bell") })
    }

    @Test
    fun getRandomMessage_locationMessages_containMapsLink() {
        val messages = (1..200).map {
            getRandomMessageWithStats(
                5, System.currentTimeMillis(), listOf(1),
                favoritePlaceName = "Chipotle",
                favoritePlaceLat = 37.7749,
                favoritePlaceLng = -122.4194,
            )
        }
        val linkMessages = messages.filter { it.contains("maps.google.com") }
        assertTrue(linkMessages.isNotEmpty())
        assertTrue(linkMessages.all { it.contains("37.7749") && it.contains("-122.4194") })
    }
}
