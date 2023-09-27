package com.w2sv.filenavigator.ui.utils.extensions

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.w2sv.filenavigator.ui.theme.AppColor

@Composable
fun Color.orDisabledIf(condition: Boolean): Color =
    if (condition) AppColor.disabled else this