# AGENTS.md

## Cortex — recovery elapsed-time counter

Android-first Kotlin Multiplatform/Compose app. **One global counter**, counted
**up** from a start timestamp. The user supplies a title and a start date/time
down to the minute; past and current times are allowed, future times are not.
App shows days, hours, minutes, seconds, and subseconds. The home-screen widget is the primary
surface and shows whole days plus `HH:MM:SS`; subseconds are app-only.

This is an addiction-recovery tool. Treat counter titles and timestamps as
sensitive data.

## Build harness

The build harness is adapted from `../asr`. Keep ASR's verified tool versions
from `gradle/libs.versions.toml` unless there is a concrete reason to upgrade.
Do **not** copy ASR domain code: `shared/core` task/habit/tag classes,
`TodayItems`, Today/tasks/habits UI, or the ASR list widget.

Android SDK and Gradle caches are shared outside this repo:

- `../.android-sdk`
- `../.gradle-home`

`scripts/gradlew` and `scripts/patch-aapt2.sh` default to those parent paths.
Override with `ANDROID_HOME` / `GRADLE_USER_HOME` only when needed. Neither
directory belongs in git. `.gradle/`, `build/`, and `**/build/` are also
generated and ignored.

`versionCode` is `git rev-list --count HEAD`, falling back to `1` before the
first commit.

## Commands

Always use the wrapper script on NixOS. It auto-resolves JDK/Nix loader paths,
uses the shared parent SDK/Gradle dirs, and patches AAPT2.

```bash
./scripts/gradlew assembleDebug
./scripts/gradlew installDebug
./scripts/gradlew :desktopApp:run
./scripts/gradlew test :shared:core:jvmTest :shared:ui:jvmTest
./scripts/gradlew :shared:core:jvmTest --tests "com.cortex.core.ElapsedBreakdownTest"
```

- Plain `./gradlew` skips NixOS bootstrap and AAPT2 patching.
- Never use `nix develop --command` or `nix develop --print-env` in OpenCode;
  they produce no usable output here.
- AAPT2 override must be a project property (`-P`), never `-D`.
- `:desktopApp` is the fast iteration surface. It must stay buildable, but
  Android remains the primary target.
- If the transform cache is corrupt, delete only
  `../.gradle-home/caches/*/transforms/`; do not wipe the whole cache.

**Debug vs release:** `installDebug` uses `com.cortex.debug`; `installRelease`
uses `com.cortex`. Never run `adb uninstall com.cortex` — it deletes the
recovery history. Test with the debug id instead.

Release workflow inherited from ASR:

```bash
# bump app.versionName in gradle.properties first
./scripts/gradlew assembleRelease
export GH_TOKEN=$(cat .secret)
gh release create v<version> --title "v<version>" --notes "## Highlights" \
  && gh release upload v<version> androidApp/build/outputs/apk/release/androidApp-release.apk
```

ASR signs release with the debug key for sideloading. Use a real keystore
before publishing anywhere public.

## Module structure

```
:shared/core  — counter model, elapsed-time math, storage interfaces
:shared/ui    — Compose screen, ViewModel, dark digital theme
:androidApp   — Room, DI, widget, Android entry point
:desktopApp   — JSON/in-memory stub, desktop preview surface
```

Persistence is a single row (`id = 1`) holding title + `startEpochMillis`.
Android uses Room; desktop uses a JSON file for preview. The app and widget must share
one Room singleton (`@Volatile` accessor), otherwise the widget reads stale data.

Use UDF/MVI-lite: sealed actions -> ViewModel -> repository interface, with
`StateFlow` state. Use Koin annotations. No-op desktop implementations must not
carry `@Single`, or they will override the real platform bindings.

Keep `androidResources.enable = true` in `shared/ui`; without it Compose
resources are silently excluded from the APK. Verify the generated Compose
resources package after the first build — it may not match the Android
namespace (ASR's is `asr.shared.ui.generated.resources`).

## Widget (primary surface)

Use raw `RemoteViews`, not Glance. ASR's Samsung One UI testing found Glance and
broadcast `PendingIntent` templates unreliable; raw views with an activity
`PendingIntent` work.

- One counter means a simple layout: title, days, `Chronometer`, optional
  progress. No `RemoteViewsService`/`ListView` unless multiple counters are
  explicitly requested later.
- Tapping the widget opens `MainActivity` via an activity `PendingIntent`; no
  trampoline activity is needed for a read-only counter.
- **Widgets cannot reliably animate at sub-second speed.**
  `updatePeriodMillis` has a 30-minute minimum; rapid `updateAppWidget` loops
  are throttled and battery-hostile. Do not use a foreground service to animate
  the widget.
- `Chronometer` provides the per-second movement. Its base uses the
  `SystemClock.elapsedRealtime()` timebase, not epoch millis:

  ```text
  base = SystemClock.elapsedRealtime() - (wallClockNow - startMillis) % 86_400_000
  ```

  Render whole days as separate text, then hours/minutes/seconds from the
  chronometer. Recompute the base on widget update, reboot, clock change,
  timezone change, and app edit.
- Schedule the next local-midnight refresh with an exact alarm and fall back to
  an inexact alarm when exact-alarm permission is denied. Reschedule on boot.
- Refresh the widget when the counter is created, reset, or edited.
- Keep widget fonts/layout static; custom bundled fonts are supported on
  minSdk 35, but avoid downloadable fonts.

## Time math

- The start date/time picker is limited to **past or current** times, with
  minute precision. Do not add future dates or countdown mode unless explicitly
  asked.
- Capture the chosen `startEpochMillis` once on explicit create/reset and
  persist it immediately. Never recapture it on recomposition, navigation, or
  app restart.
- Clamp any future start to the current clock defensively, and show validation
  in the UI.
- Elapsed = `nowMillis - startEpochMillis`, clamped to zero for clock rollback.
- Compute the breakdown in a pure function taking `startMillis` and
  `nowMillis`. Use duration-based 24-hour days; timezone/DST changes must not
  alter elapsed time.
- Use `kotlin.time.Clock.System`, not `kotlinx.datetime.Clock.System`.
- In the app, derive the value from the wall clock on each frame; do not
  accumulate ticks, or the display will drift.

## UI

Dark digital counter only; do not add a light theme or theme toggle unless
asked. Use a near-black background, high-contrast accent, and a bundled
monospace/tabular-digit font with fixed-width digits to prevent layout jitter.
Show days/hours/minutes/seconds plus centiseconds (two digits) at frame rate;
three millisecond digits are unreadable at 60 fps. Add content descriptions for
screen readers.

## Privacy

No analytics, no logging of titles or start timestamps, and no notification
text that reveals recovery details. Consider disabling Android backup for this
app. Keep any future export/import feature explicit and local-only.

## Testing

```bash
./scripts/gradlew :shared:core:jvmTest --tests "com.cortex.core.ElapsedBreakdownTest"
./scripts/gradlew :androidApp:testDebugUnitTest --tests "com.cortex.widget.*"
```

Test the pure breakdown at zero, 999 ms, 1 s, 59 s, 1 min, 1 h, 24 h,
multi-day, and clock rollback. Test the widget `Chronometer` base conversion
separately. If UI tests are copied from ASR, keep the headless Skiko JVM args
(`-Dorg.jetbrains.skiko.headless=true`) or they fail on NixOS. Robolectric is
available for RemoteViews tests.

## References

See `../asr/AGENTS.md` for deeper ASR rationale. Copy its infrastructure
patterns, not its task/habit rules or product behavior.
