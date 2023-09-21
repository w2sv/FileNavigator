package com.w2sv.filenavigator.ui.utils.extensions

import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.FloatSpringSpec
import androidx.compose.animation.core.Spring
import androidx.compose.material3.DrawerState
import androidx.compose.material3.DrawerValue
import androidx.compose.runtime.State
import androidx.compose.runtime.derivedStateOf

private val drawerAnim = FloatSpringSpec(Spring.DampingRatioMediumBouncy)

suspend fun DrawerState.closeAnimated(anim: AnimationSpec<Float> = drawerAnim) {
    animateTo(DrawerValue.Closed, anim)
}

suspend fun DrawerState.openAnimated(anim: AnimationSpec<Float> = drawerAnim) {
    animateTo(DrawerValue.Open, anim)
}

fun DrawerState.visibilityPercentage(maxWidthPx: Float): State<Float> =
    derivedStateOf { offset.value / maxWidthPx + 1 }  // offset.value = -MAX when completely hidden, 0 when completely visible