package com.ben.firstaid

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import java.util.prefs.Preferences

internal actual val canPlaceCalls: Boolean = false

@Composable
internal actual fun PlatformBackHandler(enabled: Boolean, onBack: () -> Unit) {
    // Desktop has no system Back button. The in-app Back button in the top bar is used instead.
}

/** Stored by the operating system for the current user (Windows registry, ~/.java on Linux, plist on macOS). */
private class JavaPreferenceStore : PreferenceStore {
    private val prefs: Preferences? = try {
        Preferences.userRoot().node("com/ben/firstaid")
    } catch (e: Exception) {
        null // storage not available: the app still works, the choice just is not remembered
    }

    override fun getString(key: String): String? = try {
        prefs?.get(key, null)
    } catch (e: Exception) {
        null
    }

    override fun putString(key: String, value: String) {
        try {
            prefs?.put(key, value)
            prefs?.flush()
        } catch (e: Exception) {
            // ignore: see above
        }
    }
}

@Composable
internal actual fun rememberPreferenceStore(): PreferenceStore = remember { JavaPreferenceStore() }

@Composable
internal actual fun SystemBarsEffect(darkTheme: Boolean) {
    // Desktop windows have no status or navigation bar to restyle.
}
