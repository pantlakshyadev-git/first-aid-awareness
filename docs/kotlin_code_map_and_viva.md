# Kotlin version: code map and viva preparation

Every team member should be able to explain this in their own words. (The Python version is explained in
`python_v1_code_map_and_viva.md`.)

## 1. How the Kotlin app is organised
See `architecture.md` for the file list. The idea is the same as the Python version:
**data (JSON) → logic → screens**. The logic functions in `Logic.kt` are ports of the Python files in `core/`.

| Python | Kotlin |
|---|---|
| `core/rule_engine.py` | `evaluatePathway()` |
| `core/validator.py` | `validateData()` |
| `core/search_engine.py` | `search()` and `tokenize()` |
| `core/quiz_engine.py` | `makeQuiz()` and `QuizSession` |
| `gui/*.py` | `Screens.kt`, `QuizScreen.kt`, `App.kt` |

## 2. What happens from tapping "Burns" to the result
1. `SituationsScreen` → `state.openSituation("burns")` clears old answers and pushes the Assessment screen.
2. `AssessmentScreen` shows every question. Each tap stores `answers[questionId] = value`.
3. The *Show guidance* button is enabled only when `allAnswered()` is true.
4. `GuidanceScreen` calls `evaluatePathway(situation, answers)`: the first rule whose conditions all match wins;
   otherwise the `default_pathway` (always the escalating one) is used.
5. Because the result is calculated each time the screen is drawn, it can never show an old answer.

## 3. Bugs found in the review, and the fix
| Problem | Fix |
|---|---|
| Result stayed on screen after an answer changed | Guidance is a separate screen computed from current answers |
| Guidance shown with unanswered questions (could advise CPR for a breathing person) | Button disabled until `allAnswered()`; guidance screen refuses incomplete answers |
| Result hidden below the button on phones | Result has its own screen |
| Sources never shown | "Based on" list on the guidance screen |
| Quiz answer could be changed after feedback; no result page | `QuizSession` locks the first choice; result screen added |
| Android Back button closed the app | `PlatformBackHandler` + back stack in `AppState` |
| Rotation lost progress | `configChanges` in the manifest |
| Bars under status/gesture bars on Android 15 | `enableEdgeToEdge` + window-inset padding |
| Bottom bar text too wide | Material `NavigationBar` with five short labels |
| Symbols could show as empty boxes | Letter badges instead of symbol fonts |
| No tap-to-call, links not clickable, kit pictures unused | `tel:112`, `openUri`, `KitImage` |
| 16 of 20 quiz answers were the longest option | Distractors rewritten; test guards against it |
| No Kotlin tests, CI ran none | `LogicTest.kt`, `BundledDataTest.kt`, CI runs them |
| `.idea/`, local paths in the zip; Debian package name with spaces | `.gitignore`, `linux { packageName = ... }` |

## 4. Mock viva questions
1. Why did you port the Python logic to Kotlin instead of rewriting it? *(same tested behaviour, one JSON for both)*
2. What is `expect`/`actual` and where do you use it? *(`Platform.kt`: phone calls and Back button differ per platform)*
3. Why is guidance computed in the screen and not stored? *(cannot become stale)*
4. Why does the default pathway always escalate? *(unknown or missing input must not reassure)*
5. What do your unit tests cover and not cover? *(logic and data; not how the screens look)*
6. What did you change after reviewing your own prototype? *(table above — this is your "iterate" evidence for the course)*
7. What would a medical professional need to check? *(every pathway, the quiz answers, the missing topics)*

## 5. Be ready to say
- Which parts you wrote, which parts an AI tool helped with, and that you tested and understood them.
- What is not done: iOS app project, expert review, more situations, Hindi.
