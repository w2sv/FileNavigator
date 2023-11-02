package com.w2sv.filenavigator.ui.components.drawer

import androidx.compose.material3.DrawerDefaults
import androidx.compose.material3.DrawerState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import com.w2sv.filenavigator.ui.utils.extensions.visibilityPercentage

fun Modifier.animateBasedOnDrawerProgression(drawerState: DrawerState): Modifier =
    composed {
        val maxDrawerWidthPx =
            with(LocalDensity.current) { DrawerDefaults.MaximumDrawerWidth.toPx() }

        val drawerVisibilityPercentage by remember {
            drawerState.visibilityPercentage(maxWidthPx = maxDrawerWidthPx)
        }
        val drawerVisibilityPercentageInverse by remember {
            derivedStateOf {
                1 - drawerVisibilityPercentage
            }
        }
        val drawerVisibilityPercentageAngle by remember {
            derivedStateOf {
                180 * drawerVisibilityPercentage
            }
        }

        return@composed graphicsLayer(
            scaleX = drawerVisibilityPercentageInverse,
            scaleY = drawerVisibilityPercentageInverse,
            translationX = LocalConfiguration.current.screenWidthDp * drawerVisibilityPercentage,
            translationY = LocalConfiguration.current.screenHeightDp * drawerVisibilityPercentage,
            rotationY = drawerVisibilityPercentageAngle,
            rotationZ = drawerVisibilityPercentageAngle
        )
    }