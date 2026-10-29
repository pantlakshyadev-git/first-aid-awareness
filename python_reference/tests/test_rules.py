import itertools
import unittest
from core.knowledge_base import KnowledgeBase
from core.rule_engine import RuleEngine

class RuleEngineTests(unittest.TestCase):
    def setUp(self):
        self.kb = KnowledgeBase()
        self.engine = RuleEngine()

    def test_serious_bleeding_is_urgent(self):
        s = self.kb.get_situation("bleeding")
        result = self.engine.evaluate(s, {"severe": "severe", "responsive": "responsive"})
        self.assertEqual(result["id"], "urgent")

    def test_cardiac_arrest_path(self):
        s = self.kb.get_situation("cardiac_arrest")
        result = self.engine.evaluate(s, {"responsive": "unresponsive", "breathing": "abnormal"})
        self.assertEqual(result["id"], "arrest")

    def test_heart_attack_path(self):
        s = self.kb.get_situation("heart_attack")
        result = self.engine.evaluate(s, {"symptoms": "concerning", "responsive": "stable"})
        self.assertEqual(result["id"], "urgent")

    def test_default_path(self):
        s = self.kb.get_situation("burns")
        result = self.engine.evaluate(s, {"severity": "unexpected", "location": "unexpected"})
        self.assertEqual(result["id"], "urgent")

    def test_every_answer_combination_gives_a_pathway(self):
        for s in self.kb.situations():
            values = [[o["value"] for o in q["options"]] for q in s["questions"]]
            for combo in itertools.product(*values):
                answers = {q["id"]: v for q, v in zip(s["questions"], combo)}
                result = self.engine.evaluate(s, answers)
                self.assertTrue(result["guidance"], f"{s['id']} {answers}")

    def test_responsive_but_abnormal_breathing_escalates(self):
        s = self.kb.get_situation("cardiac_arrest")
        result = self.engine.evaluate(s, {"responsive": "responsive", "breathing": "abnormal"})
        self.assertNotEqual(result["id"], "not_arrest")
        self.assertIn("112", " ".join(result["guidance"] + result["professional_help"]))

    def test_urgent_pathways_contain_the_key_action(self):
        key_words = {"bleeding": "pressure", "burns": "cool", "choking": "back blows"}
        for sid, word in key_words.items():
            urgent = next(p for p in self.kb.get_situation(sid)["pathways"] if p["id"] == "urgent")
            self.assertIn(word, " ".join(urgent["guidance"]).lower(), sid)

if __name__ == "__main__":
    unittest.main()
