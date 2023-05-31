package com.w2sv.filenavigator.ui

import androidx.compose.animation.core.Easing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.FiniteAnimationSpec
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.updateTransition
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.graphics.graphicsLayer

fun Modifier.animateGridItemSpawn(
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
        val (delay, easing) = state.calculateDelayAndEasing(itemIndex, nColumns)
        val (scale, alpha) = scaleAndAlpha(
            fromScale = fromScale,
            toScale = toScale,
            fromAlpha = fromAlpha,
            toAlpha = toAlpha,
            animation = tween(durationMillis = duration, delayMillis = delay, easing = easing)
        )
        this then graphicsLayer(alpha = alpha, scaleX = scale, scaleY = scale)
    }

private enum class GridItemState { PLACING, PLACED }

@Composable
private fun LazyListState.calculateDelayAndEasing(index: Int, columnCount: Int): Pair<Int, Easing> {
    val firstVisibleRow by remember { derivedStateOf { firstVisibleItemIndex } }

    val row = index / columnCount
    val column = index % columnCount
    val nVisibleRows = layoutInfo.visibleItemsInfo.count()
    val scrollingToBottom = firstVisibleRow < row
    val isFirstLoad = nVisibleRows == 0

    val rowDelay = 200 * when {
        isFirstLoad -> row // initial load
        scrollingToBottom -> nVisibleRows + firstVisibleRow - row // scrolling to bottom
        else -> 1 // scrolling to top
    }
    val columnDelay = column * 150 * if (scrollingToBottom || isFirstLoad) 1 else -1
    val easing =
        if (scrollingToBottom || isFirstLoad) LinearOutSlowInEasing else FastOutSlowInEasing
    return rowDelay + columnDelay to easing
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