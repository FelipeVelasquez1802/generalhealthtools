package org.infinite.solution.generalhealthtools

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application

fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        title = "GeneralHealthTools",
    ) {
        App()
    }
}