# EataBurrita — Feature Ideas

## Tier 1 — High Fun, Reasonable Effort

### 1. Burrito Photo Log
Attach an optional photo to each entry using CameraX or the gallery picker.
- Add `photoPath: String?` to `BurritoEntry` (Room migration v3)
- Show thumbnails in the entry list (Settings screen) and in the map marker bottom tray
- New photo grid screen ("Memories") accessible from Stats or the Home screen

### 12. Burrito Roulette
"I'm feeling lucky!" button on the Map screen spins a slot machine animation and picks a random nearby burrito spot, then navigates to it in Google Maps. Great for indecisive burrito seekers.

### 13. Mood Tagging
Optional emoji mood attached to each entry: 😋 Delicious / 😐 Meh / 🤢 Bad Day.
- Stored as a nullable `Int` rating on `BurritoEntry`
- Stats screen: "Mood Distribution" donut chart
- Adds personality to the entry feed without heavy new UI

### 14. Burrito Bingo
A 5×5 bingo card with challenges like "Eat at 3 different spots", "Eat before noon", "Try a veggie burrito", "Log on a Sunday", "Beat your personal record". Refreshes monthly. Tap a tile to mark it done. Show a fireworks overlay when you get a bingo.

### 15. Burrito Oracle
Shake the phone to receive a random burrito-themed fortune: *"The guac is strong with you today."* / *"A burrito in the hand is worth two on the menu."* / *"Your next burrito will be legendary."* Shown as a fun modal with a shimmering crystal ball emoji.

### 16. Streak Freeze Token
Once per week, tap "Freeze My Streak" to protect it from breaking for one day. Token refills every Monday. Shows a frozen snowflake on the streak counter when active. Stored in DataStore.

### 17. Burrito Pledge
Set a yearly/monthly burrito goal from the Stats screen. Progress bar lives on the Home screen beneath the count. Send a motivational notification at 50% and 90% of the goal. ("You're 3 burritos away from glory.")

### 18. "On This Day" Time Machine
Home screen card that appears when the user has data from a year ago: *"One year ago today you had your 12th burrito at Chipotle 🌯"*. Tap to see a mini recap of that week. Uses existing timestamp data — zero new storage needed.

---

## Tier 2 — Great Polish

### 19. Burrito Horoscope
Daily burrito horoscope generated from your eating patterns + day of week + moon phase (fake but themed). *"Your burrito energy is peaking — avoid sour cream today, Mercury is in retrograde."* Lives as a collapsible card on the Home screen next to the Burrito Facts ticker.

### 20. Burrito Zodiac Sign
Based on *when* and *where* you eat, you receive a permanent Burrito Zodiac sign (e.g., "The Midnight Carnivore", "The Loyal Local", "The Globetrotter"). Shown on the Stats screen alongside the Personality label. Recomputed monthly.

### 21. Burrito Weather Pairing
Use the device's location + a free weather API to suggest a burrito type based on current conditions. Cold + rainy → *"Perfect day for a hot bean and cheese."* Sunny → *"Grilled chicken, light and bright."* Shown as a suggestion chip on the Home screen.

### 22. Burrito Name Generator
Before saving an entry, tap "Name This Burrito" to get a ridiculous procedurally-generated name like *"The Sleepy Carnitas Supremo"* or *"El Guac del Destino"*. Stored as a nullable `String nickname` on `BurritoEntry`. Shown in the entry list.

### 23. Pixel Burrito Gallery
Each logged burrito auto-generates a tiny pixel-art burrito sprite (deterministic from timestamp seed — no network, no ML) using Canvas. A "Burrito Collection" grid in the Stats or Memories screen shows all your sprites. Purely cosmetic and delightful.

---

## Tier 3 — Bigger Lifts / Integrations

### 9. Health Connect Integration
Log estimated calories automatically to **Google Health Connect** when a burrito is logged (requires Calorie Calculator feature). Show "calories burned today" as context in Stats.

