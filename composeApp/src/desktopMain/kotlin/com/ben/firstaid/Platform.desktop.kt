package com.ben.firstaid

import androidx.compose.runtime.Composable

internal actual val canPlaceCalls: Boolean = false

@Composable
internal actual fun PlatformBackHandler(enabled: Boolean, onBack: () -> Unit) {
    // Desktop has no system Back button. The in-app Back button in the top bar is used instead.
}
