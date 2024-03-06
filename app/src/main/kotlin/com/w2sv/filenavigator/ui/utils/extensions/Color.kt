package com.w2sv.filenavigator.ui.utils.extensions

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.graphics.Color
import com.w2sv.filenavigator.ui.theme.AppColor

@Composable
@ReadOnlyComposable
fun Color.orDisabledIf(condition: Boolean): Color =
    if (condition) AppColor.disabled else this