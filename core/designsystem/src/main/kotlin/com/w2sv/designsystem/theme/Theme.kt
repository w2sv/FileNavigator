package com.w2sv.designsystem.theme

import android.annotation.SuppressLint
import android.content.Context
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import com.w2sv.androidutils.os.dynamicColorsSupported
import com.w2sv.kotlinutils.threadUnsafeLazy

@SuppressLint("NewApi")
@Composable
fun AppTheme(
    useDarkTheme: Boolean = isSystemInDarkTheme(),
    useAmoledBlackTheme: Boolean = false,
    useDynamicColors: Boolean = dynamicColorsSupported,
    context: Context = LocalContext.current,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        useDynamicColors && useDarkTheme && useAmoledBlackTheme -> dynamicDarkColorScheme(context).amoledBlack()
        useDynamicColors && useDarkTheme -> dynamicDarkColorScheme(context)
        useDynamicColors && !useDarkTheme -> dynamicLightColorScheme(context)
        useDarkTheme && useAmoledBlackTheme -> staticColorSchemeDark.amoledBlack()
        useDarkTheme -> staticColorSchemeDark
        else -> staticColorSchemeLight
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = typography,
        content = content
    )
}

private fun ColorScheme.amoledBlack(): ColorScheme =
    copy(background = Color.Black, surface = Color.Black, onBackground = Color.White, onSurface = Color.White)

private val staticColorSchemeDark by threadUnsafeLazy {
    ColorScheme(
        primary = Color(0.29803923f, 0.8509804f, 0.88235295f, 1.0f),
        onPrimary = Color(0.0f, 0.21568628f, 0.22352941f, 1.0f),
        primaryContainer = Color(0.0f, 0.30980393f, 0.3254902f, 1.0f),
        onPrimaryContainer = Color(0.43529412f, 0.9647059f, 0.99607843f, 1.0f),
        inversePrimary = Color(0.0f, 0.4117647f, 0.43137255f, 1.0f),

        secondary = Color(0.69411767f, 0.8f, 0.8039216f, 1.0f),
        onSecondary = Color(0.105882354f, 0.20392157f, 0.21176471f, 1.0f),
        secondaryContainer = Color(0.19607843f, 0.29411766f, 0.3019608f, 1.0f),
        onSecondaryContainer = Color(0.8f, 0.9098039f, 0.9137255f, 1.0f),

        tertiary = Color(0.7137255f, 0.78039217f, 0.9137255f, 1.0f),
        onTertiary = Color(0.1254902f, 0.19215687f, 0.29803923f, 1.0f),
        tertiaryContainer = Color(0.21176471f, 0.2784314f, 0.39215687f, 1.0f),
        onTertiaryContainer = Color(0.8392157f, 0.8901961f, 1.0f, 1.0f),

        background = Color(0.07450981f, 0.07450981f, 0.07450981f, 1.0f),
        onBackground = Color(0.8862745f, 0.8862745f, 0.8862745f, 1.0f),

        surface = Color(0.07450981f, 0.07450981f, 0.07450981f, 1.0f),
        onSurface = Color(0.8862745f, 0.8862745f, 0.8862745f, 1.0f),
        surfaceVariant = Color(0.2784314f, 0.2784314f, 0.2784314f, 1.0f),
        onSurfaceVariant = Color(0.7764706f, 0.7764706f, 0.7764706f, 1.0f),

        surfaceTint = Color(0.29803923f, 0.8509804f, 0.88235295f, 1.0f),
        inverseSurface = Color(0.8862745f, 0.8862745f, 0.8862745f, 1.0f),
        inverseOnSurface = Color(0.1882353f, 0.1882353f, 0.1882353f, 1.0f),

        error = Color(1.0f, 0.7058824f, 0.67058825f, 1.0f),
        onError = Color(0.4117647f, 0.0f, 0.019607844f, 1.0f),
        errorContainer = Color(0.5764706f, 0.0f, 0.039215688f, 1.0f),
        onErrorContainer = Color(1.0f, 0.85490197f, 0.8392157f, 1.0f),

        outline = Color(0.5686275f, 0.5686275f, 0.5686275f, 1.0f),
        outlineVariant = Color(0.2784314f, 0.2784314f, 0.2784314f, 1.0f),
        scrim = Color(0.0f, 0.0f, 0.0f, 1.0f),

        surfaceBright = Color(0.22352941f, 0.22352941f, 0.22352941f, 1.0f),
        surfaceDim = Color(0.07450981f, 0.07450981f, 0.07450981f, 1.0f),
        surfaceContainer = Color(0.12156863f, 0.12156863f, 0.12156863f, 1.0f),
        surfaceContainerHigh = Color(0.16470589f, 0.16470589f, 0.16470589f, 1.0f),
        surfaceContainerHighest = Color(0.20784314f, 0.20784314f, 0.20784314f, 1.0f),
        surfaceContainerLow = Color(0.105882354f, 0.105882354f, 0.105882354f, 1.0f),
        surfaceContainerLowest = Color(0.05490196f, 0.05490196f, 0.05490196f, 1.0f)
    )
}

