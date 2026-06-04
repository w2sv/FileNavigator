package com.w2sv.filenavigator.ui.navigation

import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.FiniteAnimationSpec
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import com.w2sv.kotlinutils.threadUnsafeLazy

private const val ANIMATION_DURATION = 220

object NavAnimation {

    private val easing by threadUnsafeLazy { CubicBezierEasing(0.2f, 0.0f, 0.0f, 1.0f) }

    object PrimaryDestination {

        fun enter() =
            fadeIn(animationSpec = fadeAnimationSpec) +
                scaleIn(
                    initialScale = 0.98f,
                    animationSpec = scaleAnimationSpec
                )

        fun exit() =
            fadeOut(animationSpec = fadeAnimationSpec) +
                scaleOut(
                    targetScale = 1.02f,
                    animationSpec = scaleAnimationSpec
                )

        private val scaleAnimationSpec: FiniteAnimationSpec<Float> by threadUnsafeLazy {
            tween(
                durationMillis = ANIMATION_DURATION,
                easing = easing
            )
        }

        private val fadeAnimationSpec: FiniteAnimationSpec<Float> by threadUnsafeLazy {
            tween(
                durationMillis = ANIMATION_DURATION,
                easing = easing
            )
        }
    }
}
