import tkinter as tk
from tkinter import ttk
from gui.common import add_header, add_action_bar, ScrollableFrame, CARD, BORDER

class LearnPage(ttk.Frame):
    def __init__(self, app):
        super().__init__(app)
        self.app = app
        self.lesson_id = None

    def open_lesson(self, lesson_id):
        self.lesson_id = lesson_id

    def refresh(self):
        for w in self.winfo_children():
            w.destroy()

        if self.lesson_id:
            self._detail()
        else:
            self._list()

    def _list(self):
        add_header(self, "Learn",
                   "Build the knowledge behind safe first-aid awareness.",
                   "▣")
        grid = ttk.Frame(self)
        grid.pack(fill="both", expand=True, padx=28)
        for i, lesson in enumerate(self.app.kb.lessons()):
            r, c = divmod(i, 2)
            widget = self._lesson_card(grid, lesson)
            widget.grid(row=r, column=c, sticky="nsew", padx=6, pady=6)
            grid.grid_columnconfigure(c, weight=1)

        add_action_bar(self, lambda: self.app.show("home"))

    def _lesson_card(self, parent, lesson):
        box = tk.Frame(parent, bg=CARD, highlightbackground=BORDER,
                       highlightthickness=1)
        inner = tk.Frame(box, bg=CARD)
        inner.pack(fill="both", padx=18, pady=17)
        tk.Label(inner, text=lesson.get("icon", "▣"), bg=CARD, fg="#1f5eff",
                 font=("Segoe UI Symbol", 20, "bold")).pack(anchor="w")
        tk.Label(inner, text=lesson["title"], bg=CARD, fg="#172033",
                 font=("Segoe UI", 13, "bold")).pack(anchor="w", pady=(4, 4))
        tk.Label(inner, text=lesson["summary"], bg=CARD, fg="#5f6b7a",
                 font=("Segoe UI", 9), wraplength=430,
                 justify="left").pack(anchor="w")
        ttk.Button(inner, text="Read →",
                   command=lambda lid=lesson["id"]: self._open(lid)).pack(anchor="w", pady=(12, 0))
        return box

    def _open(self, lesson_id):
        self.lesson_id = lesson_id
        self.refresh()

    def _detail(self):
        lesson = next(x for x in self.app.kb.lessons() if x["id"] == self.lesson_id)
        add_header(self, lesson["title"], lesson["summary"], lesson.get("icon", "▣"))
        scroll = ScrollableFrame(self)
        scroll.pack(fill="both", expand=True, padx=28)

        for heading, body in lesson["sections"]:
            box = tk.Frame(scroll.inner, bg=CARD, highlightbackground=BORDER,
                           highlightthickness=1)
            box.pack(fill="x", pady=7)
            inner = tk.Frame(box, bg=CARD)
            inner.pack(fill="both", padx=20, pady=16)
            tk.Label(inner, text=heading, bg=CARD, fg="#1748c7",
                     font=("Segoe UI", 11, "bold")).pack(anchor="w")
            tk.Label(inner, text=body, bg=CARD, fg="#42526a",
                     font=("Segoe UI", 10), wraplength=850,
                     justify="left").pack(anchor="w", pady=(5, 0))

        add_action_bar(self, self._back_to_list)

    def _back_to_list(self):
        self.lesson_id = None
        self.refresh()
