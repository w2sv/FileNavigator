package com.w2sv.filenavigator.ui.designsystem.drawer

import androidx.compose.material3.DrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import com.w2sv.composed.core.OnChange
import com.w2sv.composed.material3.extensions.rememberVisibilityPercentage

internal enum class DrawerRepelledAnimationContentMovement(val translationMultiplier: Int) {
    Repelling(1),
    Returning(-1)
}

@Composable
fun rememberDrawerRepelledAnimationState(drawerState: DrawerState): DrawerRepelledAnimationState {
    val drawerVisibilityPercentage by drawerState.rememberVisibilityPercentage()

    return remember(drawerState) {
        DrawerRepelledAnimationState(
            getDrawerVisibilityPercentage = { drawerVisibilityPercentage }
        )
    }
        .apply {
            OnChange(value = drawerVisibilityPercentage) {
                when (it) {
                    0f -> setContentMovement(DrawerRepelledAnimationContentMovement.Repelling)
                    1f -> setContentMovement(DrawerRepelledAnimationContentMovement.Returning)
                }
            }
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

    private var contentMovement by mutableStateOf(DrawerRepelledAnimationContentMovement.Repelling)

    internal fun setContentMovement(contentMovement: DrawerRepelledAnimationContentMovement) {
        this.contentMovement = contentMovement
    }

    val translationMultiplier
        get() = contentMovement.translationMultiplier
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
        translationX =
            animationBoxWidth * state.drawerVisibilityPercentage * state.translationMultiplier
        translationY =
            animationBoxHeight * state.drawerVisibilityPercentage * state.translationMultiplier
        rotationY = state.drawerVisibilityPercentageAngle
        rotationZ = state.drawerVisibilityPercentageAngle
    }
