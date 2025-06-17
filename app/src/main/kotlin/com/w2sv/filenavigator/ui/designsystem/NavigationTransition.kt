package com.w2sv.filenavigator.ui.designsystem

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.ui.unit.IntOffset
import androidx.navigation.NavBackStackEntry
import com.ramcosta.composedestinations.spec.DestinationStyle

private typealias NavigationEnterTransition = (AnimatedContentTransitionScope<NavBackStackEntry>.() -> EnterTransition?)
private typealias NavigationExitTransition = (AnimatedContentTransitionScope<NavBackStackEntry>.() -> ExitTransition?)

object NavigationTransitions : DestinationStyle.Animated() {

    private val springSpec = spring<IntOffset>(
        stiffness = Spring.StiffnessMediumLow,
        dampingRatio = Spring.DampingRatioNoBouncy
    )

    override val enterTransition: NavigationEnterTransition = {
        slideInHorizontally(
            initialOffsetX = { it }, // from right
            animationSpec = springSpec
        )
    }

    override val exitTransition: NavigationExitTransition = {
        slideOutHorizontally(
            targetOffsetX = { -it }, // to left
            animationSpec = springSpec
        )
    }

    override val popEnterTransition: NavigationEnterTransition = {
        slideInHorizontally(
            initialOffsetX = { -it }, // from left (back)
            animationSpec = springSpec
        )
    }

    override val popExitTransition: NavigationExitTransition = {
        slideOutHorizontally(
            targetOffsetX = { it }, // to right (back)
            animationSpec = springSpec
        )
    }
}
