import random

class QuizEngine:
    def __init__(self, questions):
        self.questions = questions

    def make_quiz(self, category=None, count=5):
        pool = [q for q in self.questions
                if not category or category == "All" or q["category"] == category]
        random.shuffle(pool)
        quiz = []
        for q in pool[:min(count, len(pool))]:
            q = dict(q)                      # copy so the stored data keeps its order
            q["options"] = random.sample(q["options"], len(q["options"]))
            quiz.append(q)
        return quiz

    @staticmethod
    def check(question, answer):
        return answer == question["answer"]
