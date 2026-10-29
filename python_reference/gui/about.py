import tkinter as tk
from tkinter import ttk
from gui.common import add_header, add_action_bar, ScrollableFrame, CARD, BORDER
from config import DISCLAIMER

class AboutPage(ttk.Frame):
    def __init__(self, app):
        super().__init__(app)
        self.app = app

    def refresh(self):
        for w in self.winfo_children():
            w.destroy()
        add_header(self, "About the Project",
                   "First-year software prototype for first-aid awareness and structured emergency guidance.",
                   "ⓘ")
        scroll = ScrollableFrame(self)
        scroll.pack(fill="both", expand=True, padx=28)

        sections = [
            ("Project vision", "LEARN → ASSIST → ESCALATE. The system aims to make reliable first-aid awareness easier to access while clearly directing users toward professional help when appropriate."),
            ("How it works", "The user selects a situation, answers structured questions, and the rule engine selects a predefined awareness pathway. Content is stored locally in JSON so the prototype can work without a database or internet connection."),
            ("Why rule-based logic", "For a first-year project, predefined decision logic is easier to test, explain and validate than attempting to build a medical diagnosis model. The application intentionally avoids claiming to diagnose users."),
            ("Technology", "Python + Tkinter/ttk for the desktop interface, JSON for structured educational content, and small independent engines for rules, search and quizzes. No third-party Python packages are required."),
            ("Project limitations", "The application is not a medical device, diagnostic system, emergency dispatch service, or substitute for trained first aid or professional medical care. Its content is a simplified summary of the cited sources, so always follow current official guidance and trained responders."),
            ("Responsible use", DISCLAIMER),
        ]
        for heading, body in sections:
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

        add_action_bar(self, lambda: self.app.show("home"))
