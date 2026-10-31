package com.ben.firstaid

import android.content.Context
import android.content.ContextWrapper
import android.content.SharedPreferences
import android.graphics.Color
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.BackHandler
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext

internal actual val canPlaceCalls: Boolean = true

@Composable
internal actual fun PlatformBackHandler(enabled: Boolean, onBack: () -> Unit) {
    BackHandler(enabled = enabled, onBack = onBack)
}

private class SharedPreferencesStore(private val prefs: SharedPreferences) : PreferenceStore {
    override fun getString(key: String): String? = prefs.getString(key, null)
    override fun putString(key: String, value: String) {
        prefs.edit().putString(key, value).apply()
    }
}

@Composable
internal actual fun rememberPreferenceStore(): PreferenceStore {
    val appContext = LocalContext.current.applicationContext
    return remember(appContext) {
        SharedPreferencesStore(appContext.getSharedPreferences("first_aid_prefs", Context.MODE_PRIVATE))
    }
}

private tailrec fun Context.findComponentActivity(): ComponentActivity? = when (this) {
    is ComponentActivity -> this
    is ContextWrapper -> baseContext.findComponentActivity()
    else -> null
}

/**
 * The top bar is dark teal in both themes, so status bar icons are always light.
 * The bottom bar is white in the light theme (dark icons) and dark in the dark theme (light icons).
 */
@Composable
internal actual fun SystemBarsEffect(darkTheme: Boolean) {
    val activity = LocalContext.current.findComponentActivity() ?: return
    LaunchedEffect(activity, darkTheme) {
        activity.enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(Color.TRANSPARENT),
            navigationBarStyle = if (darkTheme) {
                SystemBarStyle.dark(Color.TRANSPARENT)
            } else {
                SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT)
            },
        )
    }
}
