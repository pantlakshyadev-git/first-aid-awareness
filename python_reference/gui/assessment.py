import tkinter as tk
from tkinter import ttk, messagebox
from gui.common import add_header, add_action_bar, BG, CARD, BORDER

class AssessmentPage(ttk.Frame):
    def __init__(self, app):
        super().__init__(app)
        self.app = app
        self.index = 0
        self.vars = {}

    def refresh(self):
        for w in self.winfo_children():
            w.destroy()

        situation = self.app.current_situation
        if not situation:
            self.app.show("situations")
            return

        self.index = 0
        self.vars = {}
        add_header(self, situation["title"], situation.get("assessment_intro", ""),
                   situation.get("icon", "✚"))
        self.content = ttk.Frame(self)
        self.content.pack(fill="both", expand=True, padx=28)
        self.render_question()

    def render_question(self):
        for w in self.content.winfo_children():
            w.destroy()

        questions = self.app.current_situation["questions"]
        q = questions[self.index]
        total = len(questions)

        progress = ttk.Label(self.content,
                             text=f"Question {self.index + 1} of {total}",
                             style="Section.TLabel")
        progress.pack(anchor="w", pady=(8, 12))

        outer = tk.Frame(self.content, bg=CARD, highlightbackground=BORDER,
                         highlightthickness=1)
        outer.pack(fill="x", pady=5)
        inner = tk.Frame(outer, bg=CARD)
        inner.pack(fill="both", padx=24, pady=24)

        tk.Label(inner, text=q["text"], bg=CARD, fg="#172033",
                 font=("Segoe UI", 15, "bold"), wraplength=850,
                 justify="left").pack(anchor="w", pady=(0, 18))

        var = tk.StringVar(value=self.app.answers.get(q["id"], ""))
        self.vars[q["id"]] = var
        for option in q["options"]:
            ttk.Radiobutton(inner, text=option["label"], value=option["value"],
                            variable=var).pack(anchor="w", pady=7)

        bar = ttk.Frame(self.content)
        bar.pack(fill="x", pady=(18, 8))
        ttk.Button(bar, text="← Back",
                   command=self.back).pack(side="left")
        if self.index < total - 1:
            ttk.Button(bar, text="Next →", style="Primary.TButton",
                       command=self.next).pack(side="right")
        else:
            ttk.Button(bar, text="Show Guidance", style="Primary.TButton",
                       command=self.finish).pack(side="right")

    def save_current(self):
        q = self.app.current_situation["questions"][self.index]
        value = self.vars[q["id"]].get()
        if not value:
            return False
        self.app.answers[q["id"]] = value
        return True

    def next(self):
        if not self.save_current():
            messagebox.showwarning("Select an option",
                                      "Please choose an answer before continuing.")
            return
        self.index += 1
        self.render_question()

    def back(self):
        if self.index == 0:
            self.app.show("situations")
        else:
            self.save_current()
            self.index -= 1
            self.render_question()

    def finish(self):
        if not self.save_current():
            messagebox.showwarning("Select an option",
                                      "Please choose an answer before continuing.")
            return
        self.app.submit_assessment()