private val staticColorSchemeLight by threadUnsafeLazy {
    ColorScheme(
        primary = Color(0.0f, 0.4117647f, 0.43137255f, 1.0f),
        onPrimary = Color(1.0f, 1.0f, 1.0f, 1.0f),
        primaryContainer = Color(0.43529412f, 0.9647059f, 0.99607843f, 1.0f),
        onPrimaryContainer = Color(0.0f, 0.30980393f, 0.3254902f, 1.0f),
        inversePrimary = Color(0.29803923f, 0.8509804f, 0.88235295f, 1.0f),
        secondary = Color(0.2901961f, 0.3882353f, 0.39215687f, 1.0f),
        onSecondary = Color(1.0f, 1.0f, 1.0f, 1.0f),
        secondaryContainer = Color(0.8f, 0.9098039f, 0.9137255f, 1.0f),
        onSecondaryContainer = Color(0.19607843f, 0.29411766f, 0.3019608f, 1.0f),
        tertiary = Color(0.30588236f, 0.37254903f, 0.49019608f, 1.0f),
        onTertiary = Color(1.0f, 1.0f, 1.0f, 1.0f),
        tertiaryContainer = Color(0.8392157f, 0.8901961f, 1.0f, 1.0f),
        onTertiaryContainer = Color(0.21176471f, 0.2784314f, 0.39215687f, 1.0f),
        background = Color(0.9764706f, 0.9764706f, 0.9764706f, 1.0f),
        onBackground = Color(0.105882354f, 0.105882354f, 0.105882354f, 1.0f),
        surface = Color(0.9764706f, 0.9764706f, 0.9764706f, 1.0f),
        onSurface = Color(0.105882354f, 0.105882354f, 0.105882354f, 1.0f),
        surfaceVariant = Color(0.8862745f, 0.8862745f, 0.8862745f, 1.0f),
        onSurfaceVariant = Color(0.2784314f, 0.2784314f, 0.2784314f, 1.0f),
        surfaceTint = Color(0.0f, 0.4117647f, 0.43137255f, 1.0f),
        inverseSurface = Color(0.1882353f, 0.1882353f, 0.1882353f, 1.0f),
        inverseOnSurface = Color(0.94509804f, 0.94509804f, 0.94509804f, 1.0f),
        error = Color(0.7294118f, 0.101960786f, 0.101960786f, 1.0f),
        onError = Color(1.0f, 1.0f, 1.0f, 1.0f),
        errorContainer = Color(1.0f, 0.85490197f, 0.8392157f, 1.0f),
        onErrorContainer = Color(0.25490198f, 0.0f, 0.007843138f, 1.0f),
        outline = Color(0.46666667f, 0.46666667f, 0.46666667f, 1.0f),
        outlineVariant = Color(0.7764706f, 0.7764706f, 0.7764706f, 1.0f),
        scrim = Color(0.0f, 0.0f, 0.0f, 1.0f),
        surfaceBright = Color(0.9764706f, 0.9764706f, 0.9764706f, 1.0f),
        surfaceDim = Color(0.85490197f, 0.85490197f, 0.85490197f, 1.0f),
        surfaceContainer = Color(0.93333334f, 0.93333334f, 0.93333334f, 1.0f),
        surfaceContainerHigh = Color(0.9098039f, 0.9098039f, 0.9098039f, 1.0f),
        surfaceContainerHighest = Color(0.8862745f, 0.8862745f, 0.8862745f, 1.0f),
        surfaceContainerLow = Color(0.9529412f, 0.9529412f, 0.9529412f, 1.0f),
        surfaceContainerLowest = Color(1.0f, 1.0f, 1.0f, 1.0f)
    )
}
