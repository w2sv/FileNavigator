package com.w2sv.filenavigator.ui.designsystem

import androidx.compose.animation.animateColor
import androidx.compose.animation.core.animateDp
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.updateTransition
import androidx.compose.foundation.BorderStroke
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import com.w2sv.filenavigator.ui.utils.Easing

@Immutable
data class BorderAnimationState(val width: () -> Dp, val color: () -> Color) {
    val borderStroke: BorderStroke
        get() = BorderStroke(width = width(), color = color())
}

@Composable
fun rememberBorderAnimationState(
    enabled: Boolean,
    startWidth: Dp,
    endWidth: Dp,
    startColor: Color,
    endColor: Color,
    duration: Int = 500
): BorderAnimationState {
    val transition = updateTransition(targetState = enabled, label = "")

    val borderWidth by transition.animateDp(
        transitionSpec = {
            remember {
                if (targetState) {
                    tween(
                        durationMillis = duration,
                        easing = Easing.Overshoot,
                    )
                } else {
                    tween(durationMillis = duration)
                }
            }
        },
        label = "",
    ) { state ->
        if (state) endWidth else startWidth
    }

    val borderColor by transition.animateColor(
        transitionSpec = {
            remember {
                if (targetState) {
                    tween(
                        durationMillis = duration,
                        easing = Easing.Overshoot,
                    )
                } else {
                    tween(durationMillis = duration)
                }
            }
        },
        label = "",
    ) { state ->
        if (state) endColor else startColor
    }

    return remember {
        BorderAnimationState(width = { borderWidth }, color = { borderColor })
    }
}
