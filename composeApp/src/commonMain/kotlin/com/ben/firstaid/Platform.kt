package com.ben.firstaid

import androidx.compose.runtime.Composable

/** True on devices that can open a phone dialer (Android, iOS). False on desktop. */
internal expect val canPlaceCalls: Boolean

/** Handles the system Back button/gesture where the platform has one (Android). */
@Composable
internal expect fun PlatformBackHandler(enabled: Boolean, onBack: () -> Unit)

/** Small on-device storage for user settings such as the theme (see Preferences.kt). */
@Composable
internal expect fun rememberPreferenceStore(): PreferenceStore

/**
 * Keeps the system status bar and navigation bar icons readable in the current theme.
 * Only Android has such bars to restyle; desktop and iOS do nothing.
 */
@Composable
internal expect fun SystemBarsEffect(darkTheme: Boolean)
