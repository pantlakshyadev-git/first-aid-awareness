import tkinter as tk
from tkinter import ttk
from gui.common import add_header, card, BG

class HomePage(ttk.Frame):
    def __init__(self, app):
        super().__init__(app)
        self.app = app
        self.build()

    def build(self):
        add_header(
            self,
            "First-Aid Awareness System",
            "Learn essential first-aid concepts, explore a kit, follow guided awareness pathways, "
            "and understand when professional help is needed.",
            "✚"
        )

        banner = tk.Frame(self, bg="#eaf1ff", highlightbackground="#cbd9ff",
                          highlightthickness=1)
        banner.pack(fill="x", padx=28, pady=(5, 18))
        tk.Label(banner, text="LEARN  →  ASSIST  →  ESCALATE",
                 bg="#eaf1ff", fg="#1748c7",
                 font=("Segoe UI", 14, "bold")).pack(anchor="w", padx=18, pady=(14, 3))
        tk.Label(banner,
                 text="Use the system for awareness and structured guidance—not diagnosis or replacement of emergency care.",
                 bg="#eaf1ff", fg="#42526a", font=("Segoe UI", 9),
                 wraplength=900, justify="left").pack(anchor="w", padx=18, pady=(0, 14))

        grid = tk.Frame(self, bg=BG)
        grid.pack(fill="both", expand=True, padx=28)

        cards = [
            ("Guided First Aid", "Situation-based questions followed by rule-based awareness guidance.",
             "✚", lambda: self.app.show("situations")),
            ("Learn", "Build first-aid fundamentals, safety awareness and emergency recognition.",
             "▣", lambda: self.app.show("learn")),
            ("First-Aid Kit", "Explore common kit items, their purpose and important precautions.",
             "▤", lambda: self.app.show("kit")),
            ("Quiz", "Test your knowledge with short randomized questions and explanations.",
             "?", lambda: self.app.start_quiz("All")),
            ("Search", "Find relevant situations, lessons and first-aid kit topics.",
             "⌕", lambda: self.app.show("search")),
            ("Emergency Help", "See India's emergency number and clear escalation guidance.",
             "!", lambda: self.app.show("emergency")),
            ("Sources", "Review the organizations and references used for the educational content.",
             "§", lambda: self.app.show("sources")),
            ("About", "Understand the project's purpose, limits and responsible-use statement.",
             "ⓘ", lambda: self.app.show("about")),
        ]

        for i, item in enumerate(cards):
            r, c = divmod(i, 4)
            widget = card(grid, *item)
            widget.grid(row=r, column=c, sticky="nsew", padx=6, pady=6)
            grid.grid_columnconfigure(c, weight=1)
            grid.grid_rowconfigure(r, weight=1)
