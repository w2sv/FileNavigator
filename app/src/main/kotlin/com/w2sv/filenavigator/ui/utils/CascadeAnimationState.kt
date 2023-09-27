package com.w2sv.filenavigator.ui.utils

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.w2sv.filenavigator.ui.theme.DefaultAnimationDuration

class CascadeAnimationState<T>(private val animationDelayPerRunningAnimation: Int = 100) {
    @Composable
    fun getAlpha(element: T): State<Float> {
        val animationImpending = animationImpending(element)
        var animatedProgress by remember { mutableFloatStateOf(if (animationImpending) 0f else 1f) }

        if (animationImpending) {
            LaunchedEffect(key1 = element) {
                onAnimationStarted(element)
                animatedProgress = 1f
            }
        }

        return animateFloatAsState(
            targetValue = animatedProgress,
            animationSpec = tween(
                durationMillis = DefaultAnimationDuration,
                delayMillis = animationDelayMillis
            ),
            label = "",
            finishedListener = {
                onAnimationFinished()
            }
        )
    }

    private val animatedElements: MutableSet<T> = mutableSetOf()
    private var nRunningAnimations: Int = 0

    private fun animationImpending(element: T): Boolean =
        !animatedElements.contains(element)

    private fun onAnimationStarted(element: T) {
        animatedElements.add(element)
        nRunningAnimations += 1
    }

    private fun onAnimationFinished() {
        nRunningAnimations -= 1
    }

    private val animationDelayMillis: Int
        get() = nRunningAnimations * animationDelayPerRunningAnimation
}