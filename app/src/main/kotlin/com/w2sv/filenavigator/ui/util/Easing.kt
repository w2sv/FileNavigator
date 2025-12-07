package com.w2sv.filenavigator.ui.util

import android.view.animation.AnticipateInterpolator
import android.view.animation.AnticipateOvershootInterpolator
import android.view.animation.OvershootInterpolator
import com.w2sv.composed.core.extensions.toEasing
import com.w2sv.kotlinutils.threadUnsafeLazy

object Easing {
    val Anticipate by threadUnsafeLazy { AnticipateInterpolator().toEasing() }
    val Overshoot by threadUnsafeLazy { OvershootInterpolator().toEasing() }
    val AnticipateOvershoot by threadUnsafeLazy { AnticipateOvershootInterpolator().toEasing() }
}
