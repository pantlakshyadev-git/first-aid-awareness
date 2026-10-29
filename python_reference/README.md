# First-Aid Awareness System — V0.4.1

A beginner-friendly Python/Tkinter desktop prototype for first-aid awareness.
Built for the Innovation & Design Thinking Lab (AID151/AID251). See `docs/design_journal_template.md`.

## Vision
LEARN → ASSIST → ESCALATE

## Main modules
Home · Guided First Aid · Learn · First-Aid Kit Explorer · Knowledge Quiz · Search · Emergency Help · Sources · About

## Technology
Python, Tkinter/ttk, JSON. Standard library only, no database, works offline.

## Run
```bash
python main.py        # or: py main.py on Windows
```

## Tests
```bash
python -m unittest discover -s tests -p "test_*.py"
```

## Important
Educational prototype only. Not a diagnostic tool and not a replacement for emergency services,
a certified first-aid course or professional medical care. In India, call **112**.

## V0.4.1 changes
- Kit Explorer shows a picture for each item (`assets/kit/<item id>.png`). If an image is missing the page still works.
- Images renamed after their item ids and resized once (about 0.8 MB in total, was 2.1 MB).
- `kit.py` simplified; a new test checks that every kit item has an image file.
- Image sources are listed in `docs/image_credits.md`.
- `docs/code_map_and_viva.md` explains how the program works.

## V0.4.0 changes
- Bleeding, burns, choking and heart-attack pathways updated against the cited Red Cross / NHS pages
  (direct pressure for severe bleeding, burn cooling, 5 back blows + 5 abdominal thrusts, aspirin note).
- Cardiac-arrest rules: a responsive person with abnormal breathing now gets an emergency pathway.
- Search now covers lesson text and guidance text, and matches word starts only.
- Quiz: 20 questions, shuffled options, instant feedback with an explanation after every answer.
- Each guidance result shows the sources it is based on.
- Stronger data validator and 23 unit tests, including a check of every answer combination.

## V0.3.1 changes
- Heart Attack and Cardiac Arrest pathways, scrolling containers, India's 112 number, expanded content.
