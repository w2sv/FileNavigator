package com.w2sv.filenavigator.ui.components

import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.FloatSpringSpec
import androidx.compose.animation.core.Spring
import androidx.compose.material3.DrawerState
import androidx.compose.material3.DrawerValue

val drawerStateAnim = FloatSpringSpec(Spring.DampingRatioMediumBouncy)

suspend fun DrawerState.closeAnimated(anim: AnimationSpec<Float> = drawerStateAnim) {
    animateTo(DrawerValue.Closed, anim)
}

suspend fun DrawerState.openAnimated(anim: AnimationSpec<Float> = drawerStateAnim) {
    animateTo(DrawerValue.Open, anim)
}