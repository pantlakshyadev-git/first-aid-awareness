# First Aid Awareness — Kotlin version (Phase 1: bug-fix release)

An offline-first first-aid **awareness** app for the Innovation & Design Thinking Lab (AID151/AID251).
It has a Kotlin / Compose Multiplatform app (Android, desktop, iOS code) and the original Python/Tkinter
version in `python_reference/`, kept as a behaviour reference.

## Status (be honest in your viva)

- Phase 1 fixes are in the code, but **the Kotlin project has not been compiled by the person who prepared
  these changes** (no Android SDK or internet was available). Your first Gradle sync and test run is the
  first real verification. Fix anything it reports before presenting.
- **Android and Windows desktop** are the targets to demonstrate. The iOS entry point exists, but there is
  **no Xcode `iosApp` project** in this repository, so the app cannot be run on an iPhone yet.
- The first-aid content is a simplified summary of the cited sources and has **not been reviewed by a
  medical professional**. Do not present it as medically verified.

## One-time setup

1. Install **JDK 21**. In Android Studio: *Settings → Build, Execution, Deployment → Build Tools → Gradle →
   Gradle JDK* → choose JDK 21.
2. Install Android Studio with **Android SDK Platform 35**.
3. Create the Gradle wrapper once (the zip cannot contain the wrapper JAR). In a terminal, in the project root:

   ```
   gradle wrapper --gradle-version 9.5.0
   ```

   If `gradle` is not installed, open the project in Android Studio, let it sync, then open its Terminal and
   run `./gradlew wrapper --gradle-version 9.5.0` (or `.\gradlew.bat ...` on Windows PowerShell).
   This creates `gradlew`, `gradlew.bat` and `gradle/wrapper/gradle-wrapper.jar`. Commit all three.
4. Open the project folder in Android Studio and let Gradle sync finish (needs internet the first time).

## Run

| What | Command (from the project root) |
|---|---|
| Windows/Linux/macOS desktop app | `.\gradlew.bat :composeApp:run` (Windows) or `./gradlew :composeApp:run` |
| Android | Select the `composeApp` configuration in Android Studio, pick an emulator or a phone with USB debugging, press Run |
| Android debug APK | `.\gradlew.bat :composeApp:assembleDebug` → `composeApp\build\outputs\apk\debug\` |
| **Kotlin unit tests** | `.\gradlew.bat :composeApp:desktopTest` |
| Python version | `cd python_reference` then `py main.py` |
| Python unit tests | `cd python_reference` then `python -m unittest discover -s tests -p "test_*.py"` |

Test reports: `composeApp/build/reports/tests/desktopTest/index.html`.

## What the app does

Home · Guided First Aid (questions → awareness pathway → sources) · Learn · First-Aid Kit (with pictures) ·
Knowledge Quiz (category, 5/10/20 questions, feedback after each answer, score) · Search · Emergency Help
(tap-to-call 112 on phones) · Sources (clickable links) · About. An **Emergency** button is always visible
in the top bar. The Android Back button and the in-app Back button work on every screen.

## Safety

This app is for first-aid awareness and education. It is not a diagnostic tool and does not replace emergency
services, professional medical care or certified first-aid training. In India, call **112**.

## Before you submit

- Fill in `docs/image_credits.md` (source and licence of every kit picture).
- Fill in `docs/design_journal_template.md` with your real interviews, survey, problem statement, ideas and test results.
- Read `docs/kotlin_code_map_and_viva.md` and be able to explain each file in your own words.
- Do not zip the `.idea/`, `build/` or `.gradle/` folders (the `.gitignore` already excludes them).

## Phase 2 ideas (not done yet)

Saved quiz history and bookmarks, more situations (for example snakebite, seizure, poisoning, fainting),
recovery-position and hands-only CPR lessons **with cited sources and expert review**, Hindi text,
vector icons, an Xcode `iosApp` project, Compose Multiplatform version upgrade.
