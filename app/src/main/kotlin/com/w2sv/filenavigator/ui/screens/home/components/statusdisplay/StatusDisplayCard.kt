package com.w2sv.filenavigator.ui.screens.home.components.statusdisplay

import android.content.Context
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedContentScope
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.w2sv.filenavigator.R
import com.w2sv.filenavigator.ui.designsystem.MoreElevatedCard
import com.w2sv.filenavigator.ui.screens.Screen
import com.w2sv.filenavigator.ui.sharedviewmodels.AppViewModel
import com.w2sv.filenavigator.ui.sharedviewmodels.NavigatorViewModel
import com.w2sv.filenavigator.ui.theme.AppColor
import com.w2sv.filenavigator.ui.theme.DefaultAnimationDuration
import com.w2sv.filenavigator.ui.utils.Easing
import com.w2sv.navigator.FileNavigator

@Composable
fun StatusDisplayCard(
    modifier: Modifier = Modifier,
    navigatorStateVM: NavigatorViewModel = viewModel(),
    appVM: AppViewModel = viewModel(),
    context: Context = LocalContext.current
) {
    val navigatorIsRunning by navigatorStateVM.isRunning.collectAsStateWithLifecycle()

    val navigatorIsRunningDependentPropertiesMap = remember {
        mapOf(
            false to NavigatorIsRunningDependentProperties(
                statusTextProperties = StatusTextProperties(
                    textRes = R.string.inactive,
                    color = AppColor.error
                ),
                toggleButtonProperties = ToggleNavigatorButtonProperties(
                    color = AppColor.success,
                    iconRes = R.drawable.ic_start_24,
                    labelRes = R.string.start_navigator
                ) { FileNavigator.start(context) }
            ),
            true to NavigatorIsRunningDependentProperties(
                statusTextProperties = StatusTextProperties(
                    textRes = R.string.active,
                    color = AppColor.success
                ),
                toggleButtonProperties = ToggleNavigatorButtonProperties(
                    color = AppColor.error,
                    iconRes = R.drawable.ic_stop_24,
                    labelRes = R.string.stop_navigator
                ) { FileNavigator.stop(context) }
            )
        )
    }

    val navigatorIsRunningDependentProperties = remember(navigatorIsRunning) {
        navigatorIsRunningDependentPropertiesMap.getValue(navigatorIsRunning)
    }

    MoreElevatedCard(
        modifier = modifier.fillMaxSize(),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceEvenly
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = stringResource(R.string.navigator_status),
                    style = MaterialTheme.typography.headlineMedium
                )
                Spacer(modifier = Modifier.width(14.dp))
                UpSlidingAnimatedContent(targetState = navigatorIsRunningDependentProperties.statusTextProperties) {
                    Text(
                        text = stringResource(id = it.textRes),
                        fontSize = 20.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = it.color
                    )
                }
            }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceAround,
                modifier = Modifier.fillMaxWidth()
            ) {
                ToggleNavigatorButton(
                    properties = navigatorIsRunningDependentProperties.toggleButtonProperties,
                    modifier = Modifier
                        .height(65.dp)
                        .width(180.dp)
                )
                FilledIconButton(
                    onClick = { appVM.setScreen(Screen.NavigatorSettings) }
                ) {
                    Icon(
                        painter = painterResource(id = com.w2sv.navigator.R.drawable.ic_settings_24),
                        contentDescription = null
                    )
                }
            }
        }
    }
}

@Immutable
private data class NavigatorIsRunningDependentProperties(
    val statusTextProperties: StatusTextProperties,
    val toggleButtonProperties: ToggleNavigatorButtonProperties
)

private data class StatusTextProperties(@StringRes val textRes: Int, val color: Color)

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
    Button(
        onClick = properties.onClick,
        modifier = modifier,
    ) {
        UpSlidingAnimatedContent(targetState = properties) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(
                    painter = painterResource(id = it.iconRes),
                    contentDescription = null,
                    modifier = Modifier.size(32.dp),
                    tint = it.color
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = stringResource(id = it.labelRes),
                    fontSize = 18.sp,
                )
            }
        }
    }
}

@Composable
private fun <S> UpSlidingAnimatedContent(
    targetState: S,
    modifier: Modifier = Modifier,
    contentAlignment: Alignment = Alignment.TopStart,
    label: String = "AnimatedContent",
    contentKey: (targetState: S) -> Any? = { it },
    content: @Composable AnimatedContentScope.(targetState: S) -> Unit
) {
    AnimatedContent(
        targetState = targetState,
        modifier = modifier,
        contentAlignment = contentAlignment,
        label = label,
        contentKey = contentKey,
        transitionSpec = remember {
            {
                slideInVertically(
                    animationSpec = tween(
                        durationMillis = DefaultAnimationDuration,
                        easing = Easing.AnticipateOvershoot
                    ),
                    initialOffsetY = { it }) togetherWith
                        slideOutVertically(
                            targetOffsetY = { -it },
                            animationSpec = tween(
                                durationMillis = DefaultAnimationDuration,
                                easing = Easing.AnticipateOvershoot
                            )
                        )
            }
        },
        content = content
    )
}