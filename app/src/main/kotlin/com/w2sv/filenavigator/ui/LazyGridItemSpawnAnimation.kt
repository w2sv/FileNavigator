package com.w2sv.filenavigator.ui

import androidx.compose.animation.core.FiniteAnimationSpec
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.updateTransition
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.graphics.graphicsLayer

fun Modifier.animateGridItemSpawnOnScrollDown(
    itemIndex: Int,
    nColumns: Int,
    state: LazyListState,
    duration: Int = 500,
    fromScale: Float = 2f,
    toScale: Float = 1f,
    fromAlpha: Float = 0f,
    toAlpha: Float = 1f
): Modifier =
    composed {
        state.calculateDelay(itemIndex, nColumns)?.let { delay ->
            val (scale, alpha) = scaleAndAlpha(
                fromScale = fromScale,
                toScale = toScale,
                fromAlpha = fromAlpha,
                toAlpha = toAlpha,
                animation = tween(
                    durationMillis = duration,
                    delayMillis = delay,
                    easing = LinearOutSlowInEasing
                )
            )
            this then graphicsLayer(alpha = alpha, scaleX = scale, scaleY = scale)
        }
            ?: this
    }

private enum class GridItemState { PLACING, PLACED }

/**
 * @return null if scrolling up.
 */
@Composable
private fun LazyListState.calculateDelay(
    index: Int,
    columnCount: Int
): Int? {
    val firstVisibleRow by remember { mutableStateOf(firstVisibleItemIndex) }

    val row = index / columnCount
    if (firstVisibleRow >= row) {
        return null
    }

    val column = index % columnCount
    val nVisibleRows = layoutInfo.visibleItemsInfo.count()

    val rowDelay = 100 * when (nVisibleRows) {
        0 -> row // initial load
        else -> nVisibleRows + firstVisibleRow - row
    }
    val columnDelay = column * 150
    return rowDelay + columnDelay
}

@Composable
private fun scaleAndAlpha(
    fromScale: Float,
    toScale: Float,
    fromAlpha: Float,
    toAlpha: Float,
    animation: FiniteAnimationSpec<Float>
): Pair<Float, Float> {
    val transitionState =
        remember {
            MutableTransitionState(GridItemState.PLACING).apply {
                targetState = GridItemState.PLACED
            }
        }
    val transition = updateTransition(transitionState, label = "")
    val alpha by transition.animateFloat(transitionSpec = { animation }, label = "") { state ->
        when (state) {
            GridItemState.PLACING -> fromAlpha
            GridItemState.PLACED -> toAlpha
        }
    }
    val scale by transition.animateFloat(transitionSpec = { animation }, label = "") { state ->
        when (state) {
            GridItemState.PLACING -> fromScale
            GridItemState.PLACED -> toScale
        }
    }
    return alpha to scale
}