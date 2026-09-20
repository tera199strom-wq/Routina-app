package com.example.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalConfiguration

@Composable
fun rememberUiScale(): Float {
    val widthDp = LocalConfiguration.current.screenWidthDp
    return when {
        widthDp < 360 -> 0.85f
        widthDp < 400 -> 0.92f
        else -> 1f
    }
}
