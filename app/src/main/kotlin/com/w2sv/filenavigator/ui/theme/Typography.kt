package com.w2sv.filenavigator.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.w2sv.filenavigator.R

private val defaultTypography = Typography()

private val railway = FontFamily(
    Font(R.font.raleway_blackitalic, FontWeight.Black, FontStyle.Italic),
    Font(R.font.raleway_lightitalic, FontWeight.Light, FontStyle.Italic),
    Font(R.font.raleway_black, FontWeight.Black, FontStyle.Normal),
    Font(R.font.raleway_light, FontWeight.Light, FontStyle.Normal),
    Font(R.font.raleway_bolditalic, FontWeight.Bold, FontStyle.Italic),
    Font(R.font.raleway_mediumitalic, FontWeight.Medium, FontStyle.Italic),
    Font(R.font.raleway_bold, FontWeight.Bold, FontStyle.Normal),
    Font(R.font.raleway_medium, FontWeight.Medium, FontStyle.Normal),
    Font(R.font.raleway_extrabolditalic, FontWeight.ExtraBold, FontStyle.Italic),
    Font(R.font.raleway_regular, FontWeight.Normal, FontStyle.Normal),
    Font(R.font.raleway_extrabold, FontWeight.ExtraBold, FontStyle.Normal),
    Font(R.font.raleway_semibolditalic, FontWeight.SemiBold, FontStyle.Italic),
    Font(R.font.raleway_extralightitalic, FontWeight.ExtraLight, FontStyle.Italic),
    Font(R.font.raleway_semibold, FontWeight.SemiBold, FontStyle.Normal),
    Font(R.font.raleway_extralight, FontWeight.ExtraLight, FontStyle.Normal),
    Font(R.font.raleway_thinitalic, FontWeight.Thin, FontStyle.Italic),
    Font(R.font.raleway_italic, FontWeight.Normal, FontStyle.Italic),
    Font(R.font.raleway_thin, FontWeight.Thin, FontStyle.Normal)
)

val typography = Typography(
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
