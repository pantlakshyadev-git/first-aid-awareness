# Code map and viva preparation

Every team member should be able to explain this page in their own words.

## 1. The big picture
The program has three layers, and each file does one job.

| Layer | Files | Job |
|---|---|---|
| Data | `data/first_aid_data.json` | All first-aid content: situations, questions, rules, pathways, lessons, kit items, quiz, sources |
| Logic (`core/`) | `knowledge_base.py`, `rule_engine.py`, `quiz_engine.py`, `search_engine.py`, `validator.py` | Load the data, choose a pathway, make quizzes, search, check the data |
| Screens (`gui/`) | `app.py` plus one file per page | Show things and react to clicks |

`main.py` just creates `FirstAidApp` and starts the window.

## 2. What happens at start-up
1. `main.py` creates `FirstAidApp` (`gui/app.py`).
2. The app loads the JSON (`KnowledgeBase`), creates the engines, and runs `DataValidator.validate()`. If the data is wrong, it stops with a clear error.
3. It builds all 12 pages and stacks them in the same grid cell.
4. `show("home")` raises the Home page with `tkraise()` and calls that page's `refresh()`.

## 3. Guided First Aid (the main feature)
1. Situations page: the user clicks a card, which calls `app.start_situation(id)`.
2. Assessment page: shows one question at a time and stores answers in the dictionary `app.answers`.
3. On the last question, `submit_assessment()` calls `RuleEngine.evaluate(situation, answers)`.
4. The rule engine goes through the rules **in order**. The first rule whose conditions all match the answers wins. If none match, it uses `default_pathway`, which is "urgent" in every situation, so unexpected answers escalate.
5. Guidance page shows the pathway: what to do, when to get professional help, and the sources it is based on.

## 4. Other features in one line each
- **Quiz:** `make_quiz` picks 5 random questions, copies each one and shuffles its options. After each answer a pop-up shows right or wrong plus an explanation. The result page works out the percentage.
- **Search:** splits the query into words and counts how many match the start of a word in each situation, lesson or kit item. Results are sorted by score, then title.
- **Kit Explorer:** shows each item. The picture is loaded from `assets/kit/<item id>.png`. If the file is missing, a placeholder text appears instead of a crash.
- **Tests:** 24 unit tests in `tests/` check the rule engine (including every answer combination), search, quiz, data validity and kit images.

## 5. Mock viva questions (answer in your own words)
1. Walk me through what happens from clicking "Burns" to seeing the guidance.
   *Points: `start_situation`, answers dictionary, `RuleEngine.evaluate`, first matching rule, default pathway, `GuidancePage`.*
2. Why is the content in a JSON file and not written inside the code?
   *Points: change content without editing code, the validator can check it, easy to review with a doctor.*
3. What happens if no rule matches? Why "urgent"?
   *Points: `default_pathway`, safe failure, an unexpected answer should escalate.*
4. Why a rule-based system and not AI?
   *Points: predictable, testable, explainable, does not claim to diagnose, suits a first-year project.*
5. What does the validator protect you from?
   *Points: typos in the JSON, a rule pointing to a missing pathway, a quiz answer not in its options.*
6. How did you test it? What do your tests not cover?
   *Points: unit tests for logic. Not covered: how the screens look, and real users. That is why we did user testing.*
7. What are the limits of the project?
   *Points: educational only, English only, desktop only, content summarised from the cited sources, not reviewed by a doctor (unless you get one to check it).*
8. Who is it for, and how do you know?
   *Points: your interviews and survey results. This is the question the design-thinking marks depend on.*
9. Why not use an existing app?
   *Points: your comparison table. Be honest about what is the same and what is different.*
10. What would you do next?
    *Points: Hindi option, mobile version, expert review, more situations.*

## 6. Demo script (3 minutes)
1. Home screen: say who it is for and the problem (20 s).
2. Emergency Help: show 112 (15 s).
3. Guided First Aid → Burns → answer the questions → show the result and its sources (60 s).
4. Kit Explorer: open one item (20 s).
5. Quiz: answer two questions and show the explanation pop-up (30 s).
6. Close: what users told you, and what you changed because of it (35 s).

## 7. Honest limitations (say these before they are asked)
- The Tkinter window was not tested on every computer. Fonts are chosen for Windows.
- If the JSON file is missing, the app cannot start.
- The quiz category filter exists in the code, but the screen always uses "All".
