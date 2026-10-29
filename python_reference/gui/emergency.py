import tkinter as tk
from tkinter import ttk
from gui.common import add_header, add_action_bar, ScrollableFrame, CARD, BORDER
from config import EMERGENCY_NUMBER, AMBULANCE_NOTE

class EmergencyPage(ttk.Frame):
    def __init__(self, app):
        super().__init__(app)
        self.app = app

    def refresh(self):
        for w in self.winfo_children():
            w.destroy()

        add_header(self, "Emergency Help",
                   "Use emergency services immediately when a situation may be life-threatening.",
                   "!")

        scroll = ScrollableFrame(self)
        scroll.pack(fill="both", expand=True, padx=28)

        self._box(scroll.inner, "India emergency number",
                  f"☎  {EMERGENCY_NUMBER}",
                  "Primary national emergency number for urgent ambulance, police and fire assistance.",
                  danger=True)

        self._box(scroll.inner, "Ambulance information",
                  "112 — Ambulance / Emergency Assistance",
                  AMBULANCE_NOTE)

        self._box(scroll.inner, "When to escalate",
                  "Seek emergency help immediately when:",
                  "• someone is unconscious or not responding normally\n"
                  "• someone is not breathing normally or may be in cardiac arrest\n"
                  "• there is life-threatening bleeding\n"
                  "• there are signs of a serious breathing emergency\n"
                  "• a serious accident or major injury has occurred\n"
                  "• symptoms suggest a potentially life-threatening medical emergency")

        self._box(scroll.inner, "When calling",
                  "Give clear, useful information",
                  "Tell the operator your location, what happened, the person's condition, "
                  "and any immediate danger. Follow the emergency operator's instructions.")

        self._box(scroll.inner, "Do not delay for the app",
                  "Emergency services come first",
                  "This application is an educational tool. Do not spend time navigating the "
                  "application when professional emergency assistance is needed.")

        add_action_bar(self, lambda: self.app.show("home"),
                       "Guided First Aid", lambda: self.app.show("situations"))

    def _box(self, parent, heading, title, text, danger=False):
        box = tk.Frame(parent, bg=CARD, highlightbackground="#efb4b4" if danger else BORDER,
                       highlightthickness=1)
        box.pack(fill="x", pady=7)
        inner = tk.Frame(box, bg=CARD)
        inner.pack(fill="both", padx=20, pady=18)
        tk.Label(inner, text=heading, bg=CARD,
                 fg="#c62828" if danger else "#1748c7",
                 font=("Segoe UI", 11, "bold")).pack(anchor="w")
        tk.Label(inner, text=title, bg=CARD, fg="#172033",
                 font=("Segoe UI", 15, "bold"),
                 wraplength=850, justify="left").pack(anchor="w", pady=(4, 7))
        tk.Label(inner, text=text, bg=CARD, fg="#42526a",
                 font=("Segoe UI", 10), wraplength=850,
                 justify="left").pack(anchor="w")
