package com.ben.firstaid

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

/** Version shown on the About screen. Keep in sync with versionName in composeApp/build.gradle.kts. */
internal const val APP_VERSION = "1.0.0"
internal const val EMERGENCY_NUMBER = "112"

internal const val AMBULANCE_NOTE =
    "112 is India's unified emergency number for urgent assistance, including ambulance, " +
        "police and fire services. Some local or state services may also use other numbers."

internal val AppJson = Json { ignoreUnknownKeys = true }

@Serializable
internal data class Source(
    val id: String,
    val organization: String,
    val title: String,
    val purpose: String = "",
    val url: String = "",
)

@Serializable
internal data class Option(val label: String, val value: String)

@Serializable
internal data class Question(val id: String, val text: String, val options: List<Option>)

@Serializable
internal data class Rule(val conditions: Map<String, String> = emptyMap(), val pathway: String)

@Serializable
internal data class Pathway(
    val id: String,
    val title: String,
    val alert: String = "",
    val guidance: List<String> = emptyList(),
    @SerialName("professional_help") val professionalHelp: List<String> = emptyList(),
)

@Serializable
internal data class Situation(
    val id: String,
    val title: String,
    val icon: String = "",
    val summary: String = "",
    @SerialName("search_terms") val searchTerms: String = "",
    @SerialName("assessment_intro") val assessmentIntro: String = "",
    val disclaimer: String = "",
    val questions: List<Question> = emptyList(),
    val rules: List<Rule> = emptyList(),
    @SerialName("default_pathway") val defaultPathway: String = "",
    val pathways: List<Pathway> = emptyList(),
    @SerialName("source_ids") val sourceIds: List<String> = emptyList(),
)

@Serializable
internal data class Lesson(
    val id: String,
    val title: String,
    val icon: String = "",
    val summary: String = "",
    /** Each section is [heading, text]. */
    val sections: List<List<String>> = emptyList(),
)

@Serializable
internal data class KitItem(
    val id: String,
    val name: String,
    val icon: String = "",
    val purpose: String = "",
    val what: String = "",
    val details: String = "",
    val use: String = "",
    val precautions: String = "",
    val image: String = "",
)

@Serializable
internal data class QuizQuestion(
    val category: String,
    val question: String,
    val options: List<String>,
    val answer: String,
    val explanation: String = "",
)

@Serializable
internal data class FirstAidData(
    val sources: List<Source> = emptyList(),
    val situations: List<Situation> = emptyList(),
    val lessons: List<Lesson> = emptyList(),
    @SerialName("kit_items") val kitItems: List<KitItem> = emptyList(),
    @SerialName("quiz_questions") val quizQuestions: List<QuizQuestion> = emptyList(),
)
