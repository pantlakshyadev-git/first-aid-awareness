package com.ben.firstaid

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

// ---------------------------------------------------------------------------
// Theme: Light, Dark and System default
//
// Two layers of colour:
//  1. MaterialTheme.colorScheme (below) - the standard Material roles (primary, surface ...).
//  2. AppColors - the few extra colours this app needs that Material has no role for
//     (top bar, navigation bars, emergency card, quiz feedback).
// Screens never hard-code colours. They read them from the theme, so switching the theme
// recolours everything at once.
// ---------------------------------------------------------------------------

/** Fixed colours that look the same in every theme. */
internal val EmergencyRed = Color(0xFFC92A2A) // background of the red Emergency / Call buttons (white text on it)

@Immutable
internal class AppColors(
    val isDark: Boolean,
    val topBar: Color,
    val onTopBarMuted: Color,
    val sideNav: Color,
    val navBar: Color,
    val navUnselected: Color,
    val emergencyContainer: Color,
    val emergencyAccent: Color,
    val correctContainer: Color,
    val correctText: Color,
    val wrongContainer: Color,
    val wrongText: Color,
)

internal val LightAppColors = AppColors(
    isDark = false,
    topBar = Color(0xFF0F5257),
    onTopBarMuted = Color(0xFFD7EEF3),
    sideNav = Color(0xFFF0F6F7),
    navBar = Color.White,
    navUnselected = Color(0xFF64748B),
    emergencyContainer = Color(0xFFFFF0F0),
    emergencyAccent = Color(0xFFC92A2A),
    correctContainer = Color(0xFFE6F4EA),
    correctText = Color(0xFF137333),
    wrongContainer = Color(0xFFFCE8E6),
    wrongText = Color(0xFFC5221F),
)

internal val DarkAppColors = AppColors(
    isDark = true,
    topBar = Color(0xFF0B3C40),
    onTopBarMuted = Color(0xFFB8D8DC),
    sideNav = Color(0xFF111A1C),
    navBar = Color(0xFF152124),
    navUnselected = Color(0xFF9AAEB1),
    emergencyContainer = Color(0xFF3A2224),
    emergencyAccent = Color(0xFFFF8A80),
    correctContainer = Color(0xFF16301F),
    correctText = Color(0xFF81C995),
    wrongContainer = Color(0xFF3F1F1D),
    wrongText = Color(0xFFF28B82),
)

internal val LocalAppColors = staticCompositionLocalOf { LightAppColors }

/** Use as MaterialTheme.appColors.topBar */
internal val MaterialTheme.appColors: AppColors
    @Composable @ReadOnlyComposable get() = LocalAppColors.current

// The names below already exist all over the screens. They are now read from the current theme
// instead of being fixed colours, so no screen needed to change just for these.
internal val AppBlue: Color @Composable @ReadOnlyComposable get() = MaterialTheme.colorScheme.primary
internal val AppBlueDark: Color @Composable @ReadOnlyComposable get() = MaterialTheme.colorScheme.onPrimaryContainer
internal val SoftBlue: Color @Composable @ReadOnlyComposable get() = MaterialTheme.colorScheme.secondaryContainer
internal val PrimaryContainerBlue: Color @Composable @ReadOnlyComposable get() = MaterialTheme.colorScheme.primaryContainer
internal val CardBorder: Color @Composable @ReadOnlyComposable get() = MaterialTheme.colorScheme.outline

/** Red for TEXT and icons (a lighter red in dark mode so it stays readable). Buttons use EmergencyRed. */
internal val EmergencyAccent: Color @Composable @ReadOnlyComposable get() = MaterialTheme.appColors.emergencyAccent

private val LightScheme = lightColorScheme(
    primary = Color(0xFF0F5257),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFD3ECF0),
    onPrimaryContainer = Color(0xFF0B3C40),
    secondary = Color(0xFF0F5257),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFE6F4F5),
    onSecondaryContainer = Color(0xFF0E2A2D),
    background = Color(0xFFF7FAFA),
    onBackground = Color(0xFF191C1D),
    surface = Color.White,
    onSurface = Color(0xFF191C1D),
    surfaceVariant = Color(0xFFE6F4F5),
    onSurfaceVariant = Color(0xFF4A5568),
    error = EmergencyRed,
    outline = Color(0xFFD4E3E5),
    surfaceContainerLowest = Color.White,
    surfaceContainerLow = Color(0xFFF4F8F8),
    surfaceContainer = Color(0xFFEEF4F5),
    surfaceContainerHigh = Color(0xFFE8F0F1),
    surfaceContainerHighest = Color(0xFFE2EBEC),
)

private val DarkScheme = darkColorScheme(
    primary = Color(0xFF7CCFD6),
    onPrimary = Color(0xFF00363A),
    primaryContainer = Color(0xFF0F4C52),
    onPrimaryContainer = Color(0xFFC4EEF2),
    secondary = Color(0xFF7CCFD6),
    onSecondary = Color(0xFF00363A),
    secondaryContainer = Color(0xFF1B3A3E),
    onSecondaryContainer = Color(0xFFD6ECEE),
    background = Color(0xFF0E1517),
    onBackground = Color(0xFFE1ECED),
    surface = Color(0xFF152023),
    onSurface = Color(0xFFE1ECED),
    surfaceVariant = Color(0xFF1B3A3E),
    onSurfaceVariant = Color(0xFFB4C7C9),
    error = Color(0xFFFFB4AB),
    onError = Color(0xFF690005),
    outline = Color(0xFF3E5A5D),
    surfaceContainerLowest = Color(0xFF0B1113),
    surfaceContainerLow = Color(0xFF111B1D),
    surfaceContainer = Color(0xFF152023),
    surfaceContainerHigh = Color(0xFF1B282B),
    surfaceContainerHighest = Color(0xFF223134),
)

/**
 * Wraps the whole app. [mode] decides light, dark or "whatever the device is using right now".
 * isSystemInDarkTheme() updates by itself when the device switches, so System default follows live.
 */
@Composable
internal fun AppTheme(mode: ThemeMode = ThemeMode.System, content: @Composable () -> Unit) {
    val dark = mode.isDark(isSystemInDarkTheme())
    CompositionLocalProvider(LocalAppColors provides if (dark) DarkAppColors else LightAppColors) {
        MaterialTheme(colorScheme = if (dark) DarkScheme else LightScheme) {
            SystemBarsEffect(darkTheme = dark) // Android status/navigation bar icon colours
            content()
        }
    }
}

// ---------------------------------------------------------------------------
// Remembering the choice
// ---------------------------------------------------------------------------

internal const val THEME_PREFERENCE_KEY = "theme_mode"

/** Holds the chosen [ThemeMode] and saves every change to the device. */
internal class ThemeController(private val store: PreferenceStore) {
    var mode by mutableStateOf(ThemeMode.fromPreference(store.getString(THEME_PREFERENCE_KEY)))
        private set

    fun select(newMode: ThemeMode) {
        if (newMode == mode) return
        mode = newMode
        store.putString(THEME_PREFERENCE_KEY, newMode.preferenceValue)
    }
}
