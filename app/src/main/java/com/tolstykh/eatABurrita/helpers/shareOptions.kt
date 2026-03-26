package com.tolstykh.eatABurrita.helpers

import java.time.Instant
import kotlin.math.roundToInt

const val APP_NAME = "Eat-a-Burrita"

private fun mapsLink(lat: Double, lng: Double) = "https://maps.google.com/?q=$lat,$lng"

fun getRandomMessageWithStats(
    burritoCount: Int,
    lastTimestamp: Long,
    dailyCounts: List<Int>,
    favoritePlaceName: String? = null,
    favoritePlaceLat: Double? = null,
    favoritePlaceLng: Double? = null,
    lastPlaceName: String? = null,
    lastPlaceLat: Double? = null,
    lastPlaceLng: Double? = null,
): String {
    val maxDaily = dailyCounts.maxOrNull() ?: 0
    val totalLast30 = dailyCounts.sum()
    val avgDaily = if (dailyCounts.isNotEmpty()) {
        (totalLast30.toFloat() / dailyCounts.size * 10).roundToInt() / 10f
    } else {
        0f
    }
    val daysSinceLastBurrito = if (lastTimestamp > 0) {
        ((Instant.now().toEpochMilli() - lastTimestamp) / 86_400_000).toInt()
    } else {
        -1
    }

    val favLink = if (favoritePlaceLat != null && favoritePlaceLng != null) mapsLink(favoritePlaceLat, favoritePlaceLng) else null
    val lastLink = if (lastPlaceLat != null && lastPlaceLng != null) mapsLink(lastPlaceLat, lastPlaceLng) else null

    val messages = listOfNotNull(
        "🌯 I've eaten $burritoCount burritos with $APP_NAME!",
        "🌯 On a burrito streak! Averaging $avgDaily burritos/day.",
        "🌯 My best day: $maxDaily burritos! (30-day record)",
        if (daysSinceLastBurrito == 0) "🌯 Just ate a burrito! Current count: $burritoCount" else null,
        if (daysSinceLastBurrito == 1) "🌯 Had a burrito yesterday. Total: $burritoCount" else null,
        "🌯 $totalLast30 burritos in the last 30 days!",
        "🌯 $burritoCount total burritos tracked. Tracking burritos is my passion!",
        "🌯 Averaging $avgDaily burritos per day. That's commitment!",
        if (favoritePlaceName != null && favLink != null)
            "🌯 My go-to burrito spot is $favoritePlaceName — $burritoCount burritos and counting! $favLink" else null,
        if (favoritePlaceName != null && favLink == null)
            "🌯 My favorite burrito place is $favoritePlaceName. $burritoCount burritos deep!" else null,
        if (lastPlaceName != null && lastLink != null && daysSinceLastBurrito == 0)
            "🌯 Just crushed a burrito at $lastPlaceName! $lastLink" else null,
        if (lastPlaceName != null && lastLink != null && daysSinceLastBurrito > 0)
            "🌯 Last burrito was at $lastPlaceName. Time to go back! $lastLink" else null,
        if (lastPlaceName != null && lastLink == null && daysSinceLastBurrito == 0)
            "🌯 Just had a burrito at $lastPlaceName! Total: $burritoCount" else null,
        if (favoritePlaceName != null && lastPlaceName != null && favoritePlaceName != lastPlaceName && favLink != null)
            "🌯 Favorite spot: $favoritePlaceName ($favLink) — but always exploring new places like $lastPlaceName!" else null,
    )

    return messages.random()
}