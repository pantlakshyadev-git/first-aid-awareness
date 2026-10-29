import json
from pathlib import Path

class KnowledgeBase:
    def __init__(self, path=None):
        if path is None:
            path = Path(__file__).resolve().parent.parent / "data" / "first_aid_data.json"
        self.path = Path(path)
        with self.path.open("r", encoding="utf-8") as f:
            self.data = json.load(f)

    def situations(self):
        return self.data.get("situations", [])

    def get_situation(self, situation_id):
        return next((s for s in self.situations()
                     if s["id"] == situation_id), None)

    def sources(self):
        return self.data.get("sources", [])

    def kits(self):
        return self.data.get("kit_items", [])

    def lessons(self):
        return self.data.get("lessons", [])

    def quiz_questions(self):
        return self.data.get("quiz_questions", [])

    def search_documents(self):
        docs = []
        for s in self.situations():
            guidance = " ".join(g for p in s.get("pathways", [])
                                for g in p.get("guidance", []))
            docs.append({
                "title": s["title"],
                "type": "Guided First Aid",
                "keywords": " ".join([s["title"], s.get("summary", ""),
                                      s.get("search_terms", ""), guidance]),
                "id": s["id"]
            })
        for lesson in self.lessons():
            body = " ".join(h + " " + t for h, t in lesson.get("sections", []))
            docs.append({
                "title": lesson["title"],
                "type": "Learn",
                "keywords": " ".join([lesson["title"], lesson.get("summary", ""), body]),
                "id": lesson["id"]
            })
        for item in self.kits():
            docs.append({
                "title": item["name"],
                "type": "First-Aid Kit",
                "keywords": " ".join([item["name"], item.get("purpose", ""),
                                      item.get("details", "")]),
                "id": item["id"]
            })
        return docs
