from tkinter import ttk
from gui.common import add_header, add_action_bar

class QuizResultPage(ttk.Frame):
    def __init__(self, app):
        super().__init__(app)
        self.app = app
        self.score = 0
        self.total = 0
        self.category = "All"

    def set_result(self, score, total, category):
        self.score, self.total, self.category = score, total, category

    def refresh(self):
        for w in self.winfo_children():
            w.destroy()
        add_header(self, "Quiz Complete",
                   "Use the result as a learning checkpoint, not as a certification.",
                   "?")
        content = ttk.Frame(self)
        content.pack(fill="both", expand=True, padx=28)

        ttk.Label(content, text=f"{self.score} / {self.total}",
                  font=("Segoe UI", 34, "bold")).pack(pady=(50, 8))
        pct = round((self.score / self.total) * 100) if self.total else 0
        ttk.Label(content, text=f"Score: {pct}%  •  {self.category}",
                  style="Subtitle.TLabel").pack(pady=5)

        if pct >= 80:
            message = "Strong awareness foundation. Keep reviewing and practice through formal training."
        elif pct >= 50:
            message = "Good start. Revisit the Learn section and try again."
        else:
            message = "Use this as a learning checkpoint. Review the topics and try the quiz again."

        ttk.Label(content, text=message, style="Body.TLabel",
                  wraplength=700, justify="center").pack(pady=20)
        add_action_bar(self, lambda: self.app.show("home"),
                       "Try Again", lambda: self.app.start_quiz(self.category))
