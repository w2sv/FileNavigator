package com.w2sv.common.utils

import android.content.Context
import androidx.annotation.StringRes
import androidx.core.text.HtmlCompat

/**
 * Create a formatted CharSequence from a string resource containing arguments and HTML formatting
 *
 * The string resource must be wrapped in a CDATA section so that the HTML formatting is conserved.
 *
 * Example of an HTML formatted string resource:
 * <string name="html_formatted"><![CDATA[ bold text: <B>%1$s</B> ]]></string>
 *
 * Taken from https://stackoverflow.com/a/56944152/12083276
 */
fun Context.getText(@StringRes id: Int, vararg args: Any?): CharSequence =  // TODO: AndroidUtils
    HtmlCompat.fromHtml(String.format(getString(id), *args), HtmlCompat.FROM_HTML_MODE_COMPACT)