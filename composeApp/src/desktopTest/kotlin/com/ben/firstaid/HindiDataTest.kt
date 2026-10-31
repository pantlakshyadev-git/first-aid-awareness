package com.ben.firstaid

import java.io.File
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

/** Checks the real Hindi files. Run from the composeApp/ folder (Gradle does this). */
class HindiDataTest {
    private val files = File("src/commonMain/composeResources/files")
    private val sourceDir = File("src/commonMain/kotlin/com/ben/firstaid")

    private val english: FirstAidData by lazy {
        AppJson.decodeFromString(FirstAidData.serializer(), File(files, "first_aid_data.json").readText())
    }
    private val translation: Translation by lazy {
        AppJson.decodeFromString(Translation.serializer(), File(files, "first_aid_data_hi.json").readText())
    }
    private val hindi: FirstAidData by lazy { english.withTranslation(translation) }

    private fun hasDevanagari(text: String) = text.any { it.code in 0x0900..0x097F }

    @Test
    fun translation_matches_the_english_structure() {
        assertEquals(emptyList(), validateTranslation(english, translation))
    }

    @Test
    fun hindi_data_passes_the_same_validation_as_english() {
        assertEquals(emptyList(), validateData(hindi))
    }

    @Test
    fun rules_answer_values_and_sources_are_identical_in_both_languages() {
        for (en in english.situations) {
            val hi = hindi.situation(en.id)!!
            assertEquals(en.rules, hi.rules, en.id)
            assertEquals(en.defaultPathway, hi.defaultPathway, en.id)
            assertEquals(en.sourceIds, hi.sourceIds, en.id)
            assertEquals(en.questions.map { q -> q.id to q.options.map { it.value } }, hi.questions.map { q -> q.id to q.options.map { it.value } }, en.id)
            assertEquals(en.pathways.map { it.id }, hi.pathways.map { it.id }, en.id)
        }
    }

    @Test
    fun every_answer_combination_gives_the_same_pathway_in_both_languages() {
        for (en in english.situations) {
            val hi = hindi.situation(en.id)!!
            var combos: List<Map<String, String>> = listOf(emptyMap())
            for (q in en.questions) combos = combos.flatMap { base -> q.options.map { base + (q.id to it.value) } }
            for (answers in combos) {
                val a = evaluatePathway(en, answers)
                val b = evaluatePathway(hi, answers)
                assertEquals(a.id, b.id, "${en.id} $answers")
                assertEquals(a.guidance.size, b.guidance.size, "${en.id}/${a.id} steps")
                assertEquals(a.professionalHelp.size, b.professionalHelp.size, "${en.id}/${a.id} help")
            }
        }
    }

    @Test
    fun every_situation_is_translated_into_hindi() {
        for (en in english.situations) {
            val hi = hindi.situation(en.id)!!
            assertNotEquals(en.title, hi.title, en.id)
            assertTrue(hasDevanagari(hi.title) && hasDevanagari(hi.summary) && hasDevanagari(hi.assessmentIntro), en.id)
            for (q in hi.questions) {
                assertTrue(hasDevanagari(q.text), "${en.id}/${q.id}")
                for (o in q.options) assertTrue(hasDevanagari(o.label), "${en.id}/${q.id}/${o.value}")
            }
            for (p in hi.pathways) {
                assertTrue(hasDevanagari(p.title) && hasDevanagari(p.alert), "${en.id}/${p.id}")
                for (step in p.guidance + p.professionalHelp) assertTrue(hasDevanagari(step), "${en.id}/${p.id}: $step")
            }
        }
    }

    @Test
    fun emergency_number_is_kept_wherever_the_english_steps_mention_it() {
        for (en in english.situations) {
            val hi = hindi.situation(en.id)!!
            for (p in en.pathways) {
                val hp = hi.pathways.first { it.id == p.id }
                val enSteps = p.guidance + p.professionalHelp
                val hiSteps = hp.guidance + hp.professionalHelp
                enSteps.indices.forEach { i ->
                    if ("112" in enSteps[i]) assertTrue("112" in hiSteps[i], "${en.id}/${p.id} step ${i + 1} lost 112")
                    // numbers such as "5 back blows" or "20 minutes" must not change
                    val enNumbers = Regex("\\d+").findAll(enSteps[i]).map { it.value }.toList()
                    val hiNumbers = Regex("\\d+").findAll(hiSteps[i]).map { it.value }.toList()
                    assertEquals(enNumbers, hiNumbers, "${en.id}/${p.id} step ${i + 1}: numbers differ")
                }
            }
        }
    }

