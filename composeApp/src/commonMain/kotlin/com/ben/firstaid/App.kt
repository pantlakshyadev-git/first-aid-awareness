package com.ben.firstaid

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ben.firstaid.generated.resources.Res
import kotlin.coroutines.cancellation.CancellationException

private sealed interface LoadState {
    data object Loading : LoadState
    data class Failed(val problems: List<String>) : LoadState
    data class Ready(val data: FirstAidData, val hindi: FirstAidData?) : LoadState
}

private suspend fun loadAndValidate(): LoadState = try {
    val text = Res.readBytes("files/first_aid_data.json").decodeToString()
    val data = AppJson.decodeFromString(FirstAidData.serializer(), text)
    val problems = validateData(data)
    if (problems.isEmpty()) LoadState.Ready(data, loadHindi(data)) else LoadState.Failed(problems)
} catch (e: CancellationException) {
    throw e
} catch (e: Exception) {
    LoadState.Failed(listOf(e.message ?: e.toString()))
}

/** The Hindi file is optional: if it is missing or damaged the app simply stays English. */
private suspend fun loadHindi(english: FirstAidData): FirstAidData? = try {
    val text = Res.readBytes("files/first_aid_data_hi.json").decodeToString()
    val translation = AppJson.decodeFromString(Translation.serializer(), text)
    // Reject inconsistent translation data instead of partially applying broken content.
    if (validateTranslation(english, translation).isNotEmpty()) {
        null
    } else {
        english.withTranslation(translation)
    }
} catch (e: CancellationException) {
    throw e
} catch (e: Exception) {
    null
}

@Composable
fun App() {
    var load by remember { mutableStateOf<LoadState>(LoadState.Loading) }
    LaunchedEffect(Unit) { load = loadAndValidate() }

    // The theme is chosen before the data loads, so even the loading/error screen uses it.
    val preferenceStore = rememberPreferenceStore()
    val themeController = remember(preferenceStore) { ThemeController(preferenceStore) }

    AppTheme(themeController.mode) {
        Surface(Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
            when (val current = load) {
                is LoadState.Loading -> StartupMessage(loading = true, problems = emptyList())
                is LoadState.Failed -> StartupMessage(loading = false, problems = current.problems)
                is LoadState.Ready -> {
                    val state = remember(current.data) { AppState(current.data, current.hindi) }
                    CompositionLocalProvider(LocalLanguage provides state.language) {
                        MainLayout(state, themeController)
                    }
                }
            }
        }
    }
}

@Composable
private fun StartupMessage(loading: Boolean, problems: List<String>) {
    Box(
        Modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.safeDrawing),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            Modifier
                .padding(24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(
                "First Aid Awareness",
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.headlineSmall,
                color = AppBlue,
            )
            if (loading) {
                CircularProgressIndicator(
                    color = AppBlue,
                )
            } else {
                Text(
                    "The bundled first-aid data could not be loaded or failed validation. Please reinstall the app.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.error,
                )
                problems.forEach { Text("\u2022 $it", style = MaterialTheme.typography.bodySmall) }
                Text(
                    "In an emergency, call $EMERGENCY_NUMBER.",
                    fontWeight = FontWeight.Bold,
                    color = EmergencyAccent,
                )
            }
        }
    }
}

@Composable
private fun MainLayout(state: AppState, themeController: ThemeController) {
    // Android system Back. Desktop and iOS use the Back button in the top bar.
    PlatformBackHandler(enabled = state.canGoBack) { state.back() }

    BoxWithConstraints(
        Modifier.fillMaxSize().windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Horizontal)),
    ) {
        val compact = maxWidth < 700.dp
        Row(Modifier.fillMaxSize()) {
            if (!compact) SideNav(state)
            Column(Modifier.weight(1f).fillMaxHeight()) {
                TopBar(state)
                val contentModifier = if (compact) {
                    Modifier
                } else {
                    Modifier.windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Bottom))
                }
                Box(Modifier.weight(1f).fillMaxWidth().then(contentModifier)) {
                    ScreenHost(state, themeController)
                }
                if (compact) BottomNav(state)
            }
        }
    }
}

@Composable
private fun ScreenHost(state: AppState, themeController: ThemeController) {
    // key(...) gives every screen/item a fresh scroll position.
    key(state.current, state.situationId, state.lessonId, state.kitId) {
        when (state.current) {
            Screen.Home -> HomeScreen(state)
            Screen.Guide -> SituationsScreen(state)
            Screen.Assessment -> AssessmentScreen(state)
            Screen.Guidance -> GuidanceScreen(state)
            Screen.Learn -> LearnScreen(state)
            Screen.Lesson -> LessonScreen(state)
            Screen.Kit -> KitScreen(state)
            Screen.KitItem -> KitDetailScreen(state)
            Screen.Quiz -> QuizScreen(state)
            Screen.Search -> SearchScreen(state)
            Screen.Emergency -> EmergencyScreen(state)
            Screen.Sources -> SourcesScreen(state)
            Screen.About -> AboutScreen()
            Screen.Settings -> SettingsScreen(themeController.mode, themeController::select)
        }
    }
}

