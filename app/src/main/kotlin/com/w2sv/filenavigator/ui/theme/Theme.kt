package com.w2sv.filenavigator.ui.theme

import android.annotation.SuppressLint
import android.content.Context
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import com.materialkolor.PaletteStyle
import com.materialkolor.rememberDynamicColorScheme
import com.w2sv.filenavigator.ui.util.LocalUseDarkTheme

private val seedColor = Color(color = 0xFF00696E)

@SuppressLint("NewApi")
@Composable
fun AppTheme(
    useDarkTheme: Boolean = LocalUseDarkTheme.current,
    useAmoledBlackTheme: Boolean = false,
    useDynamicColors: Boolean = false,
    context: Context = LocalContext.current,
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = when {
            useDynamicColors && useDarkTheme -> dynamicDarkColorScheme(context)
            useDynamicColors && !useDarkTheme -> dynamicLightColorScheme(context)
            else -> rememberDynamicColorScheme(
                seedColor = seedColor,
                isDark = useDarkTheme,
                style = PaletteStyle.Rainbow,  // Vibrant, TonalSpot, Fidelity, Rainbow
            )
        }
            .run {
                if (useAmoledBlackTheme && useDarkTheme) {
                    copy(background = Color.Black, surface = Color.Black)
                } else {
                    this
                }
            }
            .animate(animationSpec = remember { spring(stiffness = Spring.StiffnessMedium) }),
        typography = typography
    ) {
        content()
    }
}

@Composable
private fun ColorScheme.animate(animationSpec: AnimationSpec<Color>): ColorScheme {
    return copy(
        primary = primary.animate(animationSpec),
        primaryContainer = primaryContainer.animate(animationSpec),
        secondary = secondary.animate(animationSpec),
        secondaryContainer = secondaryContainer.animate(animationSpec),
        tertiary = tertiary.animate(animationSpec),
        tertiaryContainer = tertiaryContainer.animate(animationSpec),
        background = background.animate(animationSpec),
        surface = surface.animate(animationSpec),
        surfaceTint = surfaceTint.animate(animationSpec),
        surfaceBright = surfaceBright.animate(animationSpec),
        surfaceDim = surfaceDim.animate(animationSpec),
        surfaceContainer = surfaceContainer.animate(animationSpec),
        surfaceContainerHigh = surfaceContainerHigh.animate(animationSpec),
        surfaceContainerHighest = surfaceContainerHighest.animate(animationSpec),
        surfaceContainerLow = surfaceContainerLow.animate(animationSpec),
        surfaceContainerLowest = surfaceContainerLowest.animate(animationSpec),
        surfaceVariant = surfaceVariant.animate(animationSpec),
        error = error.animate(animationSpec),
        errorContainer = errorContainer.animate(animationSpec),
        onPrimary = onPrimary.animate(animationSpec),
        onPrimaryContainer = onPrimaryContainer.animate(animationSpec),
        onSecondary = onSecondary.animate(animationSpec),
        onSecondaryContainer = onSecondaryContainer.animate(animationSpec),
        onTertiary = onTertiary.animate(animationSpec),
        onTertiaryContainer = onTertiaryContainer.animate(animationSpec),
        onBackground = onBackground.animate(animationSpec),
        onSurface = onSurface.animate(animationSpec),
        onSurfaceVariant = onSurfaceVariant.animate(animationSpec),
        onError = onError.animate(animationSpec),
        onErrorContainer = onErrorContainer.animate(animationSpec),
        inversePrimary = inversePrimary.animate(animationSpec),
        inverseSurface = inverseSurface.animate(animationSpec),
        inverseOnSurface = inverseOnSurface.animate(animationSpec),
        outline = outline.animate(animationSpec),
        outlineVariant = outlineVariant.animate(animationSpec),
        scrim = scrim.animate(animationSpec),
    )
}

@Composable
private fun Color.animate(animationSpec: AnimationSpec<Color>): Color {
    return animateColorAsState(this, animationSpec, label = "").value
}
