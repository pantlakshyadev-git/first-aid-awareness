package com.ben.firstaid

/** User preference for the application's appearance. */
internal enum class ThemeMode(val preferenceValue: String) {
    System("system"), Light("light"), Dark("dark");

    fun isDark(systemIsDark: Boolean): Boolean = when (this) {
        System -> systemIsDark
        Light -> false
        Dark -> true
    }

    companion object {
        fun fromPreference(value: String?): ThemeMode = entries.firstOrNull { it.preferenceValue == value } ?: System
    }
}
