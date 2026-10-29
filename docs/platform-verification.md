# Verification checklist

## Automated
- `./gradlew :composeApp:desktopTest` — Kotlin logic and bundled-data tests.
- `python -m unittest discover -s tests -p "test_*.py"` inside `python_reference/` — original tests.
- GitHub Actions (`.github/workflows/platform-builds.yml`) runs both test sets, then packages desktop, builds the
  Android debug APK and compiles the iOS simulator target. It needs the Gradle wrapper to be committed.
A green workflow proves the code builds and the tests pass. It does not prove the screens look right.

## Manual (Android phone and Windows desktop at least)
1. App starts and shows Home (no data error).
2. Guided First Aid → Burns: the *Show awareness guidance* button stays disabled until both questions are answered.
3. Open the guidance, press Back, change an answer, open the guidance again: the result changes accordingly.
4. Do the same for every situation; "Based on" lists sources.
5. Quiz: pick an answer (it locks), Next, finish, check the score, Try again.
6. Learn, Kit (pictures appear), Search ("bleed", "electricity", "gloves"), Sources (Open link), About.
7. Emergency button on every screen; **Call 112** opens the dialer on a phone (do not complete the call!).
8. Android: press the system Back button on a detail screen (returns), on Home (leaves the app). Rotate the phone
   during a quiz: progress stays. Check the top bar is not under the status bar and the bottom bar not under the gesture bar.
9. Narrow phone: bottom bar labels are fully readable. Wide window: side menu appears.
10. Airplane mode: everything except "Open link" still works.

## Platform notes
- iOS needs a Mac, Xcode and an `iosApp` Xcode project (not in this repository yet).
- Debian package names must be lowercase without spaces; the build sets `first-aid-awareness`.
Record failures with the toolchain versions. Do not call a platform verified because the code compiles.
