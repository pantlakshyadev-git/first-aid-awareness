package com.ben.firstaid

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import platform.Foundation.NSUserDefaults

internal actual val canPlaceCalls: Boolean = true

@Composable
internal actual fun PlatformBackHandler(enabled: Boolean, onBack: () -> Unit) {
    // iOS has no system Back button. The in-app Back button in the top bar is used instead.
}

private class UserDefaultsStore : PreferenceStore {
    private val defaults = NSUserDefaults.standardUserDefaults

    override fun getString(key: String): String? = defaults.stringForKey(key)

    override fun putString(key: String, value: String) {
        defaults.setObject(value, forKey = key)
    }
}

@Composable
internal actual fun rememberPreferenceStore(): PreferenceStore = remember { UserDefaultsStore() }

@Composable
internal actual fun SystemBarsEffect(darkTheme: Boolean) {
    // The iOS status bar follows the system appearance automatically.
}
