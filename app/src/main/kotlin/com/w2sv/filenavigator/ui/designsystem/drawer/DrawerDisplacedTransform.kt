package com.w2sv.filenavigator.ui.designsystem.drawer

import androidx.compose.runtime.Stable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer

/**
 * Applies a horizontal displacement to the content based on drawer visibility.
 *
 * The content is translated along the X axis proportionally to [visibilityProgress],
 * where `0f` represents a fully closed drawer (no displacement) and `1f` represents
 * a fully open drawer (fully displaced by [animationBoxWidthPx]).
 */
@Stable
fun Modifier.drawerDisplaced(visibilityProgress: Float, animationBoxWidthPx: Float): Modifier =
    graphicsLayer { translationX = animationBoxWidthPx * visibilityProgress }