    @Test
    fun hindi_search_finds_situations_and_english_still_works() {
        assertTrue(search(hindi, "सीपीआर").any { it.id == "cardiac_arrest" })
        assertTrue(search(hindi, "जलना").any { it.id == "burns" })
        assertTrue(search(hindi, "हार्ट अटैक").any { it.id == "heart_attack" })
        assertTrue(search(hindi, "burn").any { it.id == "burns" })
    }

    // ----- fixed interface text -----

    @Test
    fun every_tr_text_in_the_source_has_a_hindi_entry() {
        val used = mutableSetOf<String>()
        for (file in sourceDir.listFiles { f -> f.extension == "kt" }!!) {
            if (file.name == "Translation.kt" || file.name == "UiStrings.kt") continue
            val text = file.readText()
            Regex("\\btr\\(\"((?:[^\"\\\\]|\\\\.)*)\"").findAll(text).forEach { used += it.groupValues[1] }
            Regex("HomeEntry\\(Screen\\.\\w+, \"([^\"]*)\", \"([^\"]*)\"").findAll(text).forEach {
                used += it.groupValues[1]
                used += it.groupValues[2]
            }
        }
        assertTrue(used.size > 80, "found only ${used.size} texts, the scan may be broken")
        for (key in used) {
            assertTrue('$' !in key && "\\u" !in key, "keep tr texts free of \$ and \\u escapes: $key")
            assertTrue(key in hindiUi, "no Hindi text for: $key")
        }
    }

    @Test
    fun screen_titles_quiz_categories_and_search_labels_have_hindi_entries() {
        Screen.entries.forEach { assertTrue(it.title in hindiUi, "screen ${it.title}") }
        listOf("Home", "Guide", "Learn", "Quiz", "Search", "All").forEach { assertTrue(it in hindiUi, it) }
        SearchType.entries.forEach { assertTrue(it.label in hindiUi, it.label) }
        english.quizQuestions.map { it.category }.distinct().forEach { assertTrue(it in hindiUi, "category $it") }
        listOf(0, 50, 80).forEach { assertTrue(quizMessage(it, 100) in hindiUi, "quiz message at $it%") }
        assertTrue(AMBULANCE_NOTE in hindiUi)
    }

    @Test
    fun hindi_texts_keep_the_same_placeholders_and_are_not_empty() {
        val placeholder = Regex("\\{\\d+\\}")
        for ((en, hi) in hindiUi) {
            assertTrue(hi.isNotBlank(), en)
            assertEquals(
                placeholder.findAll(en).map { it.value }.toSet(),
                placeholder.findAll(hi).map { it.value }.toSet(),
                "placeholders differ: $en",
            )
        }
    }

    @Test
    fun every_kit_item_is_translated_into_hindi() {
        for (en in english.kitItems) {
            val hi = hindi.kitItem(en.id)!!
            assertNotEquals(en.name, hi.name, en.id)
            assertTrue(hasDevanagari(hi.name) && hasDevanagari(hi.purpose) && hasDevanagari(hi.what) && hasDevanagari(hi.details) && hasDevanagari(hi.use) && hasDevanagari(hi.precautions), en.id)
        }
    }

    @Test
    fun every_quiz_question_is_translated_into_hindi() {
        for ((i, en) in english.quizQuestions.withIndex()) {
            val hi = hindi.quizQuestions[i]
            assertNotEquals(en.question, hi.question, "quiz index $i")
            assertTrue(hasDevanagari(hi.question), "quiz index $i question")
            assertTrue(hasDevanagari(hi.explanation), "quiz index $i explanation")
            assertEquals(en.options.size, hi.options.size, "quiz index $i options count")
            for (opt in hi.options) {
                if (opt.all { it.isDigit() || it.isWhitespace() || it == '(' || it == ')' }) continue
                assertTrue(hasDevanagari(opt), "quiz index $i option $opt")
            }
        }
    }

    @Test
    fun hindi_language_file_loads_even_if_only_part_is_translated() {
        // The app must keep working while lessons, kit items and quiz are still English.
        val partial = english.withTranslation(Translation(situations = translation.situations))
        assertEquals(english.lessons, partial.lessons)
        assertEquals(english.quizQuestions, partial.quizQuestions)
    }
}
