package com.ben.firstaid

import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import com.ben.firstaid.generated.resources.Res
import com.ben.firstaid.generated.resources.ic_launcher
import org.jetbrains.compose.resources.painterResource
import java.awt.Dimension

fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        title = "First Aid Awareness",
        icon = painterResource(Res.drawable.ic_launcher),
        state = rememberWindowState(size = DpSize(1100.dp, 720.dp)),
    ) {
        window.minimumSize = Dimension(420, 560)
        App()
    }
}
