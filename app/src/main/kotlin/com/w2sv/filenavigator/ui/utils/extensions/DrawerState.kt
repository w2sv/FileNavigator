package com.w2sv.filenavigator.ui.utils.extensions

import androidx.compose.material3.DrawerState
import androidx.compose.runtime.State
import androidx.compose.runtime.derivedStateOf

fun DrawerState.visibilityPercentage(maxWidthPx: Float): State<Float> =
    derivedStateOf { offset.value / maxWidthPx + 1 }  // offset.value = -MAX when completely hidden, 0 when completely visible