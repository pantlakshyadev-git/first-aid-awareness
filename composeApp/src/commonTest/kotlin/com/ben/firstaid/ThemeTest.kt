package com.ben.firstaid

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue

class ThemeTest {
    @Test
    fun missing_or_unknown_saved_value_falls_back_to_system_default() {
        assertEquals(ThemeMode.System, ThemeMode.fromPreference(null))
        assertEquals(ThemeMode.System, ThemeMode.fromPreference(""))
        assertEquals(ThemeMode.System, ThemeMode.fromPreference("purple"))
    }

    @Test
    fun every_mode_survives_save_and_load() {
        for (mode in ThemeMode.entries) {
            assertEquals(mode, ThemeMode.fromPreference(mode.preferenceValue))
        }
        assertEquals(ThemeMode.entries.size, ThemeMode.entries.map { it.preferenceValue }.toSet().size)
    }

    @Test
    fun system_follows_the_device_while_light_and_dark_ignore_it() {
        assertTrue(ThemeMode.System.isDark(systemIsDark = true))
        assertFalse(ThemeMode.System.isDark(systemIsDark = false))
        assertFalse(ThemeMode.Light.isDark(systemIsDark = true))
        assertTrue(ThemeMode.Dark.isDark(systemIsDark = false))
    }

    @Test
    fun controller_starts_from_the_saved_choice() {
        val store = InMemoryPreferenceStore(mapOf(THEME_PREFERENCE_KEY to "dark"))
        assertEquals(ThemeMode.Dark, ThemeController(store).mode)
        assertEquals(ThemeMode.System, ThemeController(InMemoryPreferenceStore()).mode)
    }

    @Test
    fun controller_saves_every_change_and_a_new_controller_restores_it() {
        val store = InMemoryPreferenceStore()
        val controller = ThemeController(store)
        controller.select(ThemeMode.Light)
        assertEquals(ThemeMode.Light, controller.mode)
        assertEquals("light", store.getString(THEME_PREFERENCE_KEY))
        controller.select(ThemeMode.Dark)
        assertEquals(ThemeMode.Dark, ThemeController(store).mode)
    }

    // ----- readable colours (WCAG AA: at least 4.5 : 1 for normal text) -----

    private fun contrast(a: Color, b: Color): Float {
        val la = a.luminance()
        val lb = b.luminance()
        return (maxOf(la, lb) + 0.05f) / (minOf(la, lb) + 0.05f)
    }

    private fun assertReadable(name: String, text: Color, background: Color) {
        val ratio = contrast(text, background)
        assertTrue(ratio >= 4.5f, "$name has contrast $ratio, needs at least 4.5")
    }

    @Test
    fun extra_app_colours_are_readable_in_both_themes() {
        for (colors in listOf(LightAppColors, DarkAppColors)) {
            val theme = if (colors.isDark) "dark" else "light"
            assertReadable("$theme: white on top bar", Color.White, colors.topBar)
            assertReadable("$theme: muted subtitle on top bar", colors.onTopBarMuted, colors.topBar)
            assertReadable("$theme: unselected nav item", colors.navUnselected, colors.navBar)
            assertReadable("$theme: emergency text", colors.emergencyAccent, colors.emergencyContainer)
            assertReadable("$theme: correct feedback", colors.correctText, colors.correctContainer)
            assertReadable("$theme: wrong feedback", colors.wrongText, colors.wrongContainer)
            assertReadable("$theme: white on the red Emergency button", Color.White, EmergencyRed)
        }
    }

    @Test
    fun light_and_dark_palettes_really_differ() {
        assertNotEquals(LightAppColors.navBar, DarkAppColors.navBar)
        assertNotEquals(LightAppColors.emergencyContainer, DarkAppColors.emergencyContainer)
        assertTrue(LightAppColors.navBar.luminance() > DarkAppColors.navBar.luminance())
    }
}
