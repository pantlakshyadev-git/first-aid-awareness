package com.ben.firstaid

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlin.math.roundToInt
import kotlin.random.Random

// ---------------------------------------------------------------------------
// Data lookups
// ---------------------------------------------------------------------------

internal fun FirstAidData.situation(id: String?): Situation? = situations.firstOrNull { it.id == id }
internal fun FirstAidData.lesson(id: String?): Lesson? = lessons.firstOrNull { it.id == id }
internal fun FirstAidData.kitItem(id: String?): KitItem? = kitItems.firstOrNull { it.id == id }
internal fun FirstAidData.source(id: String): Source? = sources.firstOrNull { it.id == id }

// ---------------------------------------------------------------------------
// Rule engine (port of python_reference/core/rule_engine.py)
// ---------------------------------------------------------------------------

/**
 * The first rule whose conditions all match wins. If none match, the situation's default
 * pathway is used. In every bundled situation the default is the escalating pathway.
 */
internal fun evaluatePathway(situation: Situation, answers: Map<String, String>): Pathway {
    for (rule in situation.rules) {
        if (rule.conditions.all { (questionId, value) -> answers[questionId] == value }) {
            return situation.pathways.firstOrNull { it.id == rule.pathway } ?: situation.pathways.first()
        }
    }
    return situation.pathways.firstOrNull { it.id == situation.defaultPathway } ?: situation.pathways.first()
}

/** True only when every question has an answer that is one of its real options. */
internal fun allAnswered(situation: Situation, answers: Map<String, String>): Boolean =
    situation.questions.all { question ->
        val given = answers[question.id]
        given != null && question.options.any { it.value == given }
    }

// ---------------------------------------------------------------------------
// Data validator (port of python_reference/core/validator.py)
// ---------------------------------------------------------------------------

/** Returns a list of problems. An empty list means the data is valid. */
internal fun validateData(data: FirstAidData): List<String> {
    val errors = mutableListOf<String>()
    if (data.situations.map { it.id }.toSet().size != data.situations.size) errors += "Duplicate situation IDs"
    val sourceIds = data.sources.map { it.id }.toSet()
    for (s in data.situations) {
        if (s.questions.isEmpty() || s.pathways.isEmpty()) {
            errors += "Incomplete situation: ${s.id}"
            continue
        }
        val optionValues = s.questions.associate { q -> q.id to q.options.map { it.value }.toSet() }
        val pathwayIds = s.pathways.map { it.id }.toSet()
        if (s.defaultPathway !in pathwayIds) errors += "Bad default pathway in ${s.id}"
        for (rule in s.rules) {
            if (rule.pathway !in pathwayIds) errors += "Unknown pathway '${rule.pathway}' in ${s.id}"
            for ((questionId, value) in rule.conditions) {
                val allowed = optionValues[questionId]
                if (allowed == null) {
                    errors += "Unknown question '$questionId' in rule for ${s.id}"
                } else if (value !in allowed) {
                    errors += "Unknown answer '$value' in rule for ${s.id}"
                }
            }
        }
        for (sourceId in s.sourceIds) {
            if (sourceId !in sourceIds) errors += "Unknown source '$sourceId' in ${s.id}"
        }
    }
    for (q in data.quizQuestions) {
        if (q.answer !in q.options) errors += "Quiz answer not in options: ${q.question}"
    }
    return errors
}

// ---------------------------------------------------------------------------
// Search (port of python_reference/core/search_engine.py)
// ---------------------------------------------------------------------------

internal enum class SearchType(val label: String) {
    Situation("Guided First Aid"),
    Lesson("Learn"),
    Kit("First-Aid Kit"),
}

internal data class SearchHit(val type: SearchType, val id: String, val title: String, val summary: String)

private data class SearchDoc(val hit: SearchHit, val keywords: String)

/**
 * Letters and digits, plus the Devanagari block (Hindi). Hindi vowel signs are not "letters" for
 * Kotlin, so without this a Hindi word would be cut into pieces. The danda marks (। ॥) end a word.
 */
