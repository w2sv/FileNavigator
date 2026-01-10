package com.w2sv.designsystem.theme

import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.w2sv.core.designsystem.R

private val defaultTypography = Typography()

private val railway = FontFamily(
    Font(R.font.raleway_black, FontWeight.Black, FontStyle.Normal),
    Font(R.font.raleway_light, FontWeight.Light, FontStyle.Normal),
    Font(R.font.raleway_bold, FontWeight.Bold, FontStyle.Normal),
    Font(R.font.raleway_medium, FontWeight.Medium, FontStyle.Normal),
    Font(R.font.raleway_regular, FontWeight.Normal, FontStyle.Normal),
    Font(R.font.raleway_extrabold, FontWeight.ExtraBold, FontStyle.Normal),
    Font(R.font.raleway_semibold, FontWeight.SemiBold, FontStyle.Normal),
    Font(R.font.raleway_extralight, FontWeight.ExtraLight, FontStyle.Normal),
    Font(R.font.raleway_thin, FontWeight.Thin, FontStyle.Normal)
)

internal val typography = Typography(
    displayLarge = defaultTypography.displayLarge.copy(fontFamily = railway),
    displayMedium = defaultTypography.displayMedium.copy(fontFamily = railway),
    displaySmall = defaultTypography.displaySmall.copy(fontFamily = railway),

    headlineLarge = defaultTypography.headlineLarge.copy(fontFamily = railway),
    headlineMedium = TextStyle(
        fontSize = 20.sp,
        fontWeight = FontWeight.SemiBold,
        fontFamily = railway
    ),
    headlineSmall = defaultTypography.headlineSmall.copy(fontFamily = railway),

    titleLarge = defaultTypography.titleLarge.copy(fontFamily = railway),
    titleMedium = defaultTypography.titleMedium.copy(fontFamily = railway),
    titleSmall = defaultTypography.titleSmall.copy(fontFamily = railway),

    bodyLarge = defaultTypography.bodyLarge.copy(fontFamily = railway),
    bodyMedium = defaultTypography.bodyMedium.copy(fontFamily = railway),
    bodySmall = defaultTypography.bodySmall.copy(fontFamily = railway),

    labelLarge = defaultTypography.labelLarge.copy(fontFamily = railway),
    labelMedium = defaultTypography.labelMedium.copy(fontFamily = railway),
    labelSmall = defaultTypography.labelSmall.copy(fontFamily = railway)
)

val Typography.dialogSectionLabel
    @Composable
    @ReadOnlyComposable
    get() = bodyLarge.copy(fontSize = 18.sp)
