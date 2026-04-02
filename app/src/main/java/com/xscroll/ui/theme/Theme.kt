package com.xscroll.ui.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    primary = Color.White,
    onPrimary = Color.Black,
    background = Color.Black,
    onBackground = Color.White,
    surface = Color.Black,
    onSurface = Color.White,
    error = Color(0xFFFF6B6B),
)

@Immutable
data class XScrollColors(
    val gold: Color = Color(0xFFFFD700),
    val countdown: Color = Color(0xFFFF6B6B),
    val overlayLight: Color = Color.White.copy(alpha = 0.15f),
    val overlayDark: Color = Color.Black.copy(alpha = 0.5f),
    val textDimmed: Color = Color.White.copy(alpha = 0.7f),
    val textFaint: Color = Color.White.copy(alpha = 0.4f),
)

val LocalXScrollColors = staticCompositionLocalOf { XScrollColors() }

@Composable
fun XScrollTheme(content: @Composable () -> Unit) {
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = Color.Black.toArgb()
            window.navigationBarColor = Color.Black.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
        }
    }

    CompositionLocalProvider(LocalXScrollColors provides XScrollColors()) {
        MaterialTheme(
            colorScheme = DarkColorScheme,
            content = content,
        )
    }
}
