# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Build Commands

```bash
./gradlew assembleDebug          # Build debug APK
./gradlew assembleRelease        # Build release APK (minified + resource shrinking)
./gradlew installDebug           # Build and install on connected device/emulator
./gradlew test                   # Run all unit tests
./gradlew test --tests "com.tolstykh.eatABurrita.FormatTest"  # Run a single test class
./gradlew connectedAndroidTest   # Run instrumented tests (requires device/emulator)
./gradlew clean                  # Clean build artifacts
./gradlew lint                   # Run static code analysis
```

## Project Overview

**Eat-a-Burrita** is a five-screen Android app that tracks burrito consumption, shows nearby Mexican restaurants on a map, displays detailed stats, shows burrito recipes, and manages app settings.

- **Min SDK:** 35, **Target/Compile SDK:** 36
- **Language:** Kotlin, **UI:** 100% Jetpack Compose + Material 3
- **Architecture:** MVVM with Hilt dependency injection
- **Package:** `com.tolstykh.eatABurrita`

## Architecture

### Screens & Navigation

Type-safe Compose Navigation (`Navigation.kt`) with five serializable routes:

- `Home` → `TimerScreen` — live timer, total count, favorite place, 30-day chart, burrito facts ticker, Eat/Share/Map/Settings/Stats/Recipes buttons
- `Map` → `MapScreen` — Google Maps with nearby Mexican restaurant markers and ratings
- `Settings` → `SettingsScreen` — dark mode, per-type notification toggles, location modal toggle, entry history (edit/delete/add), reset
- `Stats` → `StatsScreen` — summary chips (total, avg/week, streaks), Canvas-drawn charts (30-day daily, day-of-week, hour-of-day, 12-month trend, top locations), and achievements section
- `Recipes` → `RecipesScreen` — expandable list of 10 hardcoded `BurritoRecipe` objects with ingredient checkboxes; shows a "Local Favorite" section at top derived from the user's country via GPS + Geocoder

### Data Layer

**Room Database** (`data/BurritoDatabase.kt`, version 2):
- `BurritoEntry` entity stores `id`, `timestamp`, `locationLat`, `locationLong`, `locationName`, `calories`
- `BurritoDao` provides CRUD plus aggregated SQLite queries (day-of-week, hour-of-day, monthly, top locations, distinct days, top locations with coords) — these avoid loading all rows by doing grouping in SQL

**DataStore Preferences** (`data/AppPreferencesRepository.kt`):
- `dark_mode`, `show_location_modal`, `notifications_enabled`, `notification_permission_asked` (Booleans)
- `geofence_notifications_enabled`, `streak_notifications_enabled`, `weekly_recap_notifications_enabled` (per-type notification toggles)
- `three_day_notified`, `seven_day_notified` (Boolean flags reset after a burrito is logged)
- `checked_ingredients` (StringSet) — keys are `"${recipeId}_${ingredientIndex}"`, persisted across sessions

### Stats Screen & Achievements

`StatsViewModel` combines two parallel `combine` flows to avoid the 5-argument `combine` limit:
1. `summaryFlow` — total count + distinct days → current/best streak, avg/week
2. `chartFlow` — daily entries + DOW/hour/monthly/location aggregates + distinct location count → chart data and achievements

All charts are drawn with `Canvas` + tap detection via `pointerInput`/`detectTapGestures` to show a floating count bubble on bar tap.

`Achievement` (`ui/stats/Achievement.kt`) is a pure data model computed by `computeAchievements(statsData, distinctLocationCount)`. Categories: COUNT, STREAK, LOCATION, TIME, CALORIE. `AchievementsSection` renders them in a lazy grid; `AchievementUnlockedDialog` is shown when a new achievement unlocks.

### Logging Flow

When the user taps "Eat!":
1. `LocationPickerModal` — optional location tagging via Places API search or GPS
2. `SizePickerModal` (`ui/main/SizePickerModal.kt`) — pick burrito size and extras to compute calorie total; calories stored on the entry
3. Entry saved to Room; `TimerScreen` resets the live timer
4. `CelebrationCanvas` (`ui/components/CelebrationCanvas.kt`) plays a particle burst animation

### Burrito Classifier

`BurritoClassifier` (`classifier/BurritoClassifier.kt`) wraps ML Kit's `ImageLabeling` API with on-device classification. It checks a hardcoded set of food-related labels (`Burrito`, `Wrap`, `Tortilla`, `Mexican food`, etc.) at a 0.4 confidence threshold. `BurritoVerdictDialog` (`ui/main/BurritoVerdictDialog.kt`) shows the result after classification.

### Recipes Screen

All 10 recipes are hardcoded in `RecipesViewModel.kt` as `allRecipes: List<BurritoRecipe>`. Each recipe has `countryCodes: List<String>`. On init, `RecipesViewModel` uses `FusedLocationProviderClient.lastLocation` + `Geocoder` to resolve the device's country code and surfaces the matching recipe as "Local Favorite" at the top of the list.

### Notifications & Background Work

`BurritoNotificationManager` manages four notification channels:
- `burrito_reminder` — 3-day and 7-day reminders when the user hasn't logged
- `burrito_geofence` — alerts when entering a favorite burrito location's geofence
- `burrito_streak` — streak milestone notifications at 7, 14, 30, and 50 consecutive days
- weekly recap — Monday morning digest via `BurritoReminderWorker`

`BurritoReminderWorker` runs every 12 hours via WorkManager; checks days since last entry, current streak, and day-of-week to send the appropriate notification. The 7-day reminder includes an intent extra (`EXTRA_OPEN_MAP`) to open the map screen directly.

`BootReceiver` re-registers geofences after device reboot.

`EatABurrita.kt` initializes all notification channels and schedules the periodic worker on app start. `TimerScreen` requests `POST_NOTIFICATIONS` permission on first launch.

