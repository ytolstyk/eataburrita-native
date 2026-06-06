package com.tolstykh.eatABurrita.ui.stats

data class Achievement(
    val id: String,
    val name: String,
    val description: String,
    val emoji: String,
    val progress: Float,
    val target: Int,
    val currentValue: Int,
    val isUnlocked: Boolean,
    val category: AchievementCategory,
)

enum class AchievementCategory { COUNT, STREAK, LOCATION, TIME, CALORIE }

fun computeAchievements(statsData: StatsData, distinctLocationCount: Int): List<Achievement> {
    fun make(
        id: String,
        name: String,
        description: String,
        emoji: String,
        category: AchievementCategory,
        currentValue: Int,
        target: Int,
    ): Achievement {
        val progress = (currentValue.toFloat() / target).coerceIn(0f, 1f)
        return Achievement(
            id = id,
            name = name,
            description = description,
            emoji = emoji,
            progress = progress,
            target = target,
            currentValue = currentValue,
            isUnlocked = progress >= 1f,
            category = category,
        )
    }

    val earlyBirdCount = statsData.hourOfDayCounts.subList(0, 9).sum()
    val lunchCount = statsData.hourOfDayCounts.subList(12, 14).sum()
    val afternoonCount = statsData.hourOfDayCounts.subList(15, 18).sum()
    val nightOwlCount = statsData.hourOfDayCounts.subList(22, 24).sum()
    val mondayCount = statsData.dayOfWeekCounts[0]
    val weekendCount = statsData.dayOfWeekCounts[5] + statsData.dayOfWeekCounts[6]
    val topLocationCount = statsData.topLocations.firstOrNull()?.second ?: 0

    return listOf(
        make("first_burrito", "First Burrito", "Log your very first burrito", "🌯", AchievementCategory.COUNT, statsData.totalCount, 1),
        make("bakers_dozen", "Baker's Dozen", "Log 13 burritos total", "🎉", AchievementCategory.COUNT, statsData.totalCount, 13),
        make("half_century", "Burrito Half-Century", "Log 50 burritos total", "🔥", AchievementCategory.COUNT, statsData.totalCount, 50),
        make("century", "Burrito Century", "Log 100 burritos total", "💯", AchievementCategory.COUNT, statsData.totalCount, 100),
        make("burrito_500", "Burrito 500", "Log 500 burritos total", "🏆", AchievementCategory.COUNT, statsData.totalCount, 500),
        make("streak_3", "Hat Trick", "Achieve a 3-day eating streak", "🎩", AchievementCategory.STREAK, statsData.bestStreak, 3),
        make("streak_7", "7-Day Streak", "Eat a burrito every day for a week", "📅", AchievementCategory.STREAK, statsData.bestStreak, 7),
        make("streak_30", "Month of Burritos", "Maintain a 30-day streak", "🗓️", AchievementCategory.STREAK, statsData.bestStreak, 30),
        make("location_scout", "Location Scout", "Log a burrito at a named location", "📍", AchievementCategory.LOCATION, distinctLocationCount, 1),
        make("globetrotter", "Globetrotter", "Log at 5 distinct locations", "🌍", AchievementCategory.LOCATION, distinctLocationCount, 5),
        make("world_explorer", "World Explorer", "Log at 10 distinct locations", "🗺️", AchievementCategory.LOCATION, distinctLocationCount, 10),
        make("creature_of_habit", "Creature of Habit", "Visit one location 5 or more times", "🏠", AchievementCategory.LOCATION, topLocationCount, 5),
        make("early_bird", "Early Bird", "Log 5 burritos before 9am", "🐦", AchievementCategory.TIME, earlyBirdCount, 5),
        make("lunch_regular", "Lunch Regular", "Log 10 burritos at lunchtime (noon–2pm)", "🥗", AchievementCategory.TIME, lunchCount, 10),
        make("afternoon_snacker", "Afternoon Snacker", "Log 5 burritos in the afternoon (3–5pm)", "☀️", AchievementCategory.TIME, afternoonCount, 5),
        make("night_owl", "Night Owl", "Log 5 burritos after 10pm", "🦉", AchievementCategory.TIME, nightOwlCount, 5),
        make("monday_motivation", "Monday Motivation", "Log 5 burritos on Mondays", "💪", AchievementCategory.TIME, mondayCount, 5),
        make("weekend_warrior", "Weekend Warrior", "Log 5 burritos on weekends", "🎮", AchievementCategory.TIME, weekendCount, 5),
        make("calorie_crusher", "Calorie Crusher", "Accumulate 5,000 calories tracked", "⚡", AchievementCategory.CALORIE, statsData.totalCalories, 5000),
        make("calorie_king", "Calorie King", "Accumulate 10,000 calories tracked", "👑", AchievementCategory.CALORIE, statsData.totalCalories, 10000),
    )
}
