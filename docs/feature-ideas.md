# EataBurrita — Feature Ideas

## Tier 1 — High Fun, Reasonable Effort

### 1. Burrito Photo Log
Attach an optional photo to each entry using CameraX or the gallery picker.
- Add `photoPath: String?` to `BurritoEntry` (Room migration v3)
- Show thumbnails in the entry list (Settings screen) and in the map marker bottom tray
- New photo grid screen ("Memories") accessible from Stats or the Home screen

### 2. On-Device "Is This a Burrito?" Classifier
Use **Google ML Kit ImageLabeler** — no custom model needed, it already recognizes "Burrito", "Wrap", "Tortilla" out of the box.
- Entry point: camera icon on the Home screen
- After capture, run inference and show a fun verdict:
  - ✅ "Confirmed Burrito! +1 logged" — prompts to add an entry
  - ❌ "That's not a burrito. Nice try." — snarky rejection message
  - 🤔 "Burrito confidence: 73% — we'll allow it" — borderline case
- If a custom model is desired later: bundle a quantized TFLite MobileNet (~5–10 MB) fine-tuned on food categories

### 3. Calorie Calculator with Fun Energy Numbers
- After logging, show a quick size picker: Small / Regular / Mega / Burrito Bowl + optional extras (guac, sour cream, etc.)
- Store calories as a nullable `Int` on `BurritoEntry` (Room migration)
- Show cumulative totals in Stats with real-world equivalents:
  - "47,300 kcal total = enough to power a Tesla for 3 days"
  - "= 1,240 hours of Netflix"
  - "= 118,000 flights of stairs"

### 4. Burrito Facts of the Day
A rotating daily fact card just above the 30-day chart on the Home screen.
- 50–100 hardcoded facts in a Kotlin file (no network needed)
- Rotates by `day % facts.size` seeded by the current date
- Example facts:
  - "The word 'burrito' means 'little donkey' in Spanish"
  - "The world's largest burrito weighed 5,799 lbs (2018)"
  - "A Mission-style burrito can contain over 1,200 calories"
- Could also flash a random fact on the "Eat!" confirmation

### 5. Achievements / Streak Badges
~20 achievements computed from existing stats — no new DB fields required.

| Achievement | Trigger |
|-------------|---------|
| First Burrito | Log entry #1 |
| 7-Day Streak | 7 consecutive days |
| 30-Day Streak | 30 consecutive days |
| Burrito Century | 100 total entries |
| Globetrotter | 5 distinct locations |
| Lunch Regular | 10 entries between 12–2pm |
| Night Owl | 5 entries after 10pm |
| Monday Motivation | 5 entries on Mondays |

- "Achievement Unlocked!" bottom-sheet with confetti animation on first unlock
- New Achievements section in Stats screen showing locked/unlocked state with progress bars

---

## Tier 2 — Great Polish

### 6. Confetti / Celebration Animations
- Burst of burrito emojis 🌯🔥 raining down when "Eat!" is pressed
- Full-screen celebration overlay on streak milestones (7, 30, 100 days)
- Compose `Canvas` particle system with `LaunchedEffect` + animated offset/alpha

### 7. Shareable Burrito Card (Spotify Wrapped-style)
Generate a shareable bitmap from a Compose layout showing:
- Annual burrito count and favorite location
- Current streak and best streak
- A computed "Burrito Personality" label (e.g., "The Lunchtime Loyalist", "The Late Night Devotee", "The Weekend Warrior")
- Available via the existing Share button on the Home screen

### 8. Mood Tagging
Optional emoji mood attached to each entry: 😋 Delicious / 😐 Meh / 🤢 Bad Day.
- Stored as a nullable `Int` rating on `BurritoEntry`
- Stats screen: "Mood Distribution" donut chart
- Adds personality to the entry feed without heavy new UI

---

## Tier 3 — Bigger Lifts / Integrations

### 9. Health Connect Integration
Log estimated calories automatically to **Google Health Connect** when a burrito is logged (requires Calorie Calculator feature). Show "calories burned today" as context in Stats.

### 10. Geofence Radar Notifications
Use `GeofencingClient` around the user's top 3 favorite locations.
- Push notification: "You're 0.3 miles from your favorite burrito spot!"
- Builds on existing `LocationService` and `BurritoNotificationManager`

### 11. AR Burrito Scanner
CameraX + ML Kit object detection — point the camera at a restaurant menu and detect "Burrito" items. Tap to log instantly. Ambitious but very memorable UX.

---

## Recommended First Sprint

**ML classifier** (most unique/memorable) + **calorie fun numbers** + **confetti animation** — these three together make the app feel alive, funny, and worth sharing.

---

## Files Likely Touched per Feature

| Feature | Key Files |
|---------|-----------|
| Photo log | `BurritoEntry.kt`, `BurritoDatabase.kt`, `BurritoDao.kt`, `TimerScreen.kt`, `TimeScreenViewModel.kt`, new `PhotoGalleryScreen.kt` |
| ML classifier | New `BurritoClassifier.kt`, `CameraScreen.kt`; `TimerScreen.kt` for entry point |
| Calorie calculator | `BurritoEntry.kt`, `BurritoDatabase.kt`, new `CaloriePickerModal.kt`, `StatsScreen.kt` |
| Facts card | `TimerScreen.kt`, new `BurritoFacts.kt` |
| Achievements | `StatsScreen.kt`, `StatsViewModel.kt`, new `Achievements.kt`, new `AchievementUnlockedModal.kt` |
| Confetti | `TimerScreen.kt`, new `ConfettiOverlay.kt` |
| Shareable card | `TimerScreen.kt`, new `ShareCardGenerator.kt` |
