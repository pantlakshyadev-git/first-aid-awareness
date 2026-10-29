from tkinter import ttk
from gui.common import add_header, card, add_action_bar

class SituationsPage(ttk.Frame):
    def __init__(self, app):
        super().__init__(app)
        self.app = app

    def refresh(self):
        for w in self.winfo_children():
            w.destroy()
        add_header(self, "Guided First Aid",
                   "Choose a situation to enter a short, rule-based awareness pathway.",
                   "✚")

        grid = ttk.Frame(self)
        grid.pack(fill="both", expand=True, padx=28)
        situations = self.app.kb.situations()
        for i, s in enumerate(situations):
            r, c = divmod(i, 3)
            widget = card(grid, s["title"], s["summary"], s.get("icon", "✚"),
                          lambda sid=s["id"]: self.app.start_situation(sid))
            widget.grid(row=r, column=c, sticky="nsew", padx=6, pady=6)
            grid.grid_columnconfigure(c, weight=1)
        add_action_bar(self, lambda: self.app.show("home"))
