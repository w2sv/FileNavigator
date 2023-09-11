package com.w2sv.filenavigator.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

object AppColor {
    val primary = Color(0xFF00696E)

    val success = Color(12, 173, 34, 200)
    val error = Color(201, 14, 52, 200)

    val disabled: Color
        @Composable get() = MaterialTheme.colorScheme.onSurface.copy(0.38f)

    object ThemeLight {
        val onPrimary = Color(0xFFFFFFFF)
        val primaryContainer = Color(0xFF6FF6FE)
        val onPrimaryContainer = Color(0xFF002021)
        val secondary = Color(0xFF006B5C)
        val onSecondary = Color(0xFFFFFFFF)
        val secondaryContainer = Color(0xFF09FEDD)
        val onSecondaryContainer = Color(0xFF00201B)
        val tertiary = Color(0xFF8C4381)
        val onTertiary = Color(0xFFFFFFFF)
        val tertiaryContainer = Color(0xFFFFD7F2)
        val onTertiaryContainer = Color(0xFF390035)
        val error = Color(0xFFBA1A1A)
        val errorContainer = Color(0xFFFFDAD6)
        val onError = Color(0xFFFFFFFF)
        val onErrorContainer = Color(0xFF410002)
        val background = Color(0xFFFAFDFC)
        val onBackground = Color(0xFF191C1C)
        val surface = Color(0xFFFAFDFC)
        val onSurface = Color(0xFF191C1C)
        val surfaceVariant = Color(0xFFDAE4E5)
        val onSurfaceVariant = Color(0xFF3F4949)
        val outline = Color(0xFF6F7979)
        val inverseOnSurface = Color(0xFFEFF1F1)
        val inverseSurface = Color(0xFF2D3131)
        val inversePrimary = Color(0xFF4CD9E1)
        val surfaceTint = Color(0xFF00696E)
        val outlineVariant = Color(0xFFBEC8C9)
        val scrim = Color(0xFF000000)
    }

    object ThemeDark {
        val onPrimary = Color(0xFF003739)
        val primaryContainer = Color(0xFF004F53)
        val onPrimaryContainer = Color(0xFF6FF6FE)
        val secondary = Color(0xFF00DFC2)
        val onSecondary = Color(0xFF00382F)
        val secondaryContainer = Color(0xFF005045)
        val onSecondaryContainer = Color(0xFF09FEDD)
        val tertiary = Color(0xFFFFACEC)
        val onTertiary = Color(0xFF56124F)
        val tertiaryContainer = Color(0xFF712B67)
        val onTertiaryContainer = Color(0xFFFFD7F2)
        val error = Color(0xFFFFB4AB)
        val errorContainer = Color(0xFF93000A)
        val onError = Color(0xFF690005)
        val onErrorContainer = Color(0xFFFFDAD6)
        val background = Color(0xFF191C1C)
        val onBackground = Color(0xFFE0E3E3)
        val surface = Color(0xFF191C1C)
        val onSurface = Color(0xFFE0E3E3)
        val surfaceVariant = Color(0xFF3F4949)
        val onSurfaceVariant = Color(0xFFBEC8C9)
        val outline = Color(0xFF899393)
        val inverseOnSurface = Color(0xFF191C1C)
        val inverseSurface = Color(0xFFE0E3E3)
        val inversePrimary = Color(0xFF00696E)
        val surfaceTint = Color(0xFF4CD9E1)
        val outlineVariant = Color(0xFF3F4949)
        val scrim = Color(0xFF000000)
    }
}