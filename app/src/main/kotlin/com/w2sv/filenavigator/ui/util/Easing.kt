package com.w2sv.filenavigator.ui.util

import android.view.animation.AnticipateInterpolator
import android.view.animation.AnticipateOvershootInterpolator
import android.view.animation.OvershootInterpolator
import com.w2sv.composed.extensions.toEasing

object Easing {
    val Anticipate = AnticipateInterpolator().toEasing()
    val Overshoot = OvershootInterpolator().toEasing()
    val AnticipateOvershoot = AnticipateOvershootInterpolator().toEasing()
}