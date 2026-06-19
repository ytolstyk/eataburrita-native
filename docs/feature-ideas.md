# EataBurrita — Feature Ideas

> Sorted by effort ↓ × impact ↑ — top = lowest effort, highest impact.

---

## Tier 1 — Quick Wins (Low Effort, High Impact)

### 18. "On This Day" Time Machine
Home screen card that appears when the user has data from a year ago: *"One year ago today you had your 12th burrito at Chipotle 🌯"*. Tap to see a mini recap of that week. Uses existing timestamp data — zero new storage needed.

### 27. Burrito Streak Heatmap
A GitHub-style contribution grid on the Stats screen showing daily burrito activity over the past year — darker green = more burritos that day. Tap any cell to see what was logged. Zero new data needed (uses existing timestamps). Canvas drawing pattern already exists in StatsScreen.

### AI-1. Gemini Burrito Photo Caption
When a photo is attached to a log entry, a "Caption This" button calls Gemini with the image and returns a witty one-liner ("Golden ratio of beans to rice. Flawless."). The image→Gemini pipeline is already built for the scanner — this reuses it with a different prompt. Caption stored as nullable `captionText: String?` on `BurritoEntry`. Displayed beneath the photo in the Memories grid and on the share card.
- Key files: `BurritoEntry.kt`, `BurritoDatabase.kt` (migration), `SizePickerModal.kt` or `MemoriesScreen.kt`, existing Gemini client

### AI-2. AI Monthly Recap Story
On the 1st of each month, format last month's stats (count, locations, streaks, best day, top calorie entry) as a JSON prompt and send to Gemini. Returns a short narrative paragraph: *"February was your most loyal month yet — 11 burritos, a 9-day streak, and one courageous late-night Chipotle run."* Shown as a dismissable home screen card. Result cached in DataStore so Gemini is called only once per month.
- Key files: `TimerScreen.kt`, `TimeScreenViewModel.kt`, `AppPreferencesRepository.kt`, existing Gemini client

### 15. Burrito Oracle
Shake the phone to receive a random burrito-themed fortune: *"The guac is strong with you today."* / *"A burrito in the hand is worth two on the menu."* / *"Your next burrito will be legendary."* Shown as a fun modal with a shimmering crystal ball emoji.

### 16. Streak Freeze Token
Once per week, tap "Freeze My Streak" to protect it from breaking for one day. Token refills every Monday. Shows a frozen snowflake on the streak counter when active. Stored in DataStore.
- Key files: `AppPreferencesRepository.kt`, `TimerScreen.kt`, `TimeScreenViewModel.kt`

---

## Tier 2 — Medium Effort, High Impact

### AI-3. AI Burrito Coach
Once a week, Gemini receives a summary of your eating patterns (top hours, top locations, avg calories, frequency trend) and returns a single actionable insight tip: *"You eat 3× more burritos on Fridays — consider pre-logging your weekend plans!"* Shown as a collapsible card in the Stats screen. Refreshed weekly, cached in DataStore.
- Key files: `StatsScreen.kt`, `StatsViewModel.kt`, `AppPreferencesRepository.kt`, existing Gemini client

### 17. Burrito Pledge
Set a yearly/monthly burrito goal from the Stats screen. Progress bar lives on the Home screen beneath the count. Send a motivational notification at 50% and 90% of the goal. (*"You're 3 burritos away from glory."*)
- Key files: `AppPreferencesRepository.kt`, `StatsScreen.kt`, `TimerScreen.kt`, `BurritoReminderWorker.kt`

### 14. Burrito Bingo
A 5×5 bingo card with challenges like "Eat at 3 different spots", "Eat before noon", "Try a veggie burrito", "Log on a Sunday", "Beat your personal record". Refreshes monthly. Tap a tile to mark it done. Show a fireworks overlay when you get a bingo.
- Key files: new `BingoScreen.kt`, `BingoViewModel.kt`, `AppPreferencesRepository.kt`

---

## Tier 3 — Medium Effort, Medium Impact

### AI-4. AI Burrito Name Generator
Before saving an entry, tap "Name This Burrito" to send the size, extras, location, and time of day to Gemini and receive a ridiculous name like *"The Sleepy Carnitas Supremo"* or *"El Guac del Destino"* with a one-sentence origin story. Name stored as nullable `nickname: String?` on `BurritoEntry`. Shown in the Settings entry list and on the share card.
- Key files: `BurritoEntry.kt`, `BurritoDatabase.kt` (migration), `SizePickerModal.kt`, existing Gemini client
- Supersedes original #22 procedural-generation idea

### 19. Burrito Horoscope
Daily burrito horoscope generated from your eating patterns + day of week + moon phase (fake but themed). *"Your burrito energy is peaking — avoid sour cream today, Mercury is in retrograde."* Lives as a collapsible card on the Home screen next to the Burrito Facts ticker.

### 20. Burrito Zodiac Sign
Based on *when* and *where* you eat, you receive a permanent Burrito Zodiac sign (e.g., "The Midnight Carnivore", "The Loyal Local", "The Globetrotter"). Shown on the Stats screen alongside the Personality label. Recomputed monthly.

### 21. Burrito Weather Pairing
Use the device's location + a free weather API to suggest a burrito type based on current conditions. Cold + rainy → *"Perfect day for a hot bean and cheese."* Sunny → *"Grilled chicken, light and bright."* Shown as a suggestion chip on the Home screen.

### 23. Pixel Burrito Gallery
Each logged burrito auto-generates a tiny pixel-art burrito sprite (deterministic from timestamp seed — no network, no ML) using Canvas. A "Burrito Collection" grid in the Stats or Memories screen shows all your sprites. Purely cosmetic and delightful.

