# Architecture overview

All shared code is in `composeApp/src/commonMain/kotlin/com/ben/firstaid/`.

| File | Job |
|---|---|
| `Model.kt` | Typed data classes that match `first_aid_data.json`, plus constants (112, version). |
| `Logic.kt` | Pure logic with no UI: rule engine, data validator, search, quiz maker, `QuizSession`. Unit-tested. |
| `AppState.kt` | Screens, navigation back stack, answers, search text, quiz. Survives screen changes and rotation. |
| `App.kt` | Loads and validates the JSON, then draws the top bar, side/bottom navigation and the current screen. |
| `Screens.kt` | Home, Guided First Aid, Guidance, Learn, Kit, Search, Emergency, Sources, About. |
| `QuizScreen.kt` | Quiz setup, one question at a time, result. |
| `Ui.kt` | Shared building blocks: theme, cards, radio rows, tap-to-call button, kit image. |
| `Platform.kt` | `expect` declarations: can the device place calls, and the Android Back handler. |

Platform folders: `androidMain` (MainActivity, manifest, Back handler), `desktopMain` (window, main),
`iosMain` (view controller). Tests: `commonTest` (logic, with small hand-made data) and `desktopTest`
(the real bundled JSON and kit images).

Bundled content: `composeResources/files/first_aid_data.json` (must stay identical to
`python_reference/data/first_aid_data.json`) and `composeResources/drawable/kit/<item id>.png`.

Safety design: every situation's `default_pathway` is the escalating one, the guidance screen is computed from
the current answers every time it is drawn, and guidance is never shown until every question is answered.
Emergency help is one tap away on every screen.
