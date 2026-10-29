import tkinter as tk
from tkinter import ttk
from gui.common import add_header, add_action_bar, ScrollableFrame, CARD, BORDER

class SearchPage(ttk.Frame):
    def __init__(self, app):
        super().__init__(app)
        self.app = app

    def refresh(self):
        for w in self.winfo_children():
            w.destroy()
        add_header(self, "Search",
                   "Search the local educational knowledge base. Results open the relevant module.",
                   "⌕")

        top = ttk.Frame(self)
        top.pack(fill="x", padx=28, pady=10)
        self.entry = ttk.Entry(top)
        self.entry.pack(side="left", fill="x", expand=True)
        self.entry.bind("<Return>", lambda _e: self.run_search())
        ttk.Button(top, text="Search", style="Primary.TButton",
                   command=self.run_search).pack(side="left", padx=(8, 0))

        self.results = ScrollableFrame(self)
        self.results.pack(fill="both", expand=True, padx=28)

        add_action_bar(self, lambda: self.app.show("home"))

    def run_search(self):
        for w in self.results.inner.winfo_children():
            w.destroy()
        query = self.entry.get().strip()
        if not query:
            ttk.Label(self.results.inner, text="Enter a topic such as burns, bleeding, CPR, gloves or safety.",
                      style="Muted.TLabel").pack(anchor="w", pady=12)
            return
        matches = self.app.search_engine.search(query)
        if not matches:
            ttk.Label(self.results.inner, text="No matching topics found.",
                      style="Muted.TLabel").pack(anchor="w", pady=12)
            return
        for result in matches:
            box = tk.Frame(self.results.inner, bg=CARD, highlightbackground=BORDER,
                           highlightthickness=1)
            box.pack(fill="x", pady=6)
            inner = tk.Frame(box, bg=CARD)
            inner.pack(fill="both", padx=18, pady=14)
            tk.Label(inner, text=result["type"], bg=CARD, fg="#1748c7",
                     font=("Segoe UI", 9, "bold")).pack(anchor="w")
            tk.Label(inner, text=result["title"], bg=CARD, fg="#172033",
                     font=("Segoe UI", 12, "bold")).pack(anchor="w", pady=(3, 2))
            ttk.Button(inner, text="Open →",
                       command=lambda r=result: self.app.open_search_result(r)).pack(anchor="w", pady=(8, 0))
