# EataBurrita — Feature Ideas

## Tier 1 — High Fun, Reasonable Effort

### 1. Burrito Photo Log
Attach an optional photo to each entry using CameraX or the gallery picker.
- Add `photoPath: String?` to `BurritoEntry` (Room migration v3)
- Show thumbnails in the entry list (Settings screen) and in the map marker bottom tray
- New photo grid screen ("Memories") accessible from Stats or the Home screen

---

## Tier 2 — Great Polish

### 8. Mood Tagging
Optional emoji mood attached to each entry: 😋 Delicious / 😐 Meh / 🤢 Bad Day.
- Stored as a nullable `Int` rating on `BurritoEntry`
- Stats screen: "Mood Distribution" donut chart
- Adds personality to the entry feed without heavy new UI

---

## Tier 3 — Bigger Lifts / Integrations

### 9. Health Connect Integration
Log estimated calories automatically to **Google Health Connect** when a burrito is logged (requires Calorie Calculator feature). Show "calories burned today" as context in Stats.

### 11. AR Burrito Scanner
CameraX + ML Kit object detection — point the camera at a restaurant menu and detect "Burrito" items. Tap to log instantly. Ambitious but very memorable UX.

---

## Files Likely Touched per Feature

| Feature | Key Files |
|---------|-----------|
| Photo log | `BurritoEntry.kt`, `BurritoDatabase.kt`, `BurritoDao.kt`, `TimerScreen.kt`, `TimeScreenViewModel.kt`, new `PhotoGalleryScreen.kt` |
| Mood tagging | `BurritoEntry.kt`, `BurritoDatabase.kt`, `StatsScreen.kt` |

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