private fun isWordChar(ch: Char): Boolean =
    ch.isLetterOrDigit() || (ch.code in 0x0900..0x097F && ch != '\u0964' && ch != '\u0965')

/** Splits text into lowercase words made of letters and digits. */
internal fun tokenize(text: String): List<String> {
    val words = mutableListOf<String>()
    var current = ""
    for (ch in text.lowercase()) {
        if (isWordChar(ch)) {
            current += ch
        } else if (current.isNotEmpty()) {
            words += current
            current = ""
        }
    }
    if (current.isNotEmpty()) words += current
    return words
}

private fun buildSearchDocs(data: FirstAidData): List<SearchDoc> {
    val docs = mutableListOf<SearchDoc>()
    for (s in data.situations) {
        val guidance = s.pathways.flatMap { it.guidance }.joinToString(" ")
        docs += SearchDoc(
            SearchHit(SearchType.Situation, s.id, s.title, s.summary),
            "${s.title} ${s.summary} ${s.searchTerms} $guidance",
        )
    }
    for (l in data.lessons) {
        val body = l.sections.joinToString(" ") { it.joinToString(" ") }
        docs += SearchDoc(SearchHit(SearchType.Lesson, l.id, l.title, l.summary), "${l.title} ${l.summary} $body")
    }
    for (k in data.kitItems) {
        docs += SearchDoc(
            SearchHit(SearchType.Kit, k.id, k.name, k.purpose),
            "${k.name} ${k.purpose} ${k.details}",
        )
    }
    return docs
}

/** A query word matches a document word that starts with it ("bleed" finds "bleeding"). */
internal fun search(data: FirstAidData, query: String, limit: Int = 12): List<SearchHit> {
    val terms = tokenize(query)
    if (terms.isEmpty()) return emptyList()
    return buildSearchDocs(data)
        .mapNotNull { doc ->
            val words = tokenize(doc.keywords)
            val score = terms.count { term -> words.any { it.startsWith(term) } }
            if (score > 0) score to doc.hit else null
        }
        .sortedWith(compareBy<Pair<Int, SearchHit>>({ -it.first }, { it.second.title }))
        .take(limit)
        .map { it.second }
}

// ---------------------------------------------------------------------------
// Quiz (port of python_reference/core/quiz_engine.py)
// ---------------------------------------------------------------------------

internal fun makeQuiz(
    questions: List<QuizQuestion>,
    category: String,
    count: Int,
    random: Random = Random.Default,
): List<QuizQuestion> {
    val pool = questions.filter { category == "All" || it.category == category }.shuffled(random)
    return pool.take(count.coerceAtMost(pool.size)).map { it.copy(options = it.options.shuffled(random)) }
}

internal fun quizMessage(score: Int, total: Int): String {
    val pct = percent(score, total)
    return when {
        pct >= 80 -> "Strong awareness foundation. Keep reviewing and practise through formal training."
        pct >= 50 -> "Good start. Revisit the Learn section and try again."
        else -> "Use this as a learning checkpoint. Review the topics and try the quiz again."
    }
}

internal fun percent(score: Int, total: Int): Int = if (total == 0) 0 else (score * 100.0 / total).roundToInt()

/** One run through a quiz. An answer is locked as soon as it is chosen. */
internal class QuizSession(val questions: List<QuizQuestion>) {
    var index by mutableStateOf(0)
        private set
    var selected by mutableStateOf<String?>(null)
        private set
    var score by mutableStateOf(0)
        private set
    var finished by mutableStateOf(false)
        private set

    val current: QuizQuestion get() = questions[index]
    val answered: Boolean get() = selected != null
    val isLast: Boolean get() = index >= questions.lastIndex

    fun choose(option: String) {
        if (finished || selected != null) return
        selected = option
        if (option == current.answer) score += 1
    }

    fun next() {
        if (selected == null) return
        if (isLast) {
            finished = true
        } else {
            index += 1
            selected = null
        }
    }
}
