package com.ben.firstaid

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

// Every fixed piece of interface text goes through tr(text) so it can be shown in Hindi.
// Text that comes from the JSON data (situations, lessons, kit, quiz) is translated in the data instead.
// Keep each tr(...) text on ONE line with no $ signs: a test checks it against the Hindi list in UiStrings.kt.

// ---------------------------------------------------------------------------
// Home
// ---------------------------------------------------------------------------

private data class HomeEntry(val screen: Screen, val description: String, val meta: String, val arg: Any? = null)

@Composable
internal fun HomeScreen(state: AppState) {
    val data = state.data
    val entries = listOf(
        HomeEntry(Screen.Guide, "Choose a situation and follow an awareness pathway.", "{0} situations", data.situations.size),
        HomeEntry(Screen.Learn, "Read short lessons covering first-aid fundamentals.", "{0} lessons", data.lessons.size),
        HomeEntry(Screen.Kit, "Explore common kit items and their purposes.", "{0} items", data.kitItems.size),
        HomeEntry(Screen.Quiz, "Practise your understanding with a short quiz.", "{0} questions", data.quizQuestions.size),
        HomeEntry(Screen.Search, "Find topics across lessons, guidance and kit items.", "Works offline"),
        HomeEntry(Screen.Emergency, "Review key emergency reminders.", "Emergency number {0}", EMERGENCY_NUMBER),
        HomeEntry(Screen.Sources, "Review references used by the educational content.", "{0} references", data.sources.size),
        HomeEntry(Screen.About, "Project purpose and safety disclaimer.", "Project information"),
    )
    Page {
        PageTitle(tr("Be prepared to help"), tr("A simple offline-first guide to first-aid awareness."))
        if (state.hasHindi) LanguagePicker(state)
        if (state.language == AppLanguage.Hindi) {
            InfoCard(
                "अनुवाद सूचना",
                "यह हिंदी अनुवाद एक प्रारंभिक मसौदा है और किसी चिकित्सा विशेषज्ञ या हिंदी भाषा विशेषज्ञ द्वारा सत्यापित नहीं है। " +
                    "किसी भी संदेह की स्थिति में अंग्रेज़ी पाठ देखें और तुरंत 112 पर कॉल करें। जिन हिस्सों का अनुवाद अभी नहीं हुआ है, वे अंग्रेज़ी में दिखेंगे।",
            )
        }
        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF0F0)),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth(),
        ) {
            Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconGlyph(AppIcon.Emergency, Modifier.size(24.dp), EmergencyRed)
                    Spacer(Modifier.width(8.dp))
                    Text(
                        tr("In an emergency"),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = EmergencyRed,
                    )
                }
                Text(tr("Check scene safety. Get emergency help for life-threatening situations. Follow the emergency operator's instructions."))
                Text(tr("India emergency number: {0}", EMERGENCY_NUMBER), fontWeight = FontWeight.Bold)
                CallEmergencyButton()
                Text(
                    tr("This app is educational and does not replace professional care."),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        Text(tr("Explore"), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = AppBlue)
        entries.forEach { entry ->
            val meta = if (entry.arg != null) tr(entry.meta, entry.arg) else tr(entry.meta)
            InfoCard(
                title = tr(entry.screen.title),
                body = tr(entry.description) + "\n" + meta,
                icon = screenIcon(entry.screen),
                onClick = { state.openTab(entry.screen) },
            )
        }
    }
}

@Composable
private fun LanguagePicker(state: AppState) {
    AppCard {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Language / भाषा", fontWeight = FontWeight.Bold, color = AppBlue)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                AppLanguage.entries.forEach { language ->
                    FilterChip(
                        selected = state.language == language,
                        onClick = { state.setLanguage(language) },
                        label = { Text(language.nativeName, fontWeight = FontWeight.Bold) },
                        shape = RoundedCornerShape(10.dp),
                    )
                }
            }
        }
    }
}

// ---------------------------------------------------------------------------
// Guided first aid: situation list -> questions -> guidance
// ---------------------------------------------------------------------------

@Composable
internal fun SituationsScreen(state: AppState) {
    Page {
        PageTitle(tr("Guided First Aid"), tr("Select a situation. The result is an awareness pathway, not a diagnosis."))
        state.data.situations.forEach { situation ->
            InfoCard(
                title = situation.title,
                body = situation.summary,
                icon = situationIcon(situation.id),
                onClick = { state.openSituation(situation.id) },
            )
        }
    }
}

