package com.ben.firstaid

import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

/** Small hand-made data so these tests do not depend on the real JSON. */
internal fun fixture(): FirstAidData {
    val situation = Situation(
        id = "burns",
        title = "Burns",
        questions = listOf(
            Question("severity", "Serious?", listOf(Option("Yes", "serious"), Option("No", "minor"))),
            Question("location", "Sensitive area?", listOf(Option("Yes", "sensitive"), Option("No", "other"))),
        ),
        rules = listOf(
            Rule(mapOf("severity" to "serious"), "urgent"),
            Rule(mapOf("location" to "sensitive"), "urgent"),
            Rule(mapOf("severity" to "minor", "location" to "other"), "basic"),
        ),
        defaultPathway = "urgent",
        pathways = listOf(Pathway("basic", "Basic", guidance = listOf("Cool the burn")), Pathway("urgent", "Urgent", guidance = listOf("Call 112"))),
        searchTerms = "burn scald",
        sourceIds = listOf("src"),
    )
    return FirstAidData(
        sources = listOf(Source("src", "Org", "Title")),
        situations = listOf(situation),
        lessons = listOf(Lesson("l1", "Heart awareness", summary = "Heart attack and arrest", sections = listOf(listOf("Heading", "Electricity can be a danger")))),
        kitItems = listOf(KitItem("gloves", "Disposable Gloves", purpose = "Barrier")),
        quizQuestions = listOf(
            QuizQuestion("Burns", "Q1", listOf("a", "b", "c", "d"), "a"),
            QuizQuestion("Burns", "Q2", listOf("a", "b", "c", "d"), "b"),
            QuizQuestion("Choking", "Q3", listOf("a", "b", "c", "d"), "c"),
        ),
    )
}

class RuleEngineTest {
    private val situation = fixture().situations.first()

    @Test
    fun serious_burn_is_urgent() {
        val result = evaluatePathway(situation, mapOf("severity" to "serious", "location" to "other"))
        assertEquals("urgent", result.id)
    }

    @Test
    fun minor_burn_in_other_place_is_basic() {
        val result = evaluatePathway(situation, mapOf("severity" to "minor", "location" to "other"))
        assertEquals("basic", result.id)
    }

    @Test
    fun minor_burn_on_sensitive_area_escalates() {
        val result = evaluatePathway(situation, mapOf("severity" to "minor", "location" to "sensitive"))
        assertEquals("urgent", result.id)
    }

    @Test
    fun unexpected_answers_use_the_default_pathway() {
        val result = evaluatePathway(situation, mapOf("severity" to "???", "location" to "???"))
        assertEquals("urgent", result.id)
    }

    @Test
    fun allAnswered_needs_every_question_with_a_real_option() {
        assertFalse(allAnswered(situation, emptyMap()))
        assertFalse(allAnswered(situation, mapOf("severity" to "minor")))
        assertFalse(allAnswered(situation, mapOf("severity" to "minor", "location" to "not-an-option")))
        assertTrue(allAnswered(situation, mapOf("severity" to "minor", "location" to "other")))
    }
}

class ValidatorTest {
    @Test
    fun good_data_has_no_problems() {
        assertEquals(emptyList(), validateData(fixture()))
    }

    @Test
    fun bad_default_pathway_is_reported() {
        val data = fixture()
        val broken = data.situations.first().copy(defaultPathway = "nope")
        assertTrue(validateData(data.copy(situations = listOf(broken))).isNotEmpty())
    }

    @Test
    fun rule_with_unknown_answer_is_reported() {
        val data = fixture()
        val broken = data.situations.first().copy(rules = listOf(Rule(mapOf("severity" to "zzz"), "basic")))
        assertTrue(validateData(data.copy(situations = listOf(broken))).isNotEmpty())
    }

    @Test
    fun unknown_source_is_reported() {
        val data = fixture()
        val broken = data.situations.first().copy(sourceIds = listOf("missing"))
        assertTrue(validateData(data.copy(situations = listOf(broken))).isNotEmpty())
    }

