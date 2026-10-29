package com.ben.firstaid

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class TranslateTextTest {
    @Test
    fun english_text_is_returned_unchanged_and_placeholders_are_filled() {
        assertEquals("Call 112", translate(AppLanguage.English, "Call {0}", listOf("112")))
        assertEquals("Question 2 of 5", translate(AppLanguage.English, "Question {0} of {1}", listOf(2, 5)))
    }

    @Test
    fun hindi_text_is_used_when_the_list_has_it() {
        assertEquals("वापस", translate(AppLanguage.Hindi, "Back"))
        assertEquals("112 पर कॉल करें", translate(AppLanguage.Hindi, "Call {0}", listOf("112")))
    }

    @Test
    fun missing_hindi_entry_falls_back_to_english() {
        assertEquals("No such text", translate(AppLanguage.Hindi, "No such text"))
    }
}

class WithTranslationTest {
    private val english = fixture()

    @Test
    fun translated_text_replaces_english_but_rules_and_values_stay() {
        val t = Translation(
            situations = mapOf(
                "burns" to SituationTr(
                    title = "जलना",
                    questions = mapOf("severity" to QuestionTr(text = "गंभीर?", options = mapOf("serious" to "हाँ"))),
                    pathways = mapOf("urgent" to PathwayTr(title = "तुरंत", guidance = listOf("112 पर कॉल करें"))),
                ),
            ),
        )
        val hi = english.withTranslation(t)
        val s = hi.situations.first()
        assertEquals("जलना", s.title)
        assertEquals("हाँ", s.questions.first().options.first { it.value == "serious" }.label)
        assertEquals("No", s.questions.first().options.first { it.value == "minor" }.label) // not translated: stays English
        assertEquals(english.situations.first().rules, s.rules)
        assertEquals("तुरंत", s.pathways.first { it.id == "urgent" }.title)
        assertEquals("Basic", s.pathways.first { it.id == "basic" }.title)
        assertEquals(emptyList(), validateData(hi))
    }

    @Test
    fun english_search_words_are_kept_so_both_languages_find_the_situation() {
        val hi = english.withTranslation(Translation(situations = mapOf("burns" to SituationTr(searchTerms = "जलना झुलसना"))))
        assertTrue(search(hi, "जलना").any { it.id == "burns" })
        assertTrue(search(hi, "scald").any { it.id == "burns" })
    }

    @Test
    fun hindi_words_are_not_cut_at_vowel_signs() {
        assertEquals(listOf("सीपीआर", "शुरू"), tokenize("सीपीआर। शुरू!"))
    }

    @Test
    fun a_broken_quiz_translation_is_ignored() {
        val badAnswer = Translation(quizQuestions = listOf(QuizTr("प्रश्न", listOf("क", "ख", "ग", "घ"), "ङ")))
        assertEquals(english.quizQuestions.first(), english.withTranslation(badAnswer).quizQuestions.first())
        val wrongCount = Translation(quizQuestions = listOf(QuizTr("प्रश्न", listOf("क", "ख"), "क")))
        assertEquals(english.quizQuestions.first(), english.withTranslation(wrongCount).quizQuestions.first())
    }

    @Test
    fun a_good_quiz_translation_is_used() {
        val good = Translation(quizQuestions = listOf(QuizTr("प्रश्न", listOf("क", "ख", "ग", "घ"), "क", "व्याख्या")))
        val q = english.withTranslation(good).quizQuestions.first()
        assertEquals("प्रश्न", q.question)
        assertEquals("क", q.answer)
    }

    @Test
    fun validateTranslation_reports_wrong_step_counts_and_unknown_ids() {
        val wrongSteps = Translation(
            situations = mapOf("burns" to SituationTr(pathways = mapOf("urgent" to PathwayTr(guidance = listOf("एक", "दो", "तीन"))))),
        )
        assertTrue(validateTranslation(english, wrongSteps).isNotEmpty())
        assertTrue(validateTranslation(english, Translation(situations = mapOf("nope" to SituationTr()))).isNotEmpty())
        assertEquals(emptyList(), validateTranslation(english, Translation()))
    }
}

class LanguageStateTest {
    @Test
    fun without_hindi_data_the_language_stays_english() {
        val s = AppState(fixture())
        assertFalse(s.hasHindi)
        s.setLanguage(AppLanguage.Hindi)
        assertEquals(AppLanguage.English, s.language)
    }

    @Test
    fun switching_language_changes_the_data_and_ends_a_running_quiz() {
        val english = fixture()
        val hindi = english.withTranslation(Translation(situations = mapOf("burns" to SituationTr(title = "जलना"))))
        val s = AppState(english, hindi)
        assertTrue(s.hasHindi)
        assertEquals("Burns", s.data.situations.first().title)
        s.startQuiz()
        assertNotNull(s.quiz)
        s.setLanguage(AppLanguage.Hindi)
        assertEquals("जलना", s.data.situations.first().title)
        assertNull(s.quiz)
        s.setLanguage(AppLanguage.English)
        assertEquals("Burns", s.data.situations.first().title)
    }

    @Test
    fun answers_survive_a_language_switch() {
        val english = fixture()
        val hindi = english.withTranslation(Translation())
        val s = AppState(english, hindi)
        s.openSituation("burns")
        s.answers["severity"] = "minor"
        s.setLanguage(AppLanguage.Hindi)
        assertEquals("minor", s.answers["severity"])
    }
}
