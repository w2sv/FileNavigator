package com.w2sv.filenavigator.ui.designsystem.drawer

import androidx.annotation.FloatRange
import androidx.compose.material3.DrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalConfiguration
import com.w2sv.composed.extensions.rememberVisibilityPercentage

@Stable
class DrawerRepelledAnimationState(
    @FloatRange(0.0, 1.0) val scale: Float,
    val rotation: Float,
    val transitionX: Float,
    val transitionY: Float
)

@Composable
fun rememberDrawerRepelledAnimationState(drawerState: DrawerState): DrawerRepelledAnimationState {
    val screenWidth = LocalConfiguration.current.screenWidthDp
    val screenHeight = LocalConfiguration.current.screenHeightDp

    val drawerVisibilityPercentage by drawerState.rememberVisibilityPercentage()

    return remember(screenWidth, screenHeight, drawerVisibilityPercentage) {
        DrawerRepelledAnimationState(
            scale = 1 - drawerVisibilityPercentage,
            rotation = 180 * drawerVisibilityPercentage,
            transitionX = screenWidth * drawerVisibilityPercentage,
            transitionY = screenHeight * drawerVisibilityPercentage
        )
    }
}

@Stable
fun Modifier.drawerRepelledAnimation(state: DrawerRepelledAnimationState): Modifier =
    this then graphicsLayer(
        scaleX = state.scale,
        scaleY = state.scale,
        translationX = state.transitionX,
        translationY = state.transitionY,
        rotationY = state.rotation,
        rotationZ = state.rotation
    )