### 29. Burrito Time Capsule
Write a short note to your future burrito self ("still chasing that perfect al pastor"). It auto-surfaces as a home screen card after 6 months with a "You wrote this 6 months ago" header. Stored in DataStore. No server needed.

### 30. Burrito Companion Log
Optionally tag who you ate with (free-text name). Stats screen shows a leaderboard of your most frequent burrito companions. Great for couples or office regulars.
- Key files: `BurritoEntry.kt`, `BurritoDatabase.kt` (migration), `SizePickerModal.kt`, `StatsScreen.kt`

---

## Tier 4 — Bigger Lifts / Integrations

### 9. Health Connect Integration
Log estimated calories automatically to **Google Health Connect** when a burrito is logged (requires Calorie Calculator feature). Show "calories burned today" as context in Stats.

### 24. Burrito Rival Mode
Set a friend's name + burrito count manually (or via a shareable code). A persistent "vs. [Rival]" banner on the Home screen shows who's winning. Update the rival's count with a quick "Update Rival" button. No backend needed — just a rivalry spirit score in DataStore.

### 25. Burrito Carbon Footprint (Ironic)
Tongue-in-cheek eco stat in the Stats screen: calculate a fake-but-themed "burrito carbon footprint" from total count × average calorie weight × a made-up beef conversion factor. Offset suggestions: *"Plant 1.3 cilantro plants to compensate."*

### 11. AR Burrito Scanner
CameraX + ML Kit object detection — point the camera at a restaurant menu and detect "Burrito" items. Tap to log instantly. Ambitious but very memorable UX.

---

## Files Likely Touched per Feature

| Feature | Key Files |
|---------|-----------|
| "On This Day" Time Machine | `TimerScreen.kt`, `TimeScreenViewModel.kt`, `BurritoDao.kt` |
| Streak Heatmap | `StatsScreen.kt`, `StatsViewModel.kt`, `BurritoDao.kt` |
| Gemini Photo Caption | `BurritoEntry.kt`, `BurritoDatabase.kt`, `MemoriesScreen.kt`, Gemini client |
| AI Monthly Recap | `TimerScreen.kt`, `TimeScreenViewModel.kt`, `AppPreferencesRepository.kt` |
| Burrito Oracle | new modal composable, `TimerScreen.kt` |
| Streak Freeze | `AppPreferencesRepository.kt`, `TimerScreen.kt`, `TimeScreenViewModel.kt` |
| AI Burrito Coach | `StatsScreen.kt`, `StatsViewModel.kt`, `AppPreferencesRepository.kt` |
| Burrito Pledge | `AppPreferencesRepository.kt`, `StatsScreen.kt`, `TimerScreen.kt`, `BurritoReminderWorker.kt` |
| Burrito Bingo | new `BingoScreen.kt`, `BingoViewModel.kt`, `AppPreferencesRepository.kt` |
| AI Burrito Name Generator | `BurritoEntry.kt`, `BurritoDatabase.kt`, `SizePickerModal.kt` |
| Burrito Horoscope | `TimerScreen.kt`, `TimeScreenViewModel.kt` |
| Burrito Zodiac | `StatsScreen.kt`, `StatsViewModel.kt` |
| Burrito Weather Pairing | `TimerScreen.kt`, `TimeScreenViewModel.kt`, weather API client |
| Pixel Burrito Gallery | `StatsScreen.kt` or new `MemoriesScreen.kt` section |
| Burrito Time Capsule | `AppPreferencesRepository.kt`, `TimerScreen.kt` |
| Burrito Companion Log | `BurritoEntry.kt`, `BurritoDatabase.kt`, `SizePickerModal.kt`, `StatsScreen.kt` |

---

## Done

### 1. Burrito Photo Log ✅
Attach an optional photo to each entry using CameraX or the gallery picker.
- `photoPath: String?` added to `BurritoEntry` (Room migration)
- Thumbnails in the entry list (Settings screen)
- Photo grid screen ("Memories") accessible from the Home screen

### 2. On-Device "Is This a Burrito?" Classifier ✅
Upgraded to **Gemini 1.5 Flash** (cloud vision) — witty burrito-oracle commentary; images compressed to 512px before sending. Previously used Google ML Kit ImageLabeler.
- Entry point: camera icon on the Home screen
- Rate-limited to once per 30 seconds
- `BurritoClassifier.kt` + `BurritoVerdictDialog.kt`

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

### 12. Burrito Roulette ✅
"🎰 Burrito Roulette" FAB on the Map screen spins a slot-machine animation through nearby restaurants and picks a random one; Navigate button opens Google Maps directions to the winner. Respects hidden restaurant preferences.
- `MapScreen.kt` + `MapScreenViewModel.kt`

### 26. Personal Place Ratings ✅
Restaurants screen — rate places 1–5 stars, write notes, hide specific places from the map. Quick-edit (✏) button in the map place detail tray. Map reactively filters hidden restaurants.
- `RestaurantNote` entity, `BurritoDatabase.kt` (migration), `MapScreen.kt`, `MapScreenViewModel.kt`, `RestaurantsScreen.kt`

### 28. Quick Log Widget ✅
2×2 home screen widget with total burrito count and an "Eat!" button that instantly logs a new entry, skipping all modals. Plus a 4×1 Stats widget that auto-cycles through 5 stats every 5 seconds.
- `BurritoWidget.kt`, `AndroidManifest.xml`, `res/xml/`
