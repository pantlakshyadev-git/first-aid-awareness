import unittest
from pathlib import Path
from core.knowledge_base import KnowledgeBase
from core.validator import DataValidator

class FakeKB:
    """Tiny stand-in so we can check that the validator rejects bad data."""
    def __init__(self, situations=None, quiz=None):
        self._s, self._q = situations or [], quiz or []
    def situations(self): return self._s
    def sources(self): return []
    def quiz_questions(self): return self._q

def good_situation(**changes):
    s = {"id": "x", "questions": [{"id": "q1", "options": [{"value": "a"}, {"value": "b"}]}],
         "pathways": [{"id": "p1"}], "default_pathway": "p1",
         "rules": [{"conditions": {"q1": "a"}, "pathway": "p1"}]}
    s.update(changes)
    return s

class DataTests(unittest.TestCase):
    def setUp(self):
        self.kb = KnowledgeBase()

    def test_data_validates(self):
        self.assertTrue(DataValidator(self.kb).validate())

    def test_six_guided_situations(self):
        self.assertGreaterEqual(len(self.kb.situations()), 6)

    def test_kit_content(self):
        self.assertGreaterEqual(len(self.kb.kits()), 10)

    def test_sources_content(self):
        self.assertGreaterEqual(len(self.kb.sources()), 6)

    def test_every_kit_item_has_an_image_file(self):
        root = Path(__file__).resolve().parent.parent
        for item in self.kb.kits():
            self.assertTrue(item.get("image"), f"{item['id']} has no image path")
            self.assertTrue((root / item["image"]).is_file(), item["image"])

    def test_quiz_size(self):
        self.assertGreaterEqual(len(self.kb.quiz_questions()), 20)

    def test_validator_accepts_good_data(self):
        self.assertTrue(DataValidator(FakeKB([good_situation()])).validate())

    def test_validator_rejects_bad_default_pathway(self):
        with self.assertRaises(ValueError):
            DataValidator(FakeKB([good_situation(default_pathway="nope")])).validate()

    def test_validator_rejects_unknown_answer_value(self):
        bad = good_situation(rules=[{"conditions": {"q1": "zzz"}, "pathway": "p1"}])
        with self.assertRaises(ValueError):
            DataValidator(FakeKB([bad])).validate()

    def test_validator_rejects_quiz_answer_not_in_options(self):
        bad_quiz = [{"question": "?", "options": ["a", "b"], "answer": "c"}]
        with self.assertRaises(ValueError):
            DataValidator(FakeKB([good_situation()], bad_quiz)).validate()

if __name__ == "__main__":
    unittest.main()
