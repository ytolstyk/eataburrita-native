package com.tolstykh.eatABurrita.helpers

enum class BurritoPersonality(val label: String, val description: String, val emoji: String) {
    EarlyBird("The Early Bird", "Peak hours: 6–10 AM", "🌅"),
    LunchtimeLoyalist("The Lunchtime Loyalist", "Peak hours: 11 AM–2 PM", "☀️"),
    AfternoonSnacker("The Afternoon Snacker", "Peak hours: 2–6 PM", "🌤️"),
    EveningEnthusiast("The Evening Enthusiast", "Peak hours: 6–9 PM", "🌆"),
    LateNightDevotee("The Late Night Devotee", "Peak hours: 9 PM–midnight", "🌙"),
    WeekendWarrior("The Weekend Warrior", "Weekends are burrito days", "🎉"),
    EverydayDevotee("The Everyday Devotee", "Any time is burrito time", "🌯"),
}

fun computeBurritoPersonality(
    hourOfDayCounts: List<Int>,
    dayOfWeekCounts: List<Int>,
): BurritoPersonality {
    val total = hourOfDayCounts.sum()
    if (total == 0) return BurritoPersonality.EverydayDevotee

    // dayOfWeekCounts is 0=Mon..6=Sun
    val weekdayTotal = (0..4).sumOf { dayOfWeekCounts.getOrElse(it) { 0 } }
    val weekendTotal = (5..6).sumOf { dayOfWeekCounts.getOrElse(it) { 0 } }
    // Normalize per-day: weekdays = 5 days, weekends = 2 days
    val weekdayPerDay = if (weekdayTotal > 0) weekdayTotal / 5.0 else 0.0
    val weekendPerDay = if (weekendTotal > 0) weekendTotal / 2.0 else 0.0
    if (weekendPerDay > weekdayPerDay * 1.5 && weekendTotal >= 3) {
        return BurritoPersonality.WeekendWarrior
    }

    val windows = mapOf(
        BurritoPersonality.EarlyBird to (6..10).sumOf { hourOfDayCounts.getOrElse(it) { 0 } },
        BurritoPersonality.LunchtimeLoyalist to (11..13).sumOf { hourOfDayCounts.getOrElse(it) { 0 } },
        BurritoPersonality.AfternoonSnacker to (14..17).sumOf { hourOfDayCounts.getOrElse(it) { 0 } },
        BurritoPersonality.EveningEnthusiast to (18..20).sumOf { hourOfDayCounts.getOrElse(it) { 0 } },
        BurritoPersonality.LateNightDevotee to ((21..23).sumOf { hourOfDayCounts.getOrElse(it) { 0 } } +
            (0..5).sumOf { hourOfDayCounts.getOrElse(it) { 0 } }),
    )

    return windows.maxByOrNull { it.value }?.key ?: BurritoPersonality.EverydayDevotee
}
