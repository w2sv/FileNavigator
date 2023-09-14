package com.w2sv.filenavigator.ui.screens.main.components

import android.view.animation.AnticipateOvershootInterpolator
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.w2sv.filenavigator.ui.components.AppFontText
import com.w2sv.filenavigator.ui.components.bounceOnClickAnimation
import com.w2sv.filenavigator.ui.theme.DefaultAnimationDuration
import com.w2sv.filenavigator.ui.utils.toEasing

data class ToggleNavigatorButtonConfiguration(
    val color: Color,
    @DrawableRes val iconRes: Int,
    @StringRes val labelRes: Int,
    val onClick: () -> Unit
)

data class ToggleNavigatorButtonConfigurations(
    val startNavigator: ToggleNavigatorButtonConfiguration,
    val stopNavigator: ToggleNavigatorButtonConfiguration
)

@Composable
internal fun ToggleNavigatorButton(
    configuration: ToggleNavigatorButtonConfiguration,
    modifier: Modifier = Modifier
) {
    ElevatedButton(
        onClick = configuration.onClick,
        modifier = modifier.bounceOnClickAnimation(),
    ) {
        AnimatedContent(
            targetState = configuration,
            label = "",
            transitionSpec = {
                slideInVertically(
                    animationSpec = tween(
                        durationMillis = DefaultAnimationDuration,
                        easing = AnticipateOvershootInterpolator().toEasing()
                    ),
                    initialOffsetY = { it }) togetherWith
                        slideOutVertically(
                            targetOffsetY = { -it },
                            animationSpec = tween(
                                durationMillis = DefaultAnimationDuration,
                                easing = AnticipateOvershootInterpolator().toEasing()
                            )
                        )
            }
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceEvenly,
                modifier = Modifier.fillMaxSize()
            ) {
                Icon(
                    painter = painterResource(id = it.iconRes),
                    contentDescription = null,
                    modifier = Modifier.size(32.dp),
                    tint = it.color
                )
                AppFontText(
                    text = stringResource(id = it.labelRes),
                    fontSize = 18.sp,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }
        }
    }
}