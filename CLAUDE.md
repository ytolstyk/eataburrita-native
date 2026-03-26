# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Build Commands

```bash
./gradlew assembleDebug          # Build debug APK
./gradlew assembleRelease        # Build release APK (minified + resource shrinking)
./gradlew installDebug           # Build and install on connected device/emulator
./gradlew test                   # Run unit tests
./gradlew connectedAndroidTest   # Run instrumented tests (requires device/emulator)
./gradlew clean                  # Clean build artifacts
./gradlew lint                   # Run static code analysis (Lint checks)
```

## Project Overview

**Eat-a-Burrita** is a three-screen Android app that tracks burrito consumption, shows nearby Mexican restaurants on a map, and manages app settings.

- **Min SDK:** 35, **Target/Compile SDK:** 36
- **Language:** Kotlin, **UI:** 100% Jetpack Compose + Material 3
- **Architecture:** MVVM with Hilt dependency injection
- **Package:** `com.tolstykh.eatABurrita`

## Architecture

### Screens & Navigation

Type-safe Compose Navigation (`Navigation.kt`) with three serializable routes:

- `Home` → `TimerScreen` — live timer, total count, favorite place, 30-day chart, Eat/Share/Map/Settings buttons
- `Map` → `MapScreen` — Google Maps with nearby Mexican restaurant markers
- `Settings` → `SettingsScreen` — dark mode, notifications, location modal toggle, entry history (edit/delete/add), reset

### Data Layer

**Room Database** (`data/BurritoDatabase.kt`, version 2):
- `BurritoEntry` entity stores `id`, `timestamp`, `locationLat`, `locationLong`, `locationName`
- `BurritoDao` provides queries for count, latest timestamp, entries since date, location entries, daily counts
- Migration 1→2 added location fields

**DataStore Preferences** (`data/AppPreferencesRepository.kt`):
- `dark_mode` (Boolean)
- `show_location_modal` (Boolean)
- `notifications_enabled` (Boolean)
- `notification_permission_asked` (Boolean)

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

| File                                                      | Role                                                                       |
| --------------------------------------------------------- | -------------------------------------------------------------------------- |
| `EatABurrita.kt`                                          | `@HiltAndroidApp` Application class; sets up notification channel + worker |
| `MainActivity.kt`                                         | Entry point; edge-to-edge + Compose setup; handles open-map intent extra   |
| `Navigation.kt`                                           | Nav graph definition (Home, Map, Settings)                                 |
| `ui/main/TimerScreen.kt` + `TimeScreenViewModel.kt`       | Home screen                                                                |
| `ui/main/LocationPickerModal.kt`                          | Modal for tagging location when logging a burrito                          |
| `ui/main/DayLocationModal.kt`                             | Modal showing locations for a chart bar tap                                |
| `ui/map/MapScreen.kt` + `MapScreenViewModel.kt`           | Map screen                                                                 |
| `ui/settings/SettingsScreen.kt` + `SettingsViewModel.kt`  | Settings screen                                                            |
| `ui/settings/LocationEditModal.kt`                        | Modal for editing a past entry's location via Places API                   |
| `data/BurritoDatabase.kt` + `BurritoDao.kt`              | Room database and DAO                                                      |
| `data/AppPreferencesRepository.kt`                        | DataStore-backed app preferences                                           |
| `data/DatabaseModule.kt`                                  | Hilt module for Room database                                              |
| `location/LocationService.kt`                             | Flow-based location updates                                                |
| `location/LocationModule.kt`                              | Hilt bindings for location                                                 |
| `notification/BurritoNotificationManager.kt`              | Notification channel creation and reminder dispatch                        |
| `worker/BurritoReminderWorker.kt`                         | Periodic WorkManager worker for burrito reminders                          |
| `gradle/libs.versions.toml`                               | All dependency versions (version catalog)                                  |

## API Keys

Add a `secrets.properties` file at the project root (not committed to git):

```
MAPS_API_KEY=your_google_maps_api_key_here
```

## Validations

Clean up unused imports, constants, functions before submitting.
