# Cortex

Recovery elapsed-time counter for Android, with a home-screen widget as the
primary surface.

Cortex tracks one global counter that counts up from a start date/time you
choose. The start can be current or backdated to any past minute; future times
are rejected. The app shows days, hours, minutes, seconds, and centiseconds.
The widget shows `DD:HH:MM:SS` with `DAY HOUR MINUTE SECOND` labels above it.

## Features

- One global counter with a title and minute-precision start date/time
- Counts up using duration-based 24-hour days, independent of timezone/DST
- Dark digital UI with monospace, fixed-width digits
- Compact 4x1 home-screen widget with per-second movement
- Optional custom widget background image with **Fit edges** or **Center crop** mode, stored locally
- No analytics, no cloud sync, no title/timestamp logging

## Requirements

- NixOS: `scripts/gradlew` auto-bootstraps the JDK and Nix loader paths
- Shared Android SDK at `../.android-sdk`
- Shared Gradle cache at `../.gradle-home`
- Android 15+ (`minSdk 35`)

## Build and run

```bash
./scripts/gradlew assembleDebug
./scripts/gradlew installDebug
./scripts/gradlew :desktopApp:run
./scripts/gradlew test :shared:core:jvmTest :shared:ui:jvmTest
```

- Use `./scripts/gradlew`, not plain `./gradlew`, on NixOS.
- `installDebug` installs `com.cortex.debug`; `installRelease` installs
  `com.cortex`.
- Never run `adb uninstall com.cortex`; it deletes the recovery history.
- Desktop is the fast preview surface. Android is the primary target.

## Widget

- Uses raw `RemoteViews`, not Glance.
- Days and hours are static text ending in `:`; `MM:SS` comes from
  `Chronometer`.
- The widget refreshes hourly (which also catches midnight) to update the
  day/hour text and reset the chronometer base. It also refreshes on boot,
  clock/timezone changes, and app edits.
- Pick a background with **Widget background** in the app. **Clear background**
  removes it. Images are copied into app-private storage and decoded to a small
  bitmap. **Fit edges** keeps the full width visible and fills the top/bottom
  gaps with the dominant color of each image edge; **Center crop** fills the
  widget by cropping. A dark scrim keeps the text readable. Images are never
  uploaded.

## Architecture

```text
:shared/core  — counter model, elapsed math, storage/widget interfaces
:shared/ui    — Compose screen, ViewModel, dark digital theme
:androidApp   — Room, Koin, widget, Android entry point
:desktopApp   — JSON storage, desktop preview
```

Persistence is a single row (`id = 1`) holding the title and
`startEpochMillis`. Android uses Room; desktop uses `~/.cortex/counter.json`.
The app and widget share one Room singleton.

The build harness and widget architecture are adapted from the ASR project in
`../asr`.

## Release

```bash
# bump app.versionName in gradle.properties first
./scripts/gradlew assembleRelease
export GH_TOKEN=$(cat .secret)
gh release create v<version> --title "v<version>" --notes "## Highlights" \
  && gh release upload v<version> androidApp/build/outputs/apk/release/androidApp-release.apk
```

The release build is signed with the debug key for sideloading. Use a real
keystore before publishing publicly.

## Privacy

This is an addiction-recovery tool. Counter titles and timestamps are sensitive.
There is no analytics, no title/timestamp logging, no notification text that
reveals recovery details, and no cloud upload. Android backup is disabled.
