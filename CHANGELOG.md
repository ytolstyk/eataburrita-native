# Changelog

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
