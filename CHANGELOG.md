# Changelog

## [Unreleased]
### Added
- Quick Log home screen widget (2×2): shows total burrito count and an "Eat!" button that instantly logs a new entry, skipping all modals
- Stats home screen widget (4×1): pill-shaped widget with an orange "Eat!" button on the left, a stats panel in the center that auto-cycles through 5 stats (total, streak, best streak, avg/week, calories) every 5 seconds, and a blue Map button on the right; tapping the stats opens the app, tapping Map opens the map screen
- Burrito Roulette — "🎰 Burrito Roulette" FAB on the Map screen spins a slot-machine animation through nearby restaurants and picks a random one; Navigate button opens Google Maps directions to the winner

### Changed
- Burrito scanner now uses Gemini 1.5 Flash (cloud vision API) instead of the local TFLite model — delivers witty, burrito-oracle commentary; images are compressed to 512px before sending to reduce token cost
- Scanner is rate-limited to once every 30 seconds; attempting sooner shows a "still digesting" message
- API unavailability shows a "brain out of money" dialog with a Donate button instead of a generic error

### Removed
- Bundled `food_V1.tflite` model (20 MB) — APK shrinks accordingly

## [1.21] - 2026-06-06
### Fixed
- Camera button icon now correctly shows black in dark mode (was using system dark mode instead of app dark mode setting)

### Changed
- Burrito scanner now uses the Google food_V1 TFLite model (2023 food labels) via TensorFlow Lite Task Vision — fully on-device, no API key, works offline

## [1.20] - 2026-06-06

### Added
- Burrito Photo Log — attach an optional photo (camera or gallery) to each burrito entry; thumbnails shown in the entry history (Settings) and on the map location tray
- Memories screen — photo grid of all logged burrito photos, accessible from the Home menu and Stats screen; tap a photo for a full-screen view with date, location, and delete option (removes only the photo, keeps the entry)
- Restaurant rating displayed on the map selection card under the restaurant name
- Share card — visual card generated when sharing burrito stats
- Particle effects on the home screen
- Geofence Radar notifications — alerts when near one of your top 3 favorite burrito spots (requires background location)
- Streak Milestone notifications — push notification when hitting 7, 14, 30, or 50-day consecutive streaks
- Weekly Recap notifications — Monday morning digest of last week's burrito count
- Granular notification settings — each notification type (burrito reminders, nearby spot alerts, streak milestones, weekly recap) can be toggled independently in Settings

### Fixed
- Map no longer stays stuck at 0,0 on open due to a race condition between camera position saving and initial centering

## [1.18] - 2026-06-05

### Added
- Achievements system
- More sharing options

## [1.17] - 2026-06-05

### Added
- Burrito facts

## [1.16] - 2026-05-XX

### Added
- Calories calculator
- Burrito-or-not camera feature

## [1.15] - 2026-03-31

### Added
- Recipes view with browsable burrito recipes
- Recipe checkboxes for tracking ingredients/steps

### Fixed
- Font sizes adjusted for better readability

## [1.14] - 2026-03-30

### Fixed
- Notification frequency reduced to avoid sending too many reminders

## [1.13] - 2026-03-26

### Fixed
- Crash when there are zero burrito records

### Changed
- Added stats and null safety improvements

## [1.12] - 2026-03-26

### Added
- Custom notification icon
- More share messages

### Changed
- Graph refreshes automatically when the day changes

## [1.11] - 2026-03-25

### Fixed
- Skip button checkbox state in the eat/log modal

## [1.10] - 2026-03-25

### Changed
- Version bump

## [1.9] - 2026-03-25

### Added
- Places search in the eat/log modal

### Changed
- "Cancel" renamed to "Skip"

### Fixed
- Crash on startup

## [1.8] - 2026-03-25

### Fixed
- App crash for users with many burrito entries per day when opening the day modal
- Day modal now caps displayed entries at 10 to prevent layout overflow
- Day modal total count now includes all entries for the day, not just those with locations
- Day modal no longer leaves blank space when there are no location entries to show

### Changed
- Day modal count text is larger and easier to read

## [1.7] - 2026-03-24

### Changed
- Restricted future date selection when logging a burrito

## [1.6] - 2026-03-24

### Changed
- Version bump

## [1.5] - 2026-03-24

### Added
- Debug symbol table in release builds

## [1.4] - 2026-03-24

### Added
- Location tracking per burrito entry — pick a location when logging a burrito
- Location picker modal with map-based selection
- Day location modal to view location logged for a given day
- Location edit modal in Settings to update saved locations
- Navigation fallback on Map screen for cases where directions cannot be launched

### Improved
- Settings screen layout and usability improvements
- Timer screen UI refinements
- Build warning fixes

## [1.3] - 2026-03-24

- Initial tracked release
