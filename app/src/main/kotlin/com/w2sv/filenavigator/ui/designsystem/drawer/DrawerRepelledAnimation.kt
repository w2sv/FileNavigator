package com.w2sv.filenavigator.ui.designsystem.drawer

import android.annotation.SuppressLint
import androidx.compose.material3.DrawerState
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalConfiguration
import com.w2sv.composed.extensions.rememberVisibilityPercentage

@SuppressLint("ComposeModifierComposed")  // TODO
@Stable
fun Modifier.drawerRepelledAnimation(drawerState: DrawerState): Modifier =
    this then composed {
        val drawerVisibilityPercentage by drawerState.rememberVisibilityPercentage()
        val drawerVisibilityPercentageInverse by remember(drawerVisibilityPercentage) {
            mutableFloatStateOf(1 - drawerVisibilityPercentage)
        }
        val drawerVisibilityPercentageAngle by remember(drawerVisibilityPercentage) {
            mutableFloatStateOf(180 * drawerVisibilityPercentage)
        }

        graphicsLayer(
            scaleX = drawerVisibilityPercentageInverse,
            scaleY = drawerVisibilityPercentageInverse,
            translationX = LocalConfiguration.current.screenWidthDp * drawerVisibilityPercentage,
            translationY = LocalConfiguration.current.screenHeightDp * drawerVisibilityPercentage,
            rotationY = drawerVisibilityPercentageAngle,
            rotationZ = drawerVisibilityPercentageAngle
        )
    }
