package com.w2sv.filenavigator.ui.navigation

import android.os.Build
import android.view.RoundedCorner
import androidx.compose.animation.EnterExitState
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateDp
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import androidx.navigation3.ui.LocalNavAnimatedContentScope

private object RoundedOnExitTokens {
    val FallbackCornerRadius = 28.dp
    val MaxShadowElevation = 28.dp
    val ShadowColor = Color.Black.copy(alpha = 0.2f)
}

/**
 * Clips the destination with animated, display-matched rounded corners and
 * adds a shadow as it exits, following Nav3's transition progress.
 */
@Composable
internal fun RoundedOnExit(modifier: Modifier = Modifier, content: @Composable BoxScope.() -> Unit) {
    val display = LocalView.current.display
    val density = LocalDensity.current

    val maxCornerRadius = remember(display, density) {
        val radiusPx = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            listOf(
                RoundedCorner.POSITION_TOP_LEFT,
                RoundedCorner.POSITION_TOP_RIGHT,
                RoundedCorner.POSITION_BOTTOM_LEFT,
                RoundedCorner.POSITION_BOTTOM_RIGHT
            )
                .mapNotNull { display?.getRoundedCorner(it)?.radius }
                .filter { it > 0 }
                .minOrNull()
        } else {
            null
        }

        with(density) { radiusPx?.toDp() } ?: RoundedOnExitTokens.FallbackCornerRadius
    }

    val cornerRadius = LocalNavAnimatedContentScope.current.transition.animateDp(
        transitionSpec = { tween(durationMillis = 350, easing = LinearEasing) },
        label = "NavigationCornerRadius"
    ) { state ->
        if (state == EnterExitState.PostExit) maxCornerRadius else 0.dp
    }

    val maxShadowElevationPx = with(density) { RoundedOnExitTokens.MaxShadowElevation.toPx() }

    Box(
        modifier = modifier
            .fillMaxSize()
            .graphicsLayer {
                val radius = cornerRadius.value
                val progress = (radius.value / maxCornerRadius.value)
                    .coerceIn(0f, 1f)

                clip = radius > 0.dp
                shape = RoundedCornerShape(radius)

                shadowElevation = maxShadowElevationPx * progress
                ambientShadowColor = RoundedOnExitTokens.ShadowColor
                spotShadowColor = RoundedOnExitTokens.ShadowColor
            },
        content = content
    )
}
