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

- `Home` → `TimerScreen` — live timer, total count, favorite place, 30-day chart, Eat/Share/Map/Settings/Stats/Recipes buttons
- `Map` → `MapScreen` — Google Maps with nearby Mexican restaurant markers
- `Settings` → `SettingsScreen` — dark mode, notifications, location modal toggle, entry history (edit/delete/add), reset
- `Stats` → `StatsScreen` — summary chips (total, avg/week, streaks), plus Canvas-drawn charts: 30-day daily, day-of-week, hour-of-day, 12-month trend, and top locations bar chart
- `Recipes` → `RecipesScreen` — expandable list of 10 hardcoded `BurritoRecipe` objects with ingredient checkboxes; shows a "Local Favorite" section at top derived from the user's country via GPS + Geocoder

### Data Layer

**Room Database** (`data/BurritoDatabase.kt`, version 2):
- `BurritoEntry` entity stores `id`, `timestamp`, `locationLat`, `locationLong`, `locationName`
- `BurritoDao` provides CRUD plus aggregated SQLite queries (day-of-week, hour-of-day, monthly, top locations, distinct days) — these avoid loading all rows by doing grouping in SQL

**DataStore Preferences** (`data/AppPreferencesRepository.kt`):
- `dark_mode`, `show_location_modal`, `notifications_enabled`, `notification_permission_asked` (Booleans)
- `three_day_notified`, `seven_day_notified` (Boolean flags reset after a burrito is logged)
- `checked_ingredients` (StringSet) — keys are `"${recipeId}_${ingredientIndex}"`, persisted across sessions

### Stats Screen data flow

`StatsViewModel` combines two parallel `combine` flows to avoid the 5-argument `combine` limit:
1. `summaryFlow` — total count + distinct days → current/best streak, avg/week
2. `chartFlow` — daily entries + DOW/hour/monthly/location aggregates → chart data lists

All charts are drawn with `Canvas` + tap detection via `pointerInput`/`detectTapGestures` to show a floating count bubble on bar tap.

### Recipes Screen

All 10 recipes are hardcoded in `RecipesViewModel.kt` as `allRecipes: List<BurritoRecipe>`. Each recipe has `countryCodes: List<String>`. On init, `RecipesViewModel` uses `FusedLocationProviderClient.lastLocation` + `Geocoder` to resolve the device's country code and surfaces the matching recipe as "Local Favorite" at the top of the list.

### Notifications & Background Work

- `BurritoReminderWorker` runs every 12 hours via WorkManager; checks days since last entry and sends reminders at 3-day ("Time for a burrito!") and 7-day ("Missing burritos?") thresholds
- `BurritoNotificationManager` creates the notification channel and sends the two reminder notification types
- The 7-day notification includes an intent extra (`EXTRA_OPEN_MAP`) to open the map screen directly
- `EatABurrita.kt` initializes the notification channel and schedules the periodic worker on app start
- `TimerScreen` requests `POST_NOTIFICATIONS` permission on first launch

### Location

- `LocationService` wraps `FusedLocationProviderClient` into a `callbackFlow`, emitting `LatLng` every 10 seconds
- `GetLocationUseCase` is the entry point; injected via Hilt (`LocationModule`)
- `MapScreenViewModel` collects the flow and manages camera position state
- Runtime permissions handled with Accompanist Permissions library
- `LocationPermissionBanner` (`ui/components/`) is a reusable composable for surfacing location permission prompts

### Google Maps Integration

- `MapScreen` uses `GoogleMap` composable with `MarkerInfoWindowComposable` for restaurant markers
- Places API searches for Mexican restaurants/cafes/meal takeaways within 50km
- Google Maps API key is stored in `secrets.properties` (not committed) and injected via the Secrets Gradle Plugin

### Theming

`ui/theme/` defines a Material 3 color scheme (orange/purple/blue palette) with custom extended colors via `CompositionLocalProvider`. Supports dark mode (toggled via Settings). Dynamic color is disabled.

### Helpers (`helpers/`)

- `format.kt` — date/duration formatting, address formatting
- `distance.kt` — `distanceBetweenInMiles()` between two `LatLng` points
- `shareOptions.kt` — random share message generation with stats
- `topBarHeight.kt` — status bar height utility
- `hasLocationPermissions.kt` — `Context.hasLocationPermission()` extension

## Key Files

| File | Role |
|------|------|
| `EatABurrita.kt` | `@HiltAndroidApp` Application class; sets up notification channel + worker |
| `MainActivity.kt` | Entry point; edge-to-edge + Compose setup; handles open-map intent extra |
| `Navigation.kt` | Nav graph definition (Home, Map, Settings, Stats, Recipes) |
| `ui/main/TimerScreen.kt` + `TimeScreenViewModel.kt` | Home screen |
| `ui/main/LocationPickerModal.kt` | Modal for tagging location when logging a burrito |
| `ui/main/DayLocationModal.kt` | Modal showing locations for a chart bar tap |
| `ui/map/MapScreen.kt` + `MapScreenViewModel.kt` | Map screen |
| `ui/settings/SettingsScreen.kt` + `SettingsViewModel.kt` | Settings screen |
| `ui/settings/LocationEditModal.kt` | Modal for editing a past entry's location via Places API |
| `ui/stats/StatsScreen.kt` + `StatsViewModel.kt` | Stats screen with Canvas charts |
| `ui/recipes/RecipesScreen.kt` + `RecipesViewModel.kt` | Recipes screen; `allRecipes` list lives here |
| `ui/components/LocationPermissionBanner.kt` | Reusable location permission banner |
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
