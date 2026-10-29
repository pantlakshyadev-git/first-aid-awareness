package com.ben.firstaid

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlin.jvm.JvmName

internal enum class Screen(val title: String) {
    Home("Home"),
    Guide("Guided First Aid"),
    Assessment("Assessment"),
    Guidance("Guidance"),
    Learn("Learn"),
    Lesson("Lesson"),
    Kit("First-Aid Kit"),
    KitItem("Kit Item"),
    Quiz("Knowledge Quiz"),
    Search("Search"),
    Emergency("Emergency Help"),
    Sources("Sources"),
    About("About"),
}

/** The top-level screen a detail screen belongs to (used to highlight navigation). */
internal val Screen.section: Screen
    get() = when (this) {
        Screen.Assessment, Screen.Guidance -> Screen.Guide
        Screen.Lesson -> Screen.Learn
        Screen.KitItem -> Screen.Kit
        else -> this
    }

/**
 * All state the screens share. It lives here (not inside composables) so it survives
 * screen changes and Android rotation, and so the logic can be unit-tested.
 */
internal class AppState(
    private val englishData: FirstAidData,
    private val hindiData: FirstAidData? = null,
) {
    @set:JvmName("setLanguageInternal")
    var language by mutableStateOf(AppLanguage.English)
        private set

    /** The content in the chosen language (English if no Hindi data could be loaded). */
    val data: FirstAidData
        get() = if (language == AppLanguage.Hindi && hindiData != null) hindiData else englishData

    val hasHindi: Boolean get() = hindiData != null

    fun setLanguage(newLanguage: AppLanguage) {
        if (newLanguage == AppLanguage.Hindi && hindiData == null) return
        if (newLanguage != language) {
            language = newLanguage
            quiz = null // a quiz in progress would otherwise mix two languages
        }
    }

    /** Navigation back stack. The last entry is the visible screen. Replaced as a whole, never edited in place. */
    var stack by mutableStateOf(listOf(Screen.Home))
        private set

    val current: Screen get() = stack.last()
    val canGoBack: Boolean get() = stack.size > 1

    var situationId by mutableStateOf<String?>(null)
        private set
    var lessonId by mutableStateOf<String?>(null)
        private set
    var kitId by mutableStateOf<String?>(null)
        private set

    /** Assessment answers: question id -> option value. */
    val answers = mutableStateMapOf<String, String>()

    var query by mutableStateOf("")

    var quizCategory by mutableStateOf("All")
    var quizCount by mutableStateOf(5)
    var quiz by mutableStateOf<QuizSession?>(null)
        private set

    // --- navigation ---------------------------------------------------------

    /** Go to a top-level screen. Back from it returns to Home. */
    fun openTab(screen: Screen) {
        stack = if (screen == Screen.Home) listOf(Screen.Home) else listOf(Screen.Home, screen)
    }

    /** Open a screen on top of the current one so Back returns here. */
    fun push(screen: Screen) {
        if (current != screen) stack = stack + screen
    }

    fun back() {
        if (stack.size > 1) stack = stack.dropLast(1)
    }

    // --- details ------------------------------------------------------------

    fun openSituation(id: String) {
        situationId = id
        answers.clear()
        push(Screen.Assessment)
    }

    fun openLesson(id: String) {
        lessonId = id
        push(Screen.Lesson)
    }

    fun openKitItem(id: String) {
        kitId = id
        push(Screen.KitItem)
    }

    // --- quiz ---------------------------------------------------------------

    fun startQuiz() {
        val questions = makeQuiz(data.quizQuestions, quizCategory, quizCount)
        quiz = if (questions.isEmpty()) null else QuizSession(questions)
    }

    fun endQuiz() {
        quiz = null
    }
}
