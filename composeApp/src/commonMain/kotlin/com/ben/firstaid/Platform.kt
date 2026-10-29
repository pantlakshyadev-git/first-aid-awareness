package com.ben.firstaid

import androidx.compose.runtime.Composable

/** True on devices that can open a phone dialer (Android, iOS). False on desktop. */
internal expect val canPlaceCalls: Boolean

/** Handles the system Back button/gesture where the platform has one (Android). */
@Composable
internal expect fun PlatformBackHandler(enabled: Boolean, onBack: () -> Unit)
