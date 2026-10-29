import tkinter as tk
from pathlib import Path
from tkinter import ttk
from gui.common import add_header, add_action_bar, ScrollableFrame, CARD, BORDER

PROJECT_ROOT = Path(__file__).resolve().parent.parent

class KitPage(ttk.Frame):
    def __init__(self, app):
        super().__init__(app)
        self.app = app
        self.item_id = None
        self._image = None   

    def open_item(self, item_id):
        self.item_id = item_id

    def refresh(self):
        for w in self.winfo_children():
            w.destroy()
        if self.item_id:
            self._detail()
        else:
            self._list()

    def _list(self):
        add_header(self, "First-Aid Kit Explorer",
                   "Learn what common kit items are for and why maintaining the kit matters.",
                   "▤")
        scroll = ScrollableFrame(self)
        scroll.pack(fill="both", expand=True, padx=28)

        intro = tk.Frame(scroll.inner, bg="#eaf1ff")
        intro.pack(fill="x", pady=(0, 8))
        tk.Label(intro, text="Kit maintenance matters",
                 bg="#eaf1ff", fg="#1748c7",
                 font=("Segoe UI", 11, "bold")).pack(anchor="w", padx=18, pady=(13, 4))
        tk.Label(intro,
                 text="A kit should be checked regularly. Replace used, damaged or out-of-date contents, "
                      "and keep emergency contact information accessible.",
                 bg="#eaf1ff", fg="#42526a", font=("Segoe UI", 10),
                 wraplength=850, justify="left").pack(anchor="w", padx=18, pady=(0, 13))

        grid = ttk.Frame(scroll.inner)
        grid.pack(fill="x")
        for i, item in enumerate(self.app.kb.kits()):
            r, c = divmod(i, 2)
            box = self._item_card(grid, item)
            box.grid(row=r, column=c, sticky="nsew", padx=6, pady=6)
            grid.grid_columnconfigure(c, weight=1)

        add_action_bar(self, lambda: self.app.show("home"))

    def _item_card(self, parent, item):
        box = tk.Frame(parent, bg=CARD, highlightbackground=BORDER,
                       highlightthickness=1)
        inner = tk.Frame(box, bg=CARD)
        inner.pack(fill="both", padx=18, pady=16)
        tk.Label(inner, text=item.get("icon", "•"), bg=CARD, fg="#1f5eff",
                 font=("Segoe UI Symbol", 19, "bold")).pack(anchor="w")
        tk.Label(inner, text=item["name"], bg=CARD, fg="#172033",
                 font=("Segoe UI", 12, "bold")).pack(anchor="w", pady=(4, 3))
        tk.Label(inner, text=item["purpose"], bg=CARD, fg="#5f6b7a",
                 font=("Segoe UI", 9), wraplength=420,
                 justify="left").pack(anchor="w")
        ttk.Button(inner, text="Explore →",
                   command=lambda iid=item["id"]: self._open(iid)).pack(anchor="w", pady=(10, 0))
        return box

    def _open(self, item_id):
        self.item_id = item_id
        self.refresh()

    def _load_image(self, relative_path):
        """Return a PhotoImage, or None if the file is missing or unreadable."""
        try:
            return tk.PhotoImage(file=str(PROJECT_ROOT / relative_path))
        except tk.TclError:
            return None

    def _detail(self):
        item = next(x for x in self.app.kb.kits() if x["id"] == self.item_id)
        add_header(self, item["name"], item["purpose"], item.get("icon", "▤"))
        scroll = ScrollableFrame(self)
        scroll.pack(fill="both", expand=True, padx=28)

        # picture (the app still works if the image is missing)
        self._image = self._load_image(item["image"]) if item.get("image") else None
        pic = tk.Frame(scroll.inner, bg=CARD, highlightbackground=BORDER,
                       highlightthickness=1)
        pic.pack(fill="x", pady=(0, 10))
        if self._image:
            tk.Label(pic, image=self._image, bg=CARD).pack(pady=16)
        else:
            tk.Label(pic, text="No image available for this item.", bg=CARD,
                     fg="#5f6b7a", font=("Segoe UI", 9)).pack(pady=24)

        for heading, body in [
            ("What it is", item["what"]),
            ("Why it is useful", item["details"]),
            ("General use", item["use"]),
            ("Precautions", item["precautions"]),
        ]:
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

        add_action_bar(self, self._back_to_list)

    def _back_to_list(self):
        self.item_id = None
        self.refresh()