@Composable
private fun TopBar(state: AppState) {
    Surface(color = MaterialTheme.appColors.topBar, contentColor = Color.White, shadowElevation = 3.dp) {
        Row(
            Modifier
                .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Top))
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (state.canGoBack) {
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = Color.White.copy(alpha = 0.15f),
                    modifier = Modifier.padding(end = 6.dp)
                ) {
                    TextButton(
                        onClick = { state.back() },
                        colors = ButtonDefaults.textButtonColors(contentColor = Color.White),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                    ) {
                        Text("\u2190 " + tr("Back"), fontWeight = FontWeight.Bold)
                    }
                }
            }
            Column(
                Modifier
                    .weight(1f)
                    .clickable(role = Role.Button) { state.openTab(Screen.Home) }
                    .padding(horizontal = 8.dp, vertical = 4.dp),
            ) {
                if (state.current == Screen.Home) {
                    Text(
                        tr("First Aid Awareness"),
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium,
                    )
                    Text(
                        tr("LEARN  ·  ASSIST  ·  ESCALATE"),
                        color = MaterialTheme.appColors.onTopBarMuted,
                        style = MaterialTheme.typography.labelSmall,
                    )
                } else {
                    Text(
                        tr(state.current.title),
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium,
                    )
                }
            }
            if (state.hasHindi) {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = Color.White.copy(alpha = 0.2f),
                    modifier = Modifier.padding(end = 8.dp),
                ) {
                    TextButton(
                        onClick = {
                            state.setLanguage(if (state.language == AppLanguage.Hindi) AppLanguage.English else AppLanguage.Hindi)
                        },
                        colors = ButtonDefaults.textButtonColors(contentColor = Color.White),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                    ) {
                        Text(
                            if (state.language == AppLanguage.Hindi) "EN" else "हि",
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.labelMedium,
                        )
                    }
                }
            }
            // Always one tap away, on every screen.
            Button(
                onClick = { state.push(Screen.Emergency) },
                shape = RoundedCornerShape(20.dp),
                colors = ButtonDefaults.buttonColors(containerColor = EmergencyRed, contentColor = Color.White),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp, pressedElevation = 4.dp),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
            ) {
                IconGlyph(AppIcon.Emergency, Modifier.size(18.dp), Color.White)
                Spacer(Modifier.width(6.dp))
                Text(tr("Emergency"), fontWeight = FontWeight.Bold)
            }
        }
    }
}

private val sideScreens = listOf(
    Screen.Home, Screen.Guide, Screen.Learn, Screen.Kit, Screen.Quiz,
    Screen.Search, Screen.Emergency, Screen.Sources, Screen.About, Screen.Settings,
)

@Composable
private fun SideNav(state: AppState) {
    Surface(color = MaterialTheme.appColors.sideNav, modifier = Modifier.width(220.dp).fillMaxHeight()) {
        Column(
            Modifier
                .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Vertical))
                .verticalScroll(rememberScrollState())
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            sideScreens.forEach { screen ->
                val selected = state.current.section == screen
                Surface(
                    color = if (selected) PrimaryContainerBlue else Color.Transparent,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .selectable(selected = selected, role = Role.Tab, onClick = { state.openTab(screen) }),
                ) {
                    val tint = if (selected) AppBlueDark else MaterialTheme.colorScheme.onSurface
                    Row(
                        Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        IconGlyph(screenIcon(screen), Modifier.size(22.dp), tint)
                        Spacer(Modifier.width(14.dp))
                        Text(
                            tr(screen.title),
                            color = tint,
                            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                        )
                    }
                }
            }
        }
    }
}

private val bottomItems = listOf(
    Screen.Home to "Home",
    Screen.Guide to "Guide",
    Screen.Learn to "Learn",
    Screen.Quiz to "Quiz",
    Screen.Search to "Search",
)

@Composable
private fun BottomNav(state: AppState) {
    NavigationBar(
        containerColor = MaterialTheme.appColors.navBar,
        tonalElevation = 6.dp,
    ) {
        bottomItems.forEach { (screen, label) ->
            val selected = state.current.section == screen
            NavigationBarItem(
                selected = selected,
                onClick = { state.openTab(screen) },
                icon = { IconGlyph(screenIcon(screen), Modifier.size(24.dp)) },
                label = {
                    Text(
                        tr(label),
                        maxLines = 1,
                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                    )
                },
                alwaysShowLabel = true,
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = AppBlue,
                    selectedTextColor = AppBlue,
                    indicatorColor = PrimaryContainerBlue,
                    unselectedIconColor = MaterialTheme.appColors.navUnselected,
                    unselectedTextColor = MaterialTheme.appColors.navUnselected,
                )
            )
        }
    }
}
