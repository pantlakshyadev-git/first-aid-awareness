import tkinter as tk
from tkinter import ttk
from gui.common import add_header, add_action_bar, ScrollableFrame, CARD, BORDER

class SourcesPage(ttk.Frame):
    def __init__(self, app):
        super().__init__(app)
        self.app = app

    def refresh(self):
        for w in self.winfo_children():
            w.destroy()
        add_header(self, "Sources & References",
                   "Educational content is adapted from the references below. It is a simplified summary, "
                   "not a substitute for a certified first-aid course.",
                   "§")

        scroll = ScrollableFrame(self)
        scroll.pack(fill="both", expand=True, padx=28)

        intro = tk.Frame(scroll.inner, bg="#eaf1ff")
        intro.pack(fill="x", pady=(0, 8))
        tk.Label(intro, text="Research principle",
                 bg="#eaf1ff", fg="#1748c7",
                 font=("Segoe UI", 11, "bold")).pack(anchor="w", padx=18, pady=(13, 4))
        tk.Label(intro,
                 text="Research → compare → simplify → cite → review. "
                      "The application is not intended to create medical advice from memory.",
                 bg="#eaf1ff", fg="#42526a", font=("Segoe UI", 10),
                 wraplength=850, justify="left").pack(anchor="w", padx=18, pady=(0, 13))

        for src in self.app.kb.sources():
            box = tk.Frame(scroll.inner, bg=CARD, highlightbackground=BORDER,
                           highlightthickness=1)
            box.pack(fill="x", pady=7)
            inner = tk.Frame(box, bg=CARD)
            inner.pack(fill="both", padx=20, pady=16)
            tk.Label(inner, text=src["organization"], bg=CARD, fg="#1748c7",
                     font=("Segoe UI", 11, "bold")).pack(anchor="w")
            tk.Label(inner, text=src["title"], bg=CARD, fg="#172033",
                     font=("Segoe UI", 13, "bold"), wraplength=850,
                     justify="left").pack(anchor="w", pady=(4, 5))
            tk.Label(inner, text=src["purpose"], bg=CARD, fg="#42526a",
                     font=("Segoe UI", 10), wraplength=850,
                     justify="left").pack(anchor="w")
            tk.Label(inner, text=src["url"], bg=CARD, fg="#5f6b7a",
                     font=("Segoe UI", 9), wraplength=850,
                     justify="left").pack(anchor="w", pady=(7, 0))

        add_action_bar(self, lambda: self.app.show("home"))
