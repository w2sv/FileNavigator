package com.w2sv.filenavigator.ui.designsystem.drawer

import androidx.compose.material3.DrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import com.w2sv.composed.extensions.rememberVisibilityPercentage

@Composable
fun rememberDrawerRepelledAnimationState(drawerState: DrawerState): DrawerRepelledAnimationState {
    val drawerVisibilityPercentage by drawerState.rememberVisibilityPercentage()

    return remember(drawerState) {
        DrawerRepelledAnimationState(getDrawerVisibilityPercentage = { drawerVisibilityPercentage })
    }
}

@Stable
class DrawerRepelledAnimationState(private val getDrawerVisibilityPercentage: () -> Float) {

    val drawerVisibilityPercentage: Float
        get() = getDrawerVisibilityPercentage()

    val drawerVisibilityPercentageInverse by derivedStateOf {
        1 - drawerVisibilityPercentage
    }

    val drawerVisibilityPercentageAngle by derivedStateOf {
        180 * drawerVisibilityPercentage
    }
}

@Stable
fun Modifier.drawerRepelledAnimation(
    state: DrawerRepelledAnimationState,
    animationBoxWidth: Int,
    animationBoxHeight: Int
): Modifier =
    graphicsLayer {
        scaleX = state.drawerVisibilityPercentageInverse
        scaleY = state.drawerVisibilityPercentageInverse
        translationX = animationBoxWidth * state.drawerVisibilityPercentage
        translationY = animationBoxHeight * state.drawerVisibilityPercentage
        rotationY = state.drawerVisibilityPercentageAngle
        rotationZ = state.drawerVisibilityPercentageAngle
    }
