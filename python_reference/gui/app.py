import tkinter as tk
from tkinter import messagebox
from config import APP_TITLE, WINDOW_SIZE, MIN_WIDTH, MIN_HEIGHT
from core.knowledge_base import KnowledgeBase
from core.rule_engine import RuleEngine
from core.quiz_engine import QuizEngine
from core.search_engine import SearchEngine
from core.validator import DataValidator
from gui.common import setup_style

class FirstAidApp(tk.Tk):
    def __init__(self):
        super().__init__()
        self.title(APP_TITLE)
        self.geometry(WINDOW_SIZE)
        self.minsize(MIN_WIDTH, MIN_HEIGHT)
        self.configure(bg="#f4f7fb")

        setup_style(self)
        self.kb = KnowledgeBase()
        self.rules = RuleEngine()
        self.quiz_engine = QuizEngine(self.kb.quiz_questions())
        self.search_engine = SearchEngine(self.kb)
        DataValidator(self.kb).validate()

        self.current_situation = None
        self.answers = {}
        self.frames = {}
        self._build_frames()
        self.show("home")

    def _build_frames(self):
        from gui.home import HomePage
        from gui.situations import SituationsPage
        from gui.assessment import AssessmentPage
        from gui.guidance import GuidancePage
        from gui.emergency import EmergencyPage
        from gui.sources import SourcesPage
        from gui.learn import LearnPage
        from gui.kit import KitPage
        from gui.quiz import QuizPage
        from gui.quiz_result import QuizResultPage
        from gui.search import SearchPage
        from gui.about import AboutPage

        pages = {
            "home": HomePage,
            "situations": SituationsPage,
            "assessment": AssessmentPage,
            "guidance": GuidancePage,
            "emergency": EmergencyPage,
            "sources": SourcesPage,
            "learn": LearnPage,
            "kit": KitPage,
            "quiz": QuizPage,
            "quiz_result": QuizResultPage,
            "search": SearchPage,
            "about": AboutPage,
        }
        for name, cls in pages.items():
            frame = cls(self)
            self.frames[name] = frame
            frame.grid(row=0, column=0, sticky="nsew")

        self.grid_rowconfigure(0, weight=1)
        self.grid_columnconfigure(0, weight=1)

    def show(self, page_name):
        frame = self.frames[page_name]
        frame.tkraise()
        if hasattr(frame, "refresh"):
            frame.refresh()

    def start_situation(self, situation_id):
        situation = self.kb.get_situation(situation_id)
        if not situation:
            messagebox.showerror("Error", "The selected situation was not found.")
            return
        self.current_situation = situation
        self.answers = {}
        self.show("assessment")

    def submit_assessment(self):
        if not self.current_situation:
            return
        pathway = self.rules.evaluate(self.current_situation, self.answers)
        self.frames["guidance"].set_result(self.current_situation, pathway)
        self.show("guidance")

    def start_quiz(self, category="All"):
        self.frames["quiz"].start_quiz(category)
        self.show("quiz")

    def open_search_result(self, result):
        if result["type"] == "Guided First Aid":
            self.start_situation(result["id"])
        elif result["type"] == "Learn":
            self.frames["learn"].open_lesson(result["id"])
            self.show("learn")
        elif result["type"] == "First-Aid Kit":
            self.frames["kit"].open_item(result["id"])
            self.show("kit")
