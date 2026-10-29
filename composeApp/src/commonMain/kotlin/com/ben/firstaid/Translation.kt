package com.ben.firstaid

import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

// ---------------------------------------------------------------------------
// Languages and the tr(...) helper for fixed interface text
// ---------------------------------------------------------------------------

internal enum class AppLanguage(val code: String, val nativeName: String) {
    English("en", "English"),
    Hindi("hi", "हिन्दी"),
}

internal val LocalLanguage = compositionLocalOf { AppLanguage.English }

/**
 * Returns [text] in the chosen language. English text is the key; if the Hindi list has no entry the
 * English text is shown, so a missing translation can never crash or blank the screen.
 * {0}, {1} ... in the text are replaced by [args].
 */
internal fun translate(language: AppLanguage, text: String, args: List<Any> = emptyList()): String {
    var result = if (language == AppLanguage.Hindi) hindiUi[text] ?: text else text
    args.forEachIndexed { index, arg -> result = result.replace("{$index}", arg.toString()) }
    return result
}

@Composable
internal fun tr(text: String, vararg args: Any): String = translate(LocalLanguage.current, text, args.toList())

// ---------------------------------------------------------------------------
// Translated content (first_aid_data_hi.json). It only holds text, keyed by the ids used in the
// English data, so the rules, answer values and step order can never differ between languages.
// ---------------------------------------------------------------------------

@Serializable
internal data class QuestionTr(val text: String = "", val options: Map<String, String> = emptyMap())

@Serializable
internal data class PathwayTr(
    val title: String = "",
    val alert: String = "",
    val guidance: List<String> = emptyList(),
    @SerialName("professional_help") val professionalHelp: List<String> = emptyList(),
)

@Serializable
internal data class SituationTr(
    val title: String = "",
    val summary: String = "",
    @SerialName("search_terms") val searchTerms: String = "",
    @SerialName("assessment_intro") val assessmentIntro: String = "",
    val disclaimer: String = "",
    val questions: Map<String, QuestionTr> = emptyMap(),
    val pathways: Map<String, PathwayTr> = emptyMap(),
)

@Serializable
internal data class LessonTr(
    val title: String = "",
    val summary: String = "",
    val sections: List<List<String>> = emptyList(),
)

@Serializable
internal data class KitTr(
    val name: String = "",
    val purpose: String = "",
    val what: String = "",
    val details: String = "",
    val use: String = "",
    val precautions: String = "",
)

/** Quiz entries are matched to the English quiz by position, option by option. */
@Serializable
internal data class QuizTr(
    val question: String = "",
    val options: List<String> = emptyList(),
    val answer: String = "",
    val explanation: String = "",
)

@Serializable
internal data class Translation(
    val situations: Map<String, SituationTr> = emptyMap(),
    val lessons: Map<String, LessonTr> = emptyMap(),
    @SerialName("kit_items") val kitItems: Map<String, KitTr> = emptyMap(),
    @SerialName("quiz_questions") val quizQuestions: List<QuizTr> = emptyList(),
)