### 11. AR Burrito Scanner
CameraX + ML Kit object detection — point the camera at a restaurant menu and detect "Burrito" items. Tap to log instantly. Ambitious but very memorable UX.

### 24. Burrito Rival Mode
Set a friend's name + burrito count manually (or via a shareable code). A persistent "vs. [Rival]" banner on the Home screen shows who's winning. Update the rival's count with a quick "Update Rival" button. No backend needed — just a rivalry spirit score in DataStore.

### 25. Burrito Carbon Footprint (Ironic)
Tongue-in-cheek eco stat in the Stats screen: calculate a fake-but-themed "burrito carbon footprint" from total count × average calorie weight × a made-up beef conversion factor. Offset suggestions: *"Plant 1.3 cilantro plants to compensate."*

---

## Files Likely Touched per Feature

| Feature | Key Files |
|---------|-----------|
| Photo log | `BurritoEntry.kt`, `BurritoDatabase.kt`, `BurritoDao.kt`, `TimerScreen.kt`, `TimeScreenViewModel.kt`, new `PhotoGalleryScreen.kt` |
| Mood tagging | `BurritoEntry.kt`, `BurritoDatabase.kt`, `StatsScreen.kt` |
| Burrito Bingo | new `BingoScreen.kt`, `BingoViewModel.kt`, `AppPreferencesRepository.kt` |
| Streak Freeze | `AppPreferencesRepository.kt`, `TimerScreen.kt`, `TimeScreenViewModel.kt` |
| Burrito Pledge | `AppPreferencesRepository.kt`, `StatsScreen.kt`, `TimerScreen.kt` |
| Burrito Roulette | `MapScreen.kt`, `MapScreenViewModel.kt` |
| Burrito Name Generator | `BurritoEntry.kt`, `BurritoDatabase.kt`, `SizePickerModal.kt` |

---

## Done

### 2. On-Device "Is This a Burrito?" Classifier ✅
Used **Google ML Kit ImageLabeler** — recognizes "Burrito", "Wrap", "Tortilla" out of the box.
- Entry point: camera icon on the Home screen
- Shows a fun verdict after capture (confirmed / rejected / borderline)
- `BurritoClassifier.kt` wraps ML Kit at 0.4 confidence threshold
- `BurritoVerdictDialog.kt` shows the result

### 3. Calorie Calculator with Fun Energy Numbers ✅
- Size picker after logging: Small / Regular / Mega / Burrito Bowl + optional extras (guac, sour cream, etc.)
- Calories stored as `Int` on `BurritoEntry`
- `SizePickerModal.kt` handles the picker and calorie computation

### 4. Burrito Facts of the Day ✅
A rotating fact card on the Home screen.
- Hardcoded facts in `BurritoFacts.kt`
- Rotates by `day % facts.size` seeded by the current date

### 5. Achievements / Streak Badges ✅
~20 achievements computed from existing stats.
- `Achievement.kt` — pure data model + `computeAchievements()` function
- Categories: COUNT, STREAK, LOCATION, TIME, CALORIE
- `AchievementsSection` in Stats screen with locked/unlocked state
- `AchievementUnlockedDialog` shown on first unlock

### 6. Confetti / Celebration Animations ✅
- `CelebrationCanvas.kt` — particle burst animation when "Eat!" is pressed
- `StreakMilestoneOverlay.kt` — full-screen overlay on streak milestones (7, 14, 30, 50 days)

### 7. Shareable Burrito Card (Spotify Wrapped-style) ✅
- `BurritoShareCard.kt` generates a visual shareable card with stats
- Includes annual count, favorite location, current/best streak, Burrito Personality label
- `shareOptions.kt` provides random share message text

### 10. Geofence Radar Notifications ✅
- `GeofenceManager.kt` registers geofences for the user's top 3 favorite locations (300m radius)
- `GeofenceBroadcastReceiver.kt` fires a notification on `GEOFENCE_TRANSITION_ENTER`
- `BootReceiver.kt` re-registers geofences after device reboot
- Geofence notifications toggleable independently in Settings
