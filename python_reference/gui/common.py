import tkinter as tk
from tkinter import ttk
from config import APP_TITLE, APP_VERSION, EMERGENCY_NUMBER

BG = "#f4f7fb"
CARD = "#ffffff"
TEXT = "#172033"
MUTED = "#5f6b7a"
PRIMARY = "#1f5eff"
PRIMARY_DARK = "#1748c7"
BORDER = "#dbe2ea"
SUCCESS = "#198754"
WARNING = "#9a6700"
DANGER = "#c62828"

def setup_style(root):
    style = ttk.Style(root)
    try:
        style.theme_use("clam")
    except tk.TclError:
        pass

    style.configure("TFrame", background=BG)
    style.configure("Card.TFrame", background=CARD)
    style.configure("TLabel", background=BG, foreground=TEXT, font=("Segoe UI", 10))
    style.configure("Title.TLabel", background=BG, foreground=TEXT,
                    font=("Segoe UI", 22, "bold"))
    style.configure("Subtitle.TLabel", background=BG, foreground=MUTED,
                    font=("Segoe UI", 11))
    style.configure("Section.TLabel", background=BG, foreground=TEXT,
                    font=("Segoe UI", 15, "bold"))
    style.configure("CardTitle.TLabel", background=CARD, foreground=TEXT,
                    font=("Segoe UI", 12, "bold"))
    style.configure("CardText.TLabel", background=CARD, foreground=MUTED,
                    font=("Segoe UI", 9))
    style.configure("Body.TLabel", background=BG, foreground=TEXT,
                    font=("Segoe UI", 10))
    style.configure("Muted.TLabel", background=BG, foreground=MUTED,
                    font=("Segoe UI", 9))
    style.configure("TButton", font=("Segoe UI", 10, "bold"), padding=(12, 8))
    style.configure("Primary.TButton", background=PRIMARY, foreground="white",
                    padding=(14, 9))
    style.map("Primary.TButton",
              background=[("active", PRIMARY_DARK)],
              foreground=[("active", "white")])
    style.configure("Danger.TButton", background=DANGER, foreground="white",
                    padding=(14, 9))
    style.map("Danger.TButton",
              background=[("active", "#9f1f1f")],
              foreground=[("active", "white")])
    style.configure("TEntry", padding=8)
    style.configure("TRadiobutton", background=BG, foreground=TEXT,
                    font=("Segoe UI", 10))
    style.configure("TCheckbutton", background=BG, foreground=TEXT)

def add_header(parent, title, subtitle=None, icon="✚"):
    frame = ttk.Frame(parent)
    frame.pack(fill="x", padx=28, pady=(22, 8))
    top = ttk.Frame(frame)
    top.pack(fill="x")
    ttk.Label(top, text=f"{icon}  {title}", style="Title.TLabel").pack(side="left")
    ttk.Label(top, text=f"v{APP_VERSION}", style="Muted.TLabel").pack(side="right")
    if subtitle:
        ttk.Label(frame, text=subtitle, style="Subtitle.TLabel",
                  wraplength=900, justify="left").pack(fill="x", pady=(5, 0))
    return frame

def add_action_bar(parent, back_command=None, primary_text=None, primary_command=None):
    bar = ttk.Frame(parent)
    bar.pack(fill="x", padx=28, pady=(10, 20))
    if back_command:
        ttk.Button(bar, text="← Back", command=back_command).pack(side="left")
    if primary_command and primary_text:
        ttk.Button(bar, text=primary_text, command=primary_command,
                   style="Primary.TButton").pack(side="right")
    return bar

def card(parent, title, description, icon="•", command=None):
    outer = tk.Frame(parent, bg=CARD, highlightbackground=BORDER,
                     highlightthickness=1, bd=0)
    inner = tk.Frame(outer, bg=CARD)
    inner.pack(fill="both", expand=True, padx=16, pady=15)

    tk.Label(inner, text=icon, bg=CARD, fg=PRIMARY,
             font=("Segoe UI Symbol", 22, "bold")).pack(anchor="w")
    tk.Label(inner, text=title, bg=CARD, fg=TEXT,
             font=("Segoe UI", 12, "bold")).pack(anchor="w", pady=(5, 3))
    tk.Label(inner, text=description, bg=CARD, fg=MUTED,
             font=("Segoe UI", 9), wraplength=250, justify="left").pack(anchor="w")

    if command:
        btn = ttk.Button(inner, text="Open →", command=command)
        btn.pack(anchor="w", pady=(12, 0))
    return outer

class ScrollableFrame(ttk.Frame):
    """Reusable vertical scrolling container for long pages."""
    def __init__(self, parent, *args, **kwargs):
        super().__init__(parent, *args, **kwargs)
        self.canvas = tk.Canvas(self, background=BG, highlightthickness=0, bd=0)
        self.scrollbar = ttk.Scrollbar(self, orient="vertical",
                                       command=self.canvas.yview)
        self.inner = ttk.Frame(self.canvas)
        self.window_id = self.canvas.create_window(
            (0, 0), window=self.inner, anchor="nw"
        )
        self.canvas.configure(yscrollcommand=self.scrollbar.set)

        self.canvas.pack(side="left", fill="both", expand=True)
        self.scrollbar.pack(side="right", fill="y")

        self.inner.bind("<Configure>", self._on_inner_configure)
        self.canvas.bind("<Configure>", self._on_canvas_configure)
        self.canvas.bind("<Enter>", self._bind_mousewheel)
        self.canvas.bind("<Leave>", self._unbind_mousewheel)
        self.inner.bind("<Enter>", self._bind_mousewheel)
        self.inner.bind("<Leave>", self._unbind_mousewheel)

    def _on_inner_configure(self, _event=None):
        self.canvas.configure(scrollregion=self.canvas.bbox("all"))

    def _on_canvas_configure(self, event):
        self.canvas.itemconfigure(self.window_id, width=event.width)

    def _bind_mousewheel(self, _event=None):
        self.canvas.bind_all("<MouseWheel>", self._on_mousewheel)
        self.canvas.bind_all("<Button-4>", self._on_mousewheel)
        self.canvas.bind_all("<Button-5>", self._on_mousewheel)

    def _unbind_mousewheel(self, _event=None):
        self.canvas.unbind_all("<MouseWheel>")
        self.canvas.unbind_all("<Button-4>")
        self.canvas.unbind_all("<Button-5>")

    def _on_mousewheel(self, event):
        if getattr(event, "num", None) == 4:
            self.canvas.yview_scroll(-1, "units")
        elif getattr(event, "num", None) == 5:
            self.canvas.yview_scroll(1, "units")
        else:
            self.canvas.yview_scroll(int(-1 * (event.delta / 120)), "units")

    def scroll_to_top(self):
        self.canvas.yview_moveto(0)
