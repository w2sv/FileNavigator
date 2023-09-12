package com.w2sv.filenavigator.ui.utils

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

fun DrawerState.getOffsetFractionState(maxWidthPx: Int): State<Float> =
    derivedStateOf { 1 + offset.value / maxWidthPx }