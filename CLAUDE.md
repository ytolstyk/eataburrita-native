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

**Eat-a-Burrita** is a two-screen Android app that tracks burrito consumption and shows nearby Mexican restaurants on a map.

- **Min SDK:** 35, **Target/Compile SDK:** 36
- **Language:** Kotlin, **UI:** 100% Jetpack Compose + Material 3
- **Architecture:** MVVM with Hilt dependency injection
- **Package:** `com.tolstykh.eatABurrita`

## Architecture

### Screens & Navigation

Type-safe Compose Navigation (`Navigation.kt`) with two serializable routes:
- `Home` → `TimerScreen` — shows time since last burrito and total count
- `Map` → `MapScreen` — Google Maps with nearby Mexican restaurant markers

### Data Layer

- **DataStore Preferences** (no database) stores `burrito_counter` (Int) and `timestamp` (Long)
- `TimeScreenViewModel` reads/writes DataStore via `EatABurrita.dataStore` extension on the Application class
- **No remote API calls** for app data — only Google Maps/Places APIs for the map screen

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

`ui/theme/` defines a Material 3 color scheme (orange/purple/blue palette) with custom extended colors via `CompositionLocalProvider`. Dynamic color is disabled.

### Helpers (`helpers/`)

- `format.kt` — date/duration formatting, address formatting
- `distance.kt` — `distanceBetweenInMiles()` between two `LatLng` points
- `shareOptions.kt` — random share message generation
- `topBarHeight.kt` — status bar height utility
- `hasLocationPermissions.kt` — `Context.hasLocationPermission()` extension

## Key Files

| File | Role |
|------|------|
| `EatABurrita.kt` | `@HiltAndroidApp` Application class; provides `dataStore` singleton |
| `MainActivity.kt` | Entry point; edge-to-edge + Compose setup |
| `Navigation.kt` | Nav graph definition |
| `ui/main/TimerScreen.kt` + `TimeScreenViewModel.kt` | Home screen |
| `ui/map/MapScreen.kt` + `MapScreenViewModel.kt` | Map screen |
| `location/LocationService.kt` | Flow-based location updates |
| `location/LocationModule.kt` | Hilt bindings for location |
| `gradle/libs.versions.toml` | All dependency versions (version catalog) |

## API Keys

Add a `secrets.properties` file at the project root (not committed to git):
```
MAPS_API_KEY=your_google_maps_api_key_here
```
