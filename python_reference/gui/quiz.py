import tkinter as tk
from tkinter import ttk, messagebox
from gui.common import add_header, add_action_bar

class QuizPage(ttk.Frame):
    def __init__(self, app):
        super().__init__(app)
        self.app = app
        self.questions = []
        self.index = 0
        self.score = 0
        self.var = None
        self.category = "All"

    def start_quiz(self, category):
        self.category = category
        self.questions = self.app.quiz_engine.make_quiz(category, 5)
        self.index = 0
        self.score = 0
        self.var = None

    def refresh(self):
        for w in self.winfo_children():
            w.destroy()
        if not self.questions:
            self.start_quiz(self.category)
        add_header(self, "Knowledge Quiz",
                   f"Category: {self.category}  •  Question {self.index + 1} of {len(self.questions)}",
                   "?")
        q = self.questions[self.index]
        content = ttk.Frame(self)
        content.pack(fill="both", expand=True, padx=28)

        box = tk.Frame(content, bg="#ffffff", highlightbackground="#dbe2ea",
                       highlightthickness=1)
        box.pack(fill="x", pady=12)
        inner = tk.Frame(box, bg="#ffffff")
        inner.pack(fill="both", padx=24, pady=24)

        tk.Label(inner, text=q["question"], bg="#ffffff", fg="#172033",
                 font=("Segoe UI", 15, "bold"), wraplength=850,
                 justify="left").pack(anchor="w", pady=(0, 18))

        self.var = tk.StringVar(value="")
        for option in q["options"]:
            ttk.Radiobutton(inner, text=option, value=option,
                            variable=self.var).pack(anchor="w", pady=7)

        bar = ttk.Frame(content)
        bar.pack(fill="x", pady=12)
        ttk.Button(bar, text="Exit Quiz",
                   command=lambda: self.app.show("home")).pack(side="left")
        ttk.Button(bar, text="Next →" if self.index < len(self.questions)-1 else "Finish",
                   style="Primary.TButton", command=self.next_question).pack(side="right")

    def next_question(self):
        if not self.var or not self.var.get():
            messagebox.showwarning("Select an answer", "Please choose an option.")
            return
        q = self.questions[self.index]
        correct = self.app.quiz_engine.check(q, self.var.get())
        if correct:
            self.score += 1
        prefix = "" if correct else f"Correct answer: {q['answer']}\n\n"
        messagebox.showinfo("Correct!" if correct else "Not quite",
                            prefix + q.get("explanation", ""))
        self.index += 1
        if self.index >= len(self.questions):
            self.app.frames["quiz_result"].set_result(self.score, len(self.questions),
                                                      self.category)
            self.app.show("quiz_result")
        else:
            self.refresh()