@Composable
internal fun AssessmentScreen(state: AppState) {
    val situation = state.data.situation(state.situationId)
    if (situation == null) {
        MissingItem(tr("No situation selected.")) { state.openTab(Screen.Guide) }
        return
    }
    val complete = allAnswered(situation, state.answers)
    val answeredCount = situation.questions.count { state.answers[it.id] != null }
    val totalCount = situation.questions.size
    Page {
        PageTitle(situation.title, situation.assessmentIntro)
        LinearProgressIndicator(
            progress = { if (totalCount > 0) answeredCount.toFloat() / totalCount else 0f },
            modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)),
            color = AppBlue,
            trackColor = SoftBlue,
        )
        situation.questions.forEachIndexed { index, question ->
            AppCard {
                Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        tr("Question {0} of {1}", index + 1, totalCount),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = AppBlue,
                    )
                    Text(question.text, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                    question.options.forEach { option ->
                        RadioRow(
                            label = option.label,
                            selected = state.answers[question.id] == option.value,
                            onClick = { state.answers[question.id] = option.value },
                        )
                    }
                }
            }
        }
        if (!complete) {
            Text(
                tr("Answer all questions to see the guidance ({0} of {1} answered).", answeredCount, totalCount),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Button(
            onClick = { state.push(Screen.Guidance) },
            enabled = complete,
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth().height(48.dp),
        ) {
            Text(tr("Show awareness guidance"), fontWeight = FontWeight.Bold)
        }
        EmergencyReminder()
    }
}

@Composable
private fun EmergencyReminder() {
    Card(
        colors = CardDefaults.cardColors(containerColor = SoftBlue),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(
                tr("If the situation is life-threatening or you are unsure, contact emergency services. In India, call {0}.", EMERGENCY_NUMBER),
                fontWeight = FontWeight.Bold,
                color = AppBlue,
            )
            CallEmergencyButton()
        }
    }
}

@Composable
internal fun GuidanceScreen(state: AppState) {
    val situation = state.data.situation(state.situationId)
    if (situation == null) {
        MissingItem(tr("No situation selected.")) { state.openTab(Screen.Guide) }
        return
    }
    if (!allAnswered(situation, state.answers)) {
        // Never show guidance built from missing answers.
        MissingItem(tr("Some questions are not answered yet.")) { state.back() }
        return
    }
    // Computed from the current answers on every composition, so it can never be out of date.
    val pathway = evaluatePathway(situation, state.answers)
    val sources = state.data.sources.filter { it.id in situation.sourceIds }
    Page {
        PageTitle(tr("Guidance Result"), tr("Awareness pathway for {0}.", situation.title))
        AppCard(containerColor = SoftBlue) {
            Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(pathway.title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = AppBlue)
                if (pathway.alert.isNotBlank()) Text(pathway.alert, fontWeight = FontWeight.Medium)
            }
        }
        BulletSection(tr("What to do"), pathway.guidance)
        if (pathway.professionalHelp.isNotEmpty()) BulletSection(tr("When to seek professional help"), pathway.professionalHelp)
        if (situation.disclaimer.isNotBlank()) InfoCard(tr("Important reminder"), situation.disclaimer)
        EmergencyReminder()
        if (sources.isNotEmpty()) BulletSection(tr("Based on"), sources.map { "${it.organization} \u2014 ${it.title}" })
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            OutlinedButton(onClick = { state.back() }, shape = RoundedCornerShape(10.dp)) {
                Text(tr("Change answers"))
            }
            Button(onClick = { state.openTab(Screen.Guide) }, shape = RoundedCornerShape(10.dp)) {
                Text(tr("All situations"), fontWeight = FontWeight.Bold)
            }
        }
    }
}

// ---------------------------------------------------------------------------
// Learn
// ---------------------------------------------------------------------------

@Composable
internal fun LearnScreen(state: AppState) {
    Page {
        PageTitle(tr("Learn"), tr("Short educational lessons. Read the complete guidance and know when to seek help."))
        state.data.lessons.forEach { lesson ->
            InfoCard(lesson.title, lesson.summary, icon = lessonIcon(lesson.id), onClick = { state.openLesson(lesson.id) })
        }
    }
}

@Composable
internal fun LessonScreen(state: AppState) {
    val lesson = state.data.lesson(state.lessonId)
    if (lesson == null) {
        MissingItem(tr("No lesson selected.")) { state.openTab(Screen.Learn) }
        return
    }
    Page {
        PageTitle(lesson.title, lesson.summary)
        lesson.sections.forEach { section ->
            if (section.size >= 2) InfoCard(section[0], section[1])
        }
    }
}

