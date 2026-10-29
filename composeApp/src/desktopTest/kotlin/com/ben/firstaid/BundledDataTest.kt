package com.ben.firstaid

import java.io.File
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Checks the real bundled JSON. Gradle runs tests with the module folder (composeApp/) as the
 * working directory, so the resource paths below are relative to it.
 */
class BundledDataTest {
    private val resources = File("src/commonMain/composeResources")
    private val data: FirstAidData by lazy {
        AppJson.decodeFromString(FirstAidData.serializer(), File(resources, "files/first_aid_data.json").readText())
    }

    @Test
    fun data_loads_and_validates() {
        assertEquals(emptyList(), validateData(data))
    }

    @Test
    fun expected_content_is_present() {
        assertTrue(data.situations.size >= 6)
        assertTrue(data.lessons.size >= 5)
        assertTrue(data.kitItems.size >= 10)
        assertTrue(data.quizQuestions.size >= 20)
        assertTrue(data.sources.size >= 6)
    }

    @Test
    fun every_answer_combination_gives_a_pathway_with_guidance() {
        for (situation in data.situations) {
            var combos: List<Map<String, String>> = listOf(emptyMap())
            for (q in situation.questions) {
                combos = combos.flatMap { base -> q.options.map { base + (q.id to it.value) } }
            }
            for (answers in combos) {
                val pathway = evaluatePathway(situation, answers)
                assertTrue(pathway.guidance.isNotEmpty(), "${situation.id} $answers")
            }
        }
    }

    @Test
    fun missing_answers_always_escalate_never_reassure() {
        // allAnswered() stops the UI from showing guidance, but the fallback must still be safe.
        for (situation in data.situations) {
            val pathway = evaluatePathway(situation, emptyMap())
            assertEquals(situation.defaultPathway, pathway.id, situation.id)
        }
    }

    @Test
    fun responsive_person_with_abnormal_breathing_is_an_emergency() {
        val situation = data.situation("cardiac_arrest")!!
        val pathway = evaluatePathway(situation, mapOf("responsive" to "responsive", "breathing" to "abnormal"))
        assertTrue(pathway.id != "not_arrest")
        assertTrue((pathway.guidance + pathway.professionalHelp).joinToString(" ").contains(EMERGENCY_NUMBER))
    }

    @Test
    fun every_kit_item_has_an_image_file() {
        for (item in data.kitItems) {
            assertTrue(File(resources, "drawable/kit/${item.id}.png").isFile, item.id)
        }
    }

    @Test
    fun every_quiz_question_has_an_explanation() {
        for (q in data.quizQuestions) assertTrue(q.explanation.isNotBlank(), q.question)
    }

    @Test
    fun correct_quiz_answer_is_not_always_the_longest_option() {
        val longest = data.quizQuestions.count { q -> q.options.maxByOrNull { it.length } == q.answer }
        assertTrue(longest * 100 / data.quizQuestions.size <= 60, "answer is the longest option in $longest questions")
    }

    @Test
    fun search_finds_cardiac_arrest() {
        assertTrue(search(data, "cardiac arrest").any { it.id == "cardiac_arrest" })
    }

    @Test
    fun every_situation_and_lesson_has_its_own_icon() {
        for (s in data.situations) assertTrue(s.id in situationIcons, "no icon for situation ${s.id}")
        for (l in data.lessons) assertTrue(l.id in lessonIcons, "no icon for lesson ${l.id}")
    }
}
