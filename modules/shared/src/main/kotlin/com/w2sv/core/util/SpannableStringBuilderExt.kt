package com.w2sv.core.util

import android.text.SpannableStringBuilder

fun SpannableStringBuilder.lineBreak(): SpannableStringBuilder =
    append("\n")
