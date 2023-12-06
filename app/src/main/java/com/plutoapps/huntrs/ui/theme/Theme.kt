package com.plutoapps.huntrs.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import com.plutoapps.huntrs.ui.theme.md_theme_dark_background
import com.plutoapps.huntrs.ui.theme.md_theme_dark_error
import com.plutoapps.huntrs.ui.theme.md_theme_dark_errorContainer
import com.plutoapps.huntrs.ui.theme.md_theme_dark_inverseOnSurface
import com.plutoapps.huntrs.ui.theme.md_theme_dark_inversePrimary
import com.plutoapps.huntrs.ui.theme.md_theme_dark_inverseSurface
import com.plutoapps.huntrs.ui.theme.md_theme_dark_onBackground
import com.plutoapps.huntrs.ui.theme.md_theme_dark_onError
import com.plutoapps.huntrs.ui.theme.md_theme_dark_onErrorContainer
import com.plutoapps.huntrs.ui.theme.md_theme_dark_onPrimary
import com.plutoapps.huntrs.ui.theme.md_theme_dark_onPrimaryContainer
import com.plutoapps.huntrs.ui.theme.md_theme_dark_onSecondary
import com.plutoapps.huntrs.ui.theme.md_theme_dark_onSecondaryContainer
import com.plutoapps.huntrs.ui.theme.md_theme_dark_onSurface
import com.plutoapps.huntrs.ui.theme.md_theme_dark_onSurfaceVariant
import com.plutoapps.huntrs.ui.theme.md_theme_dark_onTertiary
import com.plutoapps.huntrs.ui.theme.md_theme_dark_onTertiaryContainer
import com.plutoapps.huntrs.ui.theme.md_theme_dark_outline
import com.plutoapps.huntrs.ui.theme.md_theme_dark_outlineVariant
import com.plutoapps.huntrs.ui.theme.md_theme_dark_primary
import com.plutoapps.huntrs.ui.theme.md_theme_dark_primaryContainer
import com.plutoapps.huntrs.ui.theme.md_theme_dark_scrim
import com.plutoapps.huntrs.ui.theme.md_theme_dark_secondary
import com.plutoapps.huntrs.ui.theme.md_theme_dark_secondaryContainer
import com.plutoapps.huntrs.ui.theme.md_theme_dark_surface
import com.plutoapps.huntrs.ui.theme.md_theme_dark_surfaceTint
import com.plutoapps.huntrs.ui.theme.md_theme_dark_surfaceVariant
import com.plutoapps.huntrs.ui.theme.md_theme_dark_tertiary
import com.plutoapps.huntrs.ui.theme.md_theme_dark_tertiaryContainer
import com.plutoapps.huntrs.ui.theme.md_theme_light_background
import com.plutoapps.huntrs.ui.theme.md_theme_light_error
import com.plutoapps.huntrs.ui.theme.md_theme_light_errorContainer
import com.plutoapps.huntrs.ui.theme.md_theme_light_inverseOnSurface
import com.plutoapps.huntrs.ui.theme.md_theme_light_inversePrimary
import com.plutoapps.huntrs.ui.theme.md_theme_light_inverseSurface
import com.plutoapps.huntrs.ui.theme.md_theme_light_onBackground
import com.plutoapps.huntrs.ui.theme.md_theme_light_onError
import com.plutoapps.huntrs.ui.theme.md_theme_light_onErrorContainer
import com.plutoapps.huntrs.ui.theme.md_theme_light_onPrimary
import com.plutoapps.huntrs.ui.theme.md_theme_light_onPrimaryContainer
import com.plutoapps.huntrs.ui.theme.md_theme_light_onSecondary
import com.plutoapps.huntrs.ui.theme.md_theme_light_onSecondaryContainer
import com.plutoapps.huntrs.ui.theme.md_theme_light_onSurface
import com.plutoapps.huntrs.ui.theme.md_theme_light_onSurfaceVariant
import com.plutoapps.huntrs.ui.theme.md_theme_light_onTertiary
import com.plutoapps.huntrs.ui.theme.md_theme_light_onTertiaryContainer
import com.plutoapps.huntrs.ui.theme.md_theme_light_outline
import com.plutoapps.huntrs.ui.theme.md_theme_light_outlineVariant
import com.plutoapps.huntrs.ui.theme.md_theme_light_primary
import com.plutoapps.huntrs.ui.theme.md_theme_light_primaryContainer
import com.plutoapps.huntrs.ui.theme.md_theme_light_scrim
import com.plutoapps.huntrs.ui.theme.md_theme_light_secondary
import com.plutoapps.huntrs.ui.theme.md_theme_light_secondaryContainer
import com.plutoapps.huntrs.ui.theme.md_theme_light_surface
import com.plutoapps.huntrs.ui.theme.md_theme_light_surfaceTint
import com.plutoapps.huntrs.ui.theme.md_theme_light_surfaceVariant
import com.plutoapps.huntrs.ui.theme.md_theme_light_tertiary
import com.plutoapps.huntrs.ui.theme.md_theme_light_tertiaryContainer


@Composable
fun AppTheme(
    useDarkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colors = if (!useDarkTheme) {
        lightColors
    } else {
        darkColors
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colors.background.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars =
                !useDarkTheme
        }
    }

    MaterialTheme(
        colorScheme = colors,
        content = content
    )
}