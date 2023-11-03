package com.w2sv.filenavigator.ui.theme

import android.annotation.SuppressLint
import android.content.Context
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

@SuppressLint("NewApi")
@Composable
fun AppTheme(
    useDynamicTheme: Boolean = false,
    useDarkTheme: Boolean = false,
    context: Context = LocalContext.current,
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = when {
            useDynamicTheme && useDarkTheme -> dynamicDarkColorScheme(context)
            useDynamicTheme && !useDarkTheme -> dynamicLightColorScheme(context)
            !useDynamicTheme && useDarkTheme -> darkColors
            else -> lightColors
        },
        typography = typography
    ) {
        content()
    }
}

private val typography = Typography(
    headlineMedium = TextStyle(
        fontSize = 20.sp,
        fontWeight = FontWeight.SemiBold
    )
)

private val lightColors = lightColorScheme(
    primary = AppColor.primary,
    onPrimary = AppColor.ThemeLight.onPrimary,
    primaryContainer = AppColor.ThemeLight.primaryContainer,
    onPrimaryContainer = AppColor.ThemeLight.onPrimaryContainer,
    secondary = AppColor.ThemeLight.secondary,
    onSecondary = AppColor.ThemeLight.onSecondary,
    secondaryContainer = AppColor.ThemeLight.secondaryContainer,
    onSecondaryContainer = AppColor.ThemeLight.onSecondaryContainer,
    tertiary = AppColor.ThemeLight.tertiary,
    onTertiary = AppColor.ThemeLight.onTertiary,
    tertiaryContainer = AppColor.ThemeLight.tertiaryContainer,
    onTertiaryContainer = AppColor.ThemeLight.onTertiaryContainer,
    error = AppColor.ThemeLight.error,
    errorContainer = AppColor.ThemeLight.errorContainer,
    onError = AppColor.ThemeLight.onError,
    onErrorContainer = AppColor.ThemeLight.onErrorContainer,
    background = AppColor.ThemeLight.background,
    onBackground = AppColor.ThemeLight.onBackground,
    surface = AppColor.ThemeLight.surface,
    onSurface = AppColor.ThemeLight.onSurface,
    surfaceVariant = AppColor.ThemeLight.surfaceVariant,
    onSurfaceVariant = AppColor.ThemeLight.onSurfaceVariant,
    outline = AppColor.ThemeLight.outline,
    inverseOnSurface = AppColor.ThemeLight.inverseOnSurface,
    inverseSurface = AppColor.ThemeLight.inverseSurface,
    inversePrimary = AppColor.inversePrimary,
    surfaceTint = AppColor.ThemeLight.surfaceTint,
    outlineVariant = AppColor.ThemeLight.outlineVariant,
    scrim = AppColor.ThemeLight.scrim,
)

private val darkColors = darkColorScheme(
    primary = AppColor.primary,
    onPrimary = AppColor.ThemeDark.onPrimary,
    primaryContainer = AppColor.ThemeDark.primaryContainer,
    onPrimaryContainer = AppColor.ThemeDark.onPrimaryContainer,
    secondary = AppColor.ThemeDark.secondary,
    onSecondary = AppColor.ThemeDark.onSecondary,
    secondaryContainer = AppColor.ThemeDark.secondaryContainer,
    onSecondaryContainer = AppColor.ThemeDark.onSecondaryContainer,
    tertiary = AppColor.ThemeDark.tertiary,
    onTertiary = AppColor.ThemeDark.onTertiary,
    tertiaryContainer = AppColor.ThemeDark.tertiaryContainer,
    onTertiaryContainer = AppColor.ThemeDark.onTertiaryContainer,
    error = AppColor.ThemeDark.error,
    errorContainer = AppColor.ThemeDark.errorContainer,
    onError = AppColor.ThemeDark.onError,
    onErrorContainer = AppColor.ThemeDark.onErrorContainer,
    background = AppColor.ThemeDark.background,
    onBackground = AppColor.ThemeDark.onBackground,
    surface = AppColor.ThemeDark.surface,
    onSurface = AppColor.ThemeDark.onSurface,
    surfaceVariant = AppColor.ThemeDark.surfaceVariant,
    onSurfaceVariant = AppColor.ThemeDark.onSurfaceVariant,
    outline = AppColor.ThemeDark.outline,
    inverseOnSurface = AppColor.ThemeDark.inverseOnSurface,
    inverseSurface = AppColor.ThemeDark.inverseSurface,
    inversePrimary = AppColor.inversePrimary,
    surfaceTint = AppColor.ThemeDark.surfaceTint,
    outlineVariant = AppColor.ThemeDark.outlineVariant,
    scrim = AppColor.ThemeDark.scrim,
)