Each notification type can be toggled independently in Settings.

### Location & Geofences

- `LocationService` wraps `FusedLocationProviderClient` into a `callbackFlow`, emitting `LatLng` every 10 seconds
- `GetLocationUseCase` is the entry point; injected via Hilt (`LocationModule`)
- `MapScreenViewModel` collects the flow and manages camera position state
- `GeofenceManager` (`location/GeofenceManager.kt`) registers geofences for the user's top 3 favorite burrito locations (300m radius, `GEOFENCE_TRANSITION_ENTER`) using `BurritoDao.getTopLocationsWithCoords()`
- `GeofenceBroadcastReceiver` fires a geofence notification via `BurritoNotificationManager` when the user enters a registered fence
- Runtime permissions handled with Accompanist Permissions library
- `LocationPermissionBanner` (`ui/components/`) is a reusable composable for surfacing location permission prompts

### Google Maps Integration

- `MapScreen` uses `GoogleMap` composable with `MarkerInfoWindowComposable` for restaurant markers
- Places API searches for Mexican restaurants/cafes/meal takeaways within 50km; restaurant ratings are displayed in the selection card
- Google Maps API key is stored in `secrets.properties` (not committed) and injected via the Secrets Gradle Plugin

### Share Card

`BurritoShareCard` (`ui/share/BurritoShareCard.kt`) generates a visual shareable card with the user's burrito stats. `shareOptions.kt` (`helpers/`) provides random share message text with stats.

### Theming

`ui/theme/` defines a Material 3 color scheme (orange/purple/blue palette) with custom extended colors via `CompositionLocalProvider`. Supports dark mode (toggled via Settings). Dynamic color is disabled.

### Helpers (`helpers/`)

- `format.kt` — date/duration formatting, address formatting
- `distance.kt` — `distanceBetweenInMiles()` between two `LatLng` points
- `shareOptions.kt` — random share message generation with stats
- `topBarHeight.kt` — status bar height utility
- `hasLocationPermissions.kt` — `Context.hasLocationPermission()` extension
- `BurritoPersonality.kt` — personality/tone helpers for UI copy

## Key Files

| File | Role |
|------|------|
| `EatABurrita.kt` | `@HiltAndroidApp` Application class; sets up notification channels + worker |
| `MainActivity.kt` | Entry point; edge-to-edge + Compose setup; handles open-map intent extra |
| `Navigation.kt` | Nav graph definition (Home, Map, Settings, Stats, Recipes) |
| `ui/main/TimerScreen.kt` + `TimeScreenViewModel.kt` | Home screen |
| `ui/main/LocationPickerModal.kt` | Modal for tagging location when logging a burrito |
| `ui/main/SizePickerModal.kt` | Modal for picking burrito size and extras; computes calorie total |
| `ui/main/DayLocationModal.kt` | Modal showing locations for a chart bar tap |
| `ui/main/BurritoFacts.kt` | Hardcoded list of burrito facts shown on the home screen |
| `ui/main/BurritoVerdictDialog.kt` | Dialog showing ML Kit burrito classification result |
| `ui/map/MapScreen.kt` + `MapScreenViewModel.kt` | Map screen |
| `ui/settings/SettingsScreen.kt` + `SettingsViewModel.kt` | Settings screen |
| `ui/settings/LocationEditModal.kt` | Modal for editing a past entry's location via Places API |
| `ui/stats/StatsScreen.kt` + `StatsViewModel.kt` | Stats screen with Canvas charts and achievements |
| `ui/stats/Achievement.kt` | Achievement data model + `computeAchievements()` pure function |
| `ui/stats/AchievementsSection.kt` + `AchievementUnlockedDialog.kt` | Achievement UI |
| `ui/recipes/RecipesScreen.kt` + `RecipesViewModel.kt` | Recipes screen; `allRecipes` list lives here |
| `ui/share/BurritoShareCard.kt` | Visual shareable stats card |
| `ui/components/LocationPermissionBanner.kt` | Reusable location permission banner |
| `ui/components/CelebrationCanvas.kt` | Particle burst animation shown after logging a burrito |
| `ui/components/StreakMilestoneOverlay.kt` | Full-screen overlay shown when a streak milestone fires |
| `classifier/BurritoClassifier.kt` | ML Kit image labeling — burrito-or-not classification |
| `location/GeofenceManager.kt` | Registers geofences for top favorite burrito locations |
| `location/GeofenceBroadcastReceiver.kt` | Receives geofence transition events and fires notifications |
| `notification/BurritoNotificationManager.kt` | Creates notification channels and sends all notification types |
| `worker/BurritoReminderWorker.kt` | WorkManager worker: reminders, streak milestones, weekly recap |
| `worker/BootReceiver.kt` | Re-registers geofences after device reboot |
| `data/BurritoDatabase.kt` + `BurritoDao.kt` | Room database and DAO |
| `data/AppPreferencesRepository.kt` | DataStore-backed app preferences |
| `gradle/libs.versions.toml` | All dependency versions (version catalog) |

## API Keys

Add a `secrets.properties` file at the project root (not committed to git):

```
MAPS_API_KEY=your_google_maps_api_key_here
```

## Validations

Clean up unused imports, constants, functions before submitting.

## Testing

Write unit tests for any new logic added (ViewModels, helpers, use cases, DAO queries). Place tests in `app/src/test/` for unit tests and `app/src/androidTest/` for instrumented tests. Run `./gradlew test` before considering a task complete.

## Changelog

Maintain `CHANGELOG.md` at the project root. Add an entry for every feature added or bug fixed, under an `[Unreleased]` section using this format:

```
## [Unreleased]
### Added
- Description of new feature

### Fixed
- Description of bug fix
```
