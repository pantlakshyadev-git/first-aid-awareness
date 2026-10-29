import re

class SearchEngine:
    def __init__(self, knowledge_base):
        self.kb = knowledge_base

    def search(self, query, limit=12):
        terms = re.findall(r"\w+", query.lower())
        if not terms:
            return []

        results = []
        for doc in self.kb.search_documents():
            words = re.findall(r"\w+", doc["keywords"].lower())
            # a term matches a word that starts with it: "bleed" -> "bleeding"
            score = sum(1 for t in terms if any(w.startswith(t) for w in words))
            if score:
                results.append((score, doc))
        results.sort(key=lambda x: (-x[0], x[1]["title"]))
        return [doc for _, doc in results[:limit]]