    @Test
    fun quiz_answer_that_is_not_an_option_is_reported() {
        val data = fixture().copy(quizQuestions = listOf(QuizQuestion("x", "?", listOf("a", "b"), "c")))
        assertTrue(validateData(data).isNotEmpty())
    }
}

class SearchTest {
    private val data = fixture()

    @Test
    fun matches_the_start_of_a_word() {
        assertTrue(search(data, "burn").any { it.title == "Burns" })
        assertTrue(search(data, "disp").any { it.title == "Disposable Gloves" })
    }

    @Test
    fun does_not_match_inside_a_word() {
        assertFalse(search(data, "art").any { it.title == "Heart awareness" })
    }

    @Test
    fun finds_text_inside_lesson_sections() {
        assertTrue(search(data, "electricity").any { it.title == "Heart awareness" })
    }

    @Test
    fun empty_query_gives_no_results() {
        assertEquals(emptyList(), search(data, "   "))
    }
}

class QuizTest {
    private val questions = fixture().quizQuestions

    @Test
    fun makeQuiz_respects_count_and_category() {
        assertEquals(2, makeQuiz(questions, "All", 2, Random(1)).size)
        assertEquals(3, makeQuiz(questions, "All", 20, Random(1)).size)
        assertTrue(makeQuiz(questions, "Burns", 20, Random(1)).all { it.category == "Burns" })
    }

    @Test
    fun makeQuiz_keeps_the_answer_among_the_options() {
        makeQuiz(questions, "All", 3, Random(7)).forEach { assertTrue(it.answer in it.options) }
    }

    @Test
    fun session_scores_correct_answers_and_locks_after_the_first_choice() {
        val session = QuizSession(listOf(QuizQuestion("c", "Q", listOf("a", "b"), "a")))
        session.choose("b") // wrong
        session.choose("a") // ignored: already answered
        assertEquals("b", session.selected)
        assertEquals(0, session.score)
        session.next()
        assertTrue(session.finished)
    }

    @Test
    fun session_counts_a_correct_answer_once() {
        val session = QuizSession(listOf(QuizQuestion("c", "Q", listOf("a", "b"), "a")))
        session.choose("a")
        session.choose("a")
        assertEquals(1, session.score)
    }

    @Test
    fun next_does_nothing_until_an_answer_is_chosen() {
        val session = QuizSession(questions)
        session.next()
        assertEquals(0, session.index)
    }

    @Test
    fun percent_handles_zero_total() {
        assertEquals(0, percent(0, 0))
        assertEquals(80, percent(4, 5))
    }
}

class NavigationTest {
    private fun state() = AppState(fixture())

    @Test
    fun starts_on_home_with_nothing_to_go_back_to() {
        val s = state()
        assertEquals(Screen.Home, s.current)
        assertFalse(s.canGoBack)
    }

    @Test
    fun opening_a_situation_clears_old_answers_and_back_returns_to_the_list() {
        val s = state()
        s.openTab(Screen.Guide)
        s.answers["severity"] = "minor"
        s.openSituation("burns")
        assertEquals(Screen.Assessment, s.current)
        assertTrue(s.answers.isEmpty())
        s.back()
        assertEquals(Screen.Guide, s.current)
        s.back()
        assertEquals(Screen.Home, s.current)
    }

    @Test
    fun emergency_button_keeps_the_screen_underneath() {
        val s = state()
        s.openTab(Screen.Guide)
        s.openSituation("burns")
        s.push(Screen.Emergency)
        s.back()
        assertEquals(Screen.Assessment, s.current)
    }

    @Test
    fun detail_screens_belong_to_their_section() {
        assertEquals(Screen.Guide, Screen.Guidance.section)
        assertEquals(Screen.Learn, Screen.Lesson.section)
        assertEquals(Screen.Kit, Screen.KitItem.section)
    }

    @Test
    fun quiz_can_start_and_end() {
        val s = state()
        s.startQuiz()
        assertNotNull(s.quiz)
        s.endQuiz()
        assertNull(s.quiz)
    }
}
