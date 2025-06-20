package com.w2sv.filenavigator.ui.navigation

import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.FiniteAnimationSpec
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.ui.unit.IntOffset

private const val ANIMATION_DURATION = 300

object NavAnimation {

    object NonPop {

        fun enter() = slideInHorizontally(
            initialOffsetX = { it / 2 },
            animationSpec = slideAnimationSpec
        ) + fadeIn(animationSpec = fadeAnimationSpec)

        fun exit() = slideOutHorizontally(
            targetOffsetX = { -it / 2 },
            animationSpec = slideAnimationSpec
        ) + fadeOut(animationSpec = fadeAnimationSpec)

        private val slideAnimationSpec: FiniteAnimationSpec<IntOffset> = tween(
            durationMillis = (ANIMATION_DURATION * 1.5).toInt(),
            easing = CubicBezierEasing(0.4f, 0.0f, 0.2f, 1f)
        )

        private val fadeAnimationSpec: FiniteAnimationSpec<Float> = tween(
            durationMillis = ANIMATION_DURATION,
            delayMillis = ANIMATION_DURATION / 4,
            easing = LinearOutSlowInEasing
        )
    }

    object Pop {

        fun enter() = slideInHorizontally(
            initialOffsetX = { -it / 2 },
            animationSpec = slideAnimationSpec
        ) + fadeIn(animationSpec = fadeAnimationSpec)

        fun exit() = slideOutHorizontally(
            targetOffsetX = { it / 2 },
            animationSpec = slideAnimationSpec
        ) + fadeOut(animationSpec = fadeAnimationSpec)

        private val slideAnimationSpec: FiniteAnimationSpec<IntOffset> = tween(
            durationMillis = (ANIMATION_DURATION * 1.2).toInt(),
            easing = CubicBezierEasing(0.6f, 0.05f, 0.19f, 0.95f)
        )

        private val fadeAnimationSpec: FiniteAnimationSpec<Float> = tween(
            durationMillis = ANIMATION_DURATION / 2,
            delayMillis = ANIMATION_DURATION / 4,
            easing = LinearEasing
        )
    }
}