// ---------------------------------------------------------------------------
// First-aid kit
// ---------------------------------------------------------------------------

@Composable
internal fun KitScreen(state: AppState) {
    val kitSource = state.data.source("redcross_kit")
    Page {
        PageTitle(tr("First-Aid Kit Explorer"), tr("Learn what common kit items are for and why maintaining the kit matters."))
        AppCard(containerColor = SoftBlue) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(tr("Kit maintenance matters"), fontWeight = FontWeight.Bold, color = AppBlue)
                Text(tr("A kit should be checked regularly. Replace used, damaged or out-of-date contents, and keep emergency contact information accessible."))
                if (kitSource != null) {
                    Text(
                        tr("Based on: {0} — {1}", kitSource.organization, kitSource.title),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
        state.data.kitItems.forEach { item ->
            AppCard(onClick = { state.openKitItem(item.id) }) {
                Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                    KitImage(item, Modifier.size(72.dp))
                    Spacer(Modifier.width(16.dp))
                    Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(item.name, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                        Text(item.purpose, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
    }
}

@Composable
internal fun KitDetailScreen(state: AppState) {
    val item = state.data.kitItem(state.kitId)
    if (item == null) {
        MissingItem(tr("No kit item selected.")) { state.openTab(Screen.Kit) }
        return
    }
    Page {
        PageTitle(item.name, item.purpose)
        AppCard {
            KitImage(item, Modifier.fillMaxWidth().height(220.dp).padding(16.dp))
        }
        InfoCard(tr("What it is"), item.what)
        InfoCard(tr("Why it is useful"), item.details)
        InfoCard(tr("General use"), item.use)
        InfoCard(tr("Precautions"), item.precautions)
    }
}

// ---------------------------------------------------------------------------
// Search
// ---------------------------------------------------------------------------

@Composable
internal fun SearchScreen(state: AppState) {
    var selectedFilter by remember { mutableStateOf<SearchType?>(null) }
    val results = remember(state.query, state.language) { search(state.data, state.query) }
    val filteredResults = remember(results, selectedFilter) {
        if (selectedFilter == null) results else results.filter { it.type == selectedFilter }
    }
    Page {
        PageTitle(tr("Search"), tr("Search lessons, guided situations and first-aid kit items. Search works offline."))
        OutlinedTextField(
            value = state.query,
            onValueChange = { state.query = it },
            modifier = Modifier.fillMaxWidth(),
            label = { Text(tr("Search first-aid topics")) },
            leadingIcon = { IconGlyph(AppIcon.Search, Modifier.size(20.dp), AppBlue) },
            trailingIcon = {
                if (state.query.isNotEmpty()) {
                    IconButton(onClick = { state.query = "" }) {
                        Text("\u2715", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            },
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = AppBlue,
                unfocusedBorderColor = CardBorder,
            ),
        )
        OptIn(ExperimentalLayoutApi::class)
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            FilterChip(
                selected = selectedFilter == null,
                onClick = { selectedFilter = null },
                label = { Text(tr("All")) },
                shape = RoundedCornerShape(10.dp),
            )
            SearchType.entries.forEach { type ->
                FilterChip(
                    selected = selectedFilter == type,
                    onClick = { selectedFilter = if (selectedFilter == type) null else type },
                    label = { Text(tr(type.label)) },
                    shape = RoundedCornerShape(10.dp),
                )
            }
        }
        if (state.query.isNotBlank() && filteredResults.isEmpty()) {
            Text(tr("No matches found. Try a shorter or different keyword."))
        }
        filteredResults.forEach { hit ->
            InfoCard(
                title = "${tr(hit.type.label)} \u00B7 ${hit.title}",
                body = hit.summary,
                onClick = {
                    when (hit.type) {
                        SearchType.Situation -> state.openSituation(hit.id)
                        SearchType.Lesson -> state.openLesson(hit.id)
                        SearchType.Kit -> state.openKitItem(hit.id)
                    }
                },
            )
        }
    }
}

// ---------------------------------------------------------------------------
// Emergency, sources, about
// ---------------------------------------------------------------------------

@Composable
internal fun EmergencyScreen(state: AppState) {
    Page {
        PageTitle(tr("Emergency Help"), tr("Use emergency services immediately when a situation may be life-threatening."))
        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF0F0)),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.fillMaxWidth(),
        ) {
            Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(tr("India emergency number"), fontWeight = FontWeight.Bold, color = EmergencyRed)
                Text(EMERGENCY_NUMBER, style = MaterialTheme.typography.displayMedium, fontWeight = FontWeight.Bold, color = AppBlue)
                Text(tr("Primary national emergency number for urgent ambulance, police and fire assistance."))
                CallEmergencyButton()
            }
        }
        InfoCard(tr("Ambulance information"), tr(AMBULANCE_NOTE))
        BulletSection(
            tr("When to escalate: seek emergency help immediately when"),
            listOf(
                tr("someone is unconscious or not responding normally"),
                tr("someone is not breathing normally or may be in cardiac arrest"),
                tr("there is life-threatening bleeding"),
                tr("there are signs of a serious breathing emergency"),
                tr("a serious accident or major injury has occurred"),
                tr("symptoms suggest a potentially life-threatening medical emergency"),
            ),
        )
        InfoCard(
            tr("When calling"),
            tr("Tell the operator your location, what happened, the person's condition and any immediate danger. Follow the emergency operator's instructions."),
        )
        InfoCard(
            tr("Do not delay for the app"),
            tr("This application is an educational tool. Do not spend time navigating the application when professional emergency assistance is needed."),
        )
        OutlinedButton(onClick = { state.openTab(Screen.Guide) }, shape = RoundedCornerShape(10.dp)) {
            Text(tr("Guided First Aid"), fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
internal fun SourcesScreen(state: AppState) {
    Page {
        PageTitle(
            tr("Sources & References"),
            tr("Educational content is adapted from the references below. It is a simplified summary, not a substitute for a certified first-aid course. Opening a link needs internet access."),
        )
        AppCard(containerColor = SoftBlue) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(tr("Research principle"), fontWeight = FontWeight.Bold, color = AppBlue)
                Text(tr("Research → compare → simplify → cite → review. The application is not intended to create medical advice from memory."))
            }
        }
        state.data.sources.forEach { SourceCard(it) }
    }
}

@Composable
private fun SourceCard(source: Source) {
    val uriHandler = LocalUriHandler.current
    var failed by remember { mutableStateOf(false) }
    AppCard {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(source.organization, fontWeight = FontWeight.Bold, color = AppBlue, style = MaterialTheme.typography.labelLarge)
            Text(source.title, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
            Text(source.purpose, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(source.url, style = MaterialTheme.typography.bodySmall)
            if (source.url.isNotBlank()) {
                OutlinedButton(onClick = { failed = !uriHandler.tryOpen(source.url) }, shape = RoundedCornerShape(8.dp)) {
                    Text(tr("Open link"))
                }
            }
            if (failed) {
                Text(
                    tr("Could not open the link on this device."),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error,
                )
            }
        }
    }
}

@Composable
internal fun AboutScreen() {
    Page {
        PageTitle(tr("About the Project"), tr("Innovation & Design Thinking Lab (AID151/AID251) · B.Tech CSE, Year 1"))
        InfoCard(
            tr("Project vision"),
            tr("LEARN → ASSIST → ESCALATE. The system aims to make reliable first-aid awareness easier to access while clearly directing users toward professional help when appropriate."),
        )
        InfoCard(
            tr("How it works"),
            tr("The user selects a situation, answers structured questions, and the rule engine selects a predefined awareness pathway. Content is stored locally in JSON so the app works without a database or internet connection."),
        )
        InfoCard(
            tr("Why rule-based logic"),
            tr("For a first-year project, predefined decision logic is easier to test, explain and validate than a medical diagnosis model. The application intentionally avoids claiming to diagnose users."),
        )
        InfoCard(
            tr("Technology"),
            tr("Kotlin · Compose Multiplatform · bundled JSON. The original Python/Tkinter version and its unit tests are kept in python_reference/ for comparison. App version {0}.", APP_VERSION),
        )
        InfoCard(
            tr("Languages"),
            tr("The app is available in English and Hindi. The Hindi text is a draft translation that has not been checked by a medical or language expert."),
        )
        InfoCard(
            tr("Project limitations"),
            tr("This is an educational prototype, not a medical device, diagnostic system, emergency dispatch service or substitute for a certified first-aid course, emergency services or professional medical care. Its content is a simplified summary of the cited sources and should be reviewed by a qualified first-aid professional before public release."),
        )
        InfoCard(
            tr("Responsible use"),
            tr("Educational awareness tool only. It does not diagnose conditions or replace trained first-aiders, doctors or emergency services. In a serious or life-threatening emergency, contact emergency services immediately."),
        )
    }
}
