import tkinter as tk
from tkinter import ttk
from gui.common import add_header, add_action_bar, ScrollableFrame, CARD, BORDER

class GuidancePage(ttk.Frame):
    def __init__(self, app):
        super().__init__(app)
        self.app = app
        self.situation = None
        self.pathway = None

    def set_result(self, situation, pathway):
        self.situation = situation
        self.pathway = pathway

    def refresh(self):
        for w in self.winfo_children():
            w.destroy()
        if not self.situation:
            self.app.show("situations")
            return

        add_header(self, "Guidance Result",
                   f"Awareness pathway for {self.situation['title']}.",
                   self.situation.get("icon", "✚"))

        scroll = ScrollableFrame(self)
        scroll.pack(fill="both", expand=True, padx=28)

        p = self.pathway
        self._section(scroll.inner, "Outcome", p["title"], p.get("alert", ""))
        self._section(scroll.inner, "What to do", "", p.get("guidance", []))
        self._section(scroll.inner, "When to seek professional help", "",
                      p.get("professional_help", []))
        self._section(scroll.inner, "Important reminder", "",
                      [self.situation.get("disclaimer", "")])

        wanted = self.situation.get("source_ids", [])
        names = [f"{s['organization']} — {s['title']}"
                 for s in self.app.kb.sources() if s["id"] in wanted]
        if names:
            self._section(scroll.inner, "Based on", "", names)

        add_action_bar(self,
                       lambda: self.app.show("situations"),
                       "Emergency Help", lambda: self.app.show("emergency"))

    def _section(self, parent, heading, title, content):
        box = tk.Frame(parent, bg=CARD, highlightbackground=BORDER,
                       highlightthickness=1)
        box.pack(fill="x", pady=7)
        inner = tk.Frame(box, bg=CARD)
        inner.pack(fill="both", padx=20, pady=16)
        tk.Label(inner, text=heading, bg=CARD, fg="#1748c7",
                 font=("Segoe UI", 11, "bold")).pack(anchor="w")
        if title:
            tk.Label(inner, text=title, bg=CARD, fg="#172033",
                     font=("Segoe UI", 14, "bold"),
                     wraplength=850, justify="left").pack(anchor="w", pady=(5, 6))
        if isinstance(content, str):
            content = [content]
        for item in content:
            tk.Label(inner, text="• " + item, bg=CARD, fg="#42526a",
                     font=("Segoe UI", 10), wraplength=850,
                     justify="left", anchor="w").pack(anchor="w", pady=3)
