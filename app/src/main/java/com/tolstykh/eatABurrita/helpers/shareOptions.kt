package com.tolstykh.eatABurrita.helpers

import java.time.Instant
import kotlin.math.roundToInt

const val APP_NAME = "Eat-a-Burrita"

fun getRandomMessageWithStats(
    burritoCount: Int,
    lastTimestamp: Long,
    dailyCounts: List<Int>,
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

    val messages = listOfNotNull(
        "🌯 I've eaten $burritoCount burritos with $APP_NAME!",
        "🌯 On a burrito streak! Averaging $avgDaily burritos/day.",
        "🌯 My best day: $maxDaily burritos! (30-day record)",
        if (daysSinceLastBurrito == 0) "🌯 Just ate a burrito! Current count: $burritoCount" else null,
        if (daysSinceLastBurrito == 1) "🌯 Had a burrito yesterday. Total: $burritoCount" else null,
        "🌯 $totalLast30 burritos in the last 30 days!",
        "🌯 $burritoCount total burritos tracked. Tracking burritos is my passion!",
        "🌯 Averaging $avgDaily burritos per day. That's commitment!",
    )

    return messages.random()
}