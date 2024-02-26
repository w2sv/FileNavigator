package com.w2sv.filenavigator.ui.screens.home.components.statusdisplay

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
import androidx.compose.foundation.layout.size
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.w2sv.filenavigator.ui.components.bounceOnClickAnimation
import com.w2sv.filenavigator.ui.theme.DefaultAnimationDuration
import com.w2sv.filenavigator.ui.utils.toEasing

@Immutable
data class ToggleNavigatorButtonProperties(
    val color: Color,
    @DrawableRes val iconRes: Int,
    @StringRes val labelRes: Int,
    val onClick: () -> Unit
)

@Composable
internal fun ToggleNavigatorButton(
    properties: ToggleNavigatorButtonProperties,
    modifier: Modifier = Modifier
) {
    FilledTonalButton(
        onClick = properties.onClick,
        modifier = modifier.bounceOnClickAnimation(),
    ) {
        AnimatedContent(
            targetState = properties,
            label = "",
            transitionSpec = remember {
                {
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
            }
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceEvenly,
            ) {
                Icon(
                    painter = painterResource(id = it.iconRes),
                    contentDescription = null,
                    modifier = Modifier.size(32.dp),
                    tint = it.color
                )
                Text(
                    text = stringResource(id = it.labelRes),
                    fontSize = 18.sp,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }
        }
    }
}