package com.w2sv.filenavigator.ui.components

import android.graphics.Typeface
import android.text.Spanned
import android.text.style.StyleSpan
import androidx.annotation.StringRes
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.TextUnit
import androidx.core.text.HtmlCompat
import com.w2sv.filenavigator.R

@Composable
fun AppFontText(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = Color.Unspecified,
    fontSize: TextUnit = TextUnit.Unspecified,
    fontStyle: FontStyle? = null,
    fontWeight: FontWeight? = null,
    letterSpacing: TextUnit = TextUnit.Unspecified,
    textDecoration: TextDecoration? = null,
    textAlign: TextAlign? = null,
    lineHeight: TextUnit = TextUnit.Unspecified,
    overflow: TextOverflow = TextOverflow.Clip,
    softWrap: Boolean = true,
    maxLines: Int = Int.MAX_VALUE,
    minLines: Int = 1,
    onTextLayout: (TextLayoutResult) -> Unit = {},
    style: TextStyle = LocalTextStyle.current
) {
    Text(
        text = text,
        modifier = modifier,
        color = color,
        fontSize = fontSize,
        fontStyle = fontStyle,
        fontWeight = fontWeight,
        fontFamily = Railway,
        letterSpacing = letterSpacing,
        textDecoration = textDecoration,
        textAlign = textAlign,
        lineHeight = lineHeight,
        overflow = overflow,
        softWrap = softWrap,
        maxLines = maxLines,
        minLines = minLines,
        onTextLayout = onTextLayout,
        style = style
    )
}

@Composable
fun AppFontText(
    text: AnnotatedString,
    modifier: Modifier = Modifier,
    color: Color = Color.Unspecified,
    fontSize: TextUnit = TextUnit.Unspecified,
    fontStyle: FontStyle? = null,
    fontWeight: FontWeight? = null,
    letterSpacing: TextUnit = TextUnit.Unspecified,
    textDecoration: TextDecoration? = null,
    textAlign: TextAlign? = null,
    lineHeight: TextUnit = TextUnit.Unspecified,
    overflow: TextOverflow = TextOverflow.Clip,
    softWrap: Boolean = true,
    maxLines: Int = Int.MAX_VALUE,
    minLines: Int = 1,
    onTextLayout: (TextLayoutResult) -> Unit = {},
    style: TextStyle = LocalTextStyle.current
) {
    Text(
        text = text,
        modifier = modifier,
        color = color,
        fontSize = fontSize,
        fontStyle = fontStyle,
        fontWeight = fontWeight,
        fontFamily = Railway,
        letterSpacing = letterSpacing,
        textDecoration = textDecoration,
        textAlign = textAlign,
        lineHeight = lineHeight,
        overflow = overflow,
        softWrap = softWrap,
        maxLines = maxLines,
        minLines = minLines,
        onTextLayout = onTextLayout,
        style = style
    )
}

private val Railway = FontFamily(
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

/**
 * From https://stackoverflow.com/a/77319763/12083276
 */
@Composable
@ReadOnlyComposable
fun styledTextResource(@StringRes id: Int): AnnotatedString =
    HtmlCompat.fromHtml(
        stringResource(id = id),
        HtmlCompat.FROM_HTML_MODE_COMPACT
    )
        .toAnnotatedString()

private fun Spanned.toAnnotatedString(): AnnotatedString =
    buildAnnotatedString {
        val spanned = this@toAnnotatedString
        append(spanned.toString())

        getSpans(
            0,
            spanned.length,
            Any::class.java
        )
            .forEach { span ->
                val start = getSpanStart(span)
                val end = getSpanEnd(span)

                when (span) {
                    is StyleSpan ->
                        when (span.style) {
                            Typeface.BOLD -> addStyle(
                                SpanStyle(fontWeight = FontWeight.Bold),
                                start,
                                end
                            )

                            Typeface.ITALIC -> addStyle(
                                SpanStyle(fontStyle = FontStyle.Italic),
                                start,
                                end
                            )

                            Typeface.BOLD_ITALIC -> addStyle(
                                SpanStyle(
                                    fontWeight = FontWeight.Bold,
                                    fontStyle = FontStyle.Italic
                                ),
                                start,
                                end
                            )
                        }
                }
            }
    }