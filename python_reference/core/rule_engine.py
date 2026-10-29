class RuleEngine:
    def evaluate(self, situation, answers):
        for rule in situation.get("rules", []):
            conditions = rule.get("conditions", {})
            if all(answers.get(k) == v for k, v in conditions.items()):
                pathway_id = rule["pathway"]
                return next(
                    (p for p in situation["pathways"] if p["id"] == pathway_id),
                    situation["pathways"][0]
                )
        default_id = situation.get("default_pathway")
        return next(
            (p for p in situation["pathways"] if p["id"] == default_id),
            situation["pathways"][0]
        )
