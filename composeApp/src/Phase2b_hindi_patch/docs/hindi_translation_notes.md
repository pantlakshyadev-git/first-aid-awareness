# Hindi language support: how it works and what is still a draft

## What the user sees
- A language switch: the **हि / EN** button in the top bar, and a **Language / भाषा** card on the Home screen.
- Menus, buttons, titles and messages switch to Hindi immediately. Answers already chosen are kept.
- A notice on the Home screen (Hindi mode) and a note on the About screen say that the Hindi text is a **draft that has not been checked by a medical or Hindi-language expert**.
- Parts that are not translated yet are shown in English instead of being blank.

## How it is built
| Part | File | Idea |
|---|---|---|
| Fixed interface text | `UiStrings.kt` | The English text is the key. `tr("English text")` returns the Hindi text, or the English text if no entry exists. |
| Content (situations, lessons, kit, quiz) | `composeResources/files/first_aid_data_hi.json` | Only the text, keyed by the same ids as the English data. It is laid over the English data when the app starts (`withTranslation` in `Translation.kt`). |
| Rules and answers | `first_aid_data.json` only | Rules, answer values, pathway ids and sources exist **once**, so the Hindi version cannot behave differently from the English one. |

## Safety checks (unit tests, `HindiDataTest.kt`)
- Every answer combination gives the **same pathway** in English and Hindi.
- Every pathway has the **same number of steps**, in the same order.
- The emergency number **112** is still present wherever the English step has it, and every **number** (5 back blows, 20 minutes, 1 year) is unchanged.
- Every fixed interface text used in the code has a Hindi entry, with the same `{0}` placeholders.
- Hindi search works (for example "सीपीआर", "जलना") and English search words still work.

## Status
| Content | Hindi |
|---|---|
| Interface text (menus, buttons, messages, About, Emergency) | Draft translation done |
| 6 guided situations (questions, answers, pathways, disclaimers) | Draft translation done |
| Lessons (5) | Not translated yet, shown in English |
| First-aid kit items (10) | Not translated yet, shown in English |
| Quiz (20 questions) | Not translated yet, shown in English |
| Source titles and links | Intentionally English (they are the names of the organisations and pages) |

## Review checklist for a Hindi speaker (ideally someone with first-aid training)
1. Read each situation in Hindi next to the English. Does every step mean the same thing?
2. Are the words natural for ordinary people (for example "सीपीआर (CPR)", "एईडी (AED)", "टूर्निकेट")?
3. Is any instruction easier to misunderstand in Hindi than in English?
4. Write corrections in `first_aid_data_hi.json` (text only). Do not change keys, option values or the number of steps. Run `.\gradlew.bat :composeApp:desktopTest` afterwards.

## For the viva
- Why one rules file for two languages? So the safety logic cannot drift apart, and tests can prove it.
- Why a draft label? Machine-assisted medical translation must be reviewed before it is trusted.
- What would you do next? Translate lessons, kit and quiz, then have the whole Hindi text reviewed.
