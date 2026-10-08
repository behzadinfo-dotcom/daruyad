package com.example.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

fun getPastelColorScheme(paletteKey: String) = when (paletteKey) {
    "lavender" -> lightColorScheme(
        primary = PastelLavenderPrimary,
        primaryContainer = PastelLavenderContainer,
        onPrimaryContainer = PastelLavenderOnContainer,
        background = PastelLavenderBackground,
        surface = SoftSurface,
        surfaceVariant = SoftSurfaceVariant,
        outline = SoftOutline,
        onPrimary = androidx.compose.ui.graphics.Color.White,
        onBackground = TextPrimary,
        onSurface = TextPrimary
    )
    "peach" -> lightColorScheme(
        primary = PastelPeachPrimary,
        primaryContainer = PastelPeachContainer,
        onPrimaryContainer = PastelPeachOnContainer,
        background = PastelPeachBackground,
        surface = SoftSurface,
        surfaceVariant = SoftSurfaceVariant,
        outline = SoftOutline,
        onPrimary = androidx.compose.ui.graphics.Color.White,
        onBackground = TextPrimary,
        onSurface = TextPrimary
    )
    "rose" -> lightColorScheme(
        primary = PastelRosePrimary,
        primaryContainer = PastelRoseContainer,
        onPrimaryContainer = PastelRoseOnContainer,
        background = PastelRoseBackground,
        surface = SoftSurface,
        surfaceVariant = SoftSurfaceVariant,
        outline = SoftOutline,
        onPrimary = androidx.compose.ui.graphics.Color.White,
        onBackground = TextPrimary,
        onSurface = TextPrimary
    )
    "sky" -> lightColorScheme(
        primary = PastelSkyPrimary,
        primaryContainer = PastelSkyContainer,
        onPrimaryContainer = PastelSkyOnContainer,
        background = PastelSkyBackground,
        surface = SoftSurface,
        surfaceVariant = SoftSurfaceVariant,
        outline = SoftOutline,
        onPrimary = androidx.compose.ui.graphics.Color.White,
        onBackground = TextPrimary,
        onSurface = TextPrimary
    )
    else -> lightColorScheme(
        primary = PastelMintPrimary,
        primaryContainer = PastelMintContainer,
        onPrimaryContainer = PastelMintOnContainer,
        background = PastelMintBackground,
        surface = SoftSurface,
        surfaceVariant = SoftSurfaceVariant,
        outline = SoftOutline,
        onPrimary = androidx.compose.ui.graphics.Color.White,
        onBackground = TextPrimary,
        onSurface = TextPrimary
    )
}

@Composable
fun MedRemindTheme(
    paletteKey: String = "mint",
    fontScale: Float = 1.0f,
    fontWeightLevel: Int = 400,
    content: @Composable () -> Unit
) {
    val colorScheme = getPastelColorScheme(paletteKey)
    val typography = createAppTypography(fontScale, fontWeightLevel)

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window
            if (window != null) {
                window.statusBarColor = colorScheme.background.toArgb()
                window.navigationBarColor = colorScheme.background.toArgb()
                val controller = WindowCompat.getInsetsController(window, view)
                controller.isAppearanceLightStatusBars = true
                controller.isAppearanceLightNavigationBars = true
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = typography,
        content = content
    )
}

@Composable
fun MyApplicationTheme(content: @Composable () -> Unit) {
    MedRemindTheme(content = content)
}
