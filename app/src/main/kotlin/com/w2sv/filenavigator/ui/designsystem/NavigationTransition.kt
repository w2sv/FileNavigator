package com.w2sv.filenavigator.ui.designsystem

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.FiniteAnimationSpec
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.navigation.NavBackStackEntry
import com.ramcosta.composedestinations.spec.DestinationStyle

private typealias NavigationEnterTransition = (AnimatedContentTransitionScope<NavBackStackEntry>.() -> EnterTransition?)
private typealias NavigationExitTransition = (AnimatedContentTransitionScope<NavBackStackEntry>.() -> ExitTransition?)

abstract class PopNonPopIdenticalAnimatedDestinationStyle : DestinationStyle.Animated() {
    abstract override val enterTransition: NavigationEnterTransition
    abstract override val exitTransition: NavigationExitTransition

    override val popEnterTransition: NavigationEnterTransition get() = enterTransition
    override val popExitTransition: NavigationExitTransition get() = exitTransition
}

object NavigationTransitions : PopNonPopIdenticalAnimatedDestinationStyle() {
    override val enterTransition: NavigationEnterTransition = {
        fadeIn(animationSpec = animationSpec) +
            scaleIn(initialScale = 0.92f, animationSpec = animationSpec)
    }
    override val exitTransition: NavigationExitTransition = {
        fadeOut(animationSpec = animationSpec) + scaleOut(
            targetScale = 0.92f,
            animationSpec = animationSpec
        )
    }
}

private val animationSpec: FiniteAnimationSpec<Float> = tween(220)
