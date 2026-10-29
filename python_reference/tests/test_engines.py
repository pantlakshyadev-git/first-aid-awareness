import unittest
from core.knowledge_base import KnowledgeBase
from core.quiz_engine import QuizEngine
from core.search_engine import SearchEngine

class EngineTests(unittest.TestCase):
    def setUp(self):
        self.kb = KnowledgeBase()
        self.search = SearchEngine(self.kb)

    def test_quiz_count(self):
        quiz = QuizEngine(self.kb.quiz_questions()).make_quiz("All", 5)
        self.assertEqual(len(quiz), 5)

    def test_quiz_options_shuffled_but_answer_kept(self):
        for q in QuizEngine(self.kb.quiz_questions()).make_quiz("All", 20):
            self.assertIn(q["answer"], q["options"])
            self.assertTrue(QuizEngine.check(q, q["answer"]))

    def test_quiz_questions_have_explanations(self):
        for q in self.kb.quiz_questions():
            self.assertTrue(q.get("explanation"), q["question"])

    def test_search(self):
        results = self.search.search("cardiac arrest")
        self.assertTrue(results)
        self.assertTrue(any("Cardiac" in r["title"] for r in results))

    def test_search_finds_lesson_text(self):
        # "electricity" only appears inside the Scene Safety lesson body
        titles = [r["title"] for r in self.search.search("electricity")]
        self.assertIn("Scene Safety & Response", titles)

    def test_search_matches_word_starts_only(self):
        titles = [r["title"] for r in self.search.search("art")]
        self.assertNotIn("Cardiac Arrest", titles)   # "art" is inside "heart", not a word start

    def test_search_empty_query(self):
        self.assertEqual(self.search.search("   "), [])

if __name__ == "__main__":
    unittest.main()