/** Lays the translated text over the English data. Anything not translated stays English. */
internal fun FirstAidData.withTranslation(t: Translation): FirstAidData = copy(
    situations = situations.map { s ->
        val st = t.situations[s.id] ?: return@map s
        s.copy(
            title = st.title.ifBlank { s.title },
            summary = st.summary.ifBlank { s.summary },
            // Keep the English search words too, so both languages find the same situation.
            searchTerms = (st.searchTerms + " " + s.searchTerms).trim(),
            assessmentIntro = st.assessmentIntro.ifBlank { s.assessmentIntro },
            disclaimer = st.disclaimer.ifBlank { s.disclaimer },
            questions = s.questions.map { q ->
                val qt = t.situations[s.id]?.questions?.get(q.id) ?: return@map q
                q.copy(
                    text = qt.text.ifBlank { q.text },
                    options = q.options.map { o ->
                        val label = qt.options[o.value]
                        if (label.isNullOrBlank()) o else o.copy(label = label)
                    },
                )
            },
            pathways = s.pathways.map { p ->
                val pt = st.pathways[p.id] ?: return@map p
                p.copy(
                    title = pt.title.ifBlank { p.title },
                    alert = pt.alert.ifBlank { p.alert },
                    guidance = pt.guidance.ifEmpty { p.guidance },
                    professionalHelp = pt.professionalHelp.ifEmpty { p.professionalHelp },
                )
            },
        )
    },
    lessons = lessons.map { l ->
        val lt = t.lessons[l.id] ?: return@map l
        l.copy(
            title = lt.title.ifBlank { l.title },
            summary = lt.summary.ifBlank { l.summary },
            sections = lt.sections.ifEmpty { l.sections },
        )
    },
    kitItems = kitItems.map { k ->
        val kt = t.kitItems[k.id] ?: return@map k
        k.copy(
            name = kt.name.ifBlank { k.name },
            purpose = kt.purpose.ifBlank { k.purpose },
            what = kt.what.ifBlank { k.what },
            details = kt.details.ifBlank { k.details },
            use = kt.use.ifBlank { k.use },
            precautions = kt.precautions.ifBlank { k.precautions },
        )
    },
    quizQuestions = quizQuestions.mapIndexed { index, q ->
        val qt = t.quizQuestions.getOrNull(index)
        // Use a translated question only if it is complete and its answer is one of its options.
        if (qt == null || qt.question.isBlank() || qt.options.size != q.options.size || qt.answer !in qt.options) {
            q
        } else {
            q.copy(
                question = qt.question,
                options = qt.options,
                answer = qt.answer,
                explanation = qt.explanation.ifBlank { q.explanation },
            )
        }
    },
)

/** Problems that would make a translation unsafe or inconsistent. Used by the tests. */
internal fun validateTranslation(english: FirstAidData, t: Translation): List<String> {
    val errors = mutableListOf<String>()
    for ((sid, st) in t.situations) {
        val s = english.situation(sid)
        if (s == null) {
            errors += "Unknown situation '$sid'"
            continue
        }
        for ((qid, qt) in st.questions) {
            val q = s.questions.firstOrNull { it.id == qid }
            if (q == null) {
                errors += "$sid: unknown question '$qid'"
                continue
            }
            for (value in qt.options.keys) {
                if (q.options.none { it.value == value }) errors += "$sid/$qid: unknown option '$value'"
            }
        }
        for ((pid, pt) in st.pathways) {
            val p = s.pathways.firstOrNull { it.id == pid }
            if (p == null) {
                errors += "$sid: unknown pathway '$pid'"
                continue
            }
            // Same number of steps, in the same order, so step N means the same thing in both languages.
            if (pt.guidance.isNotEmpty() && pt.guidance.size != p.guidance.size) errors += "$sid/$pid: guidance has ${pt.guidance.size} steps, English has ${p.guidance.size}"
            if (pt.professionalHelp.isNotEmpty() && pt.professionalHelp.size != p.professionalHelp.size) errors += "$sid/$pid: professional_help count differs"
        }
    }
    for ((lid, lt) in t.lessons) {
        val l = english.lesson(lid)
        if (l == null) {
            errors += "Unknown lesson '$lid'"
            continue
        }
        if (lt.sections.isNotEmpty() && lt.sections.size != l.sections.size) errors += "$lid: section count differs"
        if (lt.sections.any { it.size != 2 }) errors += "$lid: every section must be [heading, text]"
    }
    for (kid in t.kitItems.keys) if (english.kitItem(kid) == null) errors += "Unknown kit item '$kid'"
    if (t.quizQuestions.isNotEmpty()) {
        if (t.quizQuestions.size != english.quizQuestions.size) errors += "Quiz has ${t.quizQuestions.size} translated questions, English has ${english.quizQuestions.size}"
        t.quizQuestions.forEachIndexed { i, qt ->
            val q = english.quizQuestions.getOrNull(i) ?: return@forEachIndexed
            if (qt.options.size != q.options.size) errors += "Quiz ${i + 1}: option count differs"
            if (qt.answer !in qt.options) errors += "Quiz ${i + 1}: answer is not one of the options"
            // The correct answer must sit at the same position as in English.
            if (qt.options.indexOf(qt.answer) != q.options.indexOf(q.answer)) errors += "Quiz ${i + 1}: correct answer is at a different position"
        }
    }
    return errors
}
