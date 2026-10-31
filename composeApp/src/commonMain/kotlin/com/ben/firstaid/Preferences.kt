package com.ben.firstaid

/**
 * A tiny key/value store for small user settings (for example the chosen theme).
 * Each platform provides its own storage in rememberPreferenceStore() (see Platform.kt):
 * Android = SharedPreferences, desktop = java.util.prefs, iOS = NSUserDefaults.
 * Reading never throws: a missing or unreadable value simply comes back as null.
 */
internal interface PreferenceStore {
    fun getString(key: String): String?
    fun putString(key: String, value: String)
}

/** Keeps values in memory only. Used by the unit tests. */
internal class InMemoryPreferenceStore(initial: Map<String, String> = emptyMap()) : PreferenceStore {
    private val values = initial.toMutableMap()
    override fun getString(key: String): String? = values[key]
    override fun putString(key: String, value: String) {
        values[key] = value
    }
}
