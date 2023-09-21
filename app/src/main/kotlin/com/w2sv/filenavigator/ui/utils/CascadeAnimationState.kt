package com.w2sv.filenavigator.ui.utils

class CascadeAnimationState<T>(private val animationDelayPerRunningAnimation: Int = 100) {
    private val animatedElements: MutableSet<T> = mutableSetOf()
    private var nRunningAnimations: Int = 0

    fun animationImpending(element: T): Boolean =
        !animatedElements.contains(element)

    fun onAnimationStarted(element: T) {
        animatedElements.add(element)
        nRunningAnimations += 1
    }

    fun onAnimationFinished() {
        nRunningAnimations -= 1
    }

    val animationDelayMillis: Int
        get() = nRunningAnimations * animationDelayPerRunningAnimation
}