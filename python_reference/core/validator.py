class DataValidator:
    def __init__(self, kb):
        self.kb = kb

    def validate(self):
        situations = self.kb.situations()
        ids = {s["id"] for s in situations}
        if len(ids) != len(situations):
            raise ValueError("Duplicate situation IDs")

        source_ids = {s["id"] for s in self.kb.sources()}
        for s in situations:
            if not s.get("questions") or not s.get("pathways"):
                raise ValueError(f"Incomplete situation: {s['id']}")
            options = {q["id"]: {o["value"] for o in q["options"]} for q in s["questions"]}
            pathway_ids = {p["id"] for p in s["pathways"]}
            if s.get("default_pathway") not in pathway_ids:
                raise ValueError(f"Bad default pathway in {s['id']}")
            for rule in s.get("rules", []):
                if rule["pathway"] not in pathway_ids:
                    raise ValueError(f"Unknown pathway in {s['id']}")
                for qid, value in rule.get("conditions", {}).items():
                    if qid not in options:
                        raise ValueError(f"Unknown question in rule for {s['id']}")
                    if value not in options[qid]:
                        raise ValueError(f"Unknown answer '{value}' in rule for {s['id']}")
            for sid in s.get("source_ids", []):
                if sid not in source_ids:
                    raise ValueError(f"Unknown source '{sid}' in {s['id']}")

        for q in self.kb.quiz_questions():
            if q["answer"] not in q["options"]:
                raise ValueError(f"Quiz answer not in options: {q['question']}")
        return True
