package com.w2sv.filenavigator.ui.screen.home.components.statusdisplay

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
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.FilledTonalButton
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
import com.ramcosta.composedestinations.generated.destinations.NavigatorSettingsScreenDestination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import com.w2sv.core.common.R
import com.w2sv.filenavigator.ui.LocalDestinationsNavigator
import com.w2sv.filenavigator.ui.screen.home.components.HomeScreenCard
import com.w2sv.filenavigator.ui.theme.AppColor
import com.w2sv.filenavigator.ui.theme.DEFAULT_ANIMATION_DURATION
import com.w2sv.filenavigator.ui.util.Easing
import com.w2sv.filenavigator.ui.util.activityViewModel
import com.w2sv.filenavigator.ui.viewmodel.NavigatorViewModel
import com.w2sv.navigator.FileNavigator

@Immutable
private data class NavigatorStatusUiData(
    val statusText: StatusText,
    val toggleButton: ToggleNavigatorButton
) {
    companion object {
        val map = mapOf(
            false to NavigatorStatusUiData(
                statusText = StatusText(
                    textRes = R.string.inactive,
                    color = AppColor.error
                ),
                toggleButton = ToggleNavigatorButton(
                    color = AppColor.success,
                    iconRes = R.drawable.ic_start_24,
                    labelRes = R.string.start,
                    onClick = { FileNavigator.start(it) }
                )
            ),
            true to NavigatorStatusUiData(
                statusText = StatusText(
                    textRes = R.string.active,
                    color = AppColor.success
                ),
                toggleButton = ToggleNavigatorButton(
                    color = AppColor.error,
                    iconRes = R.drawable.ic_stop_24,
                    labelRes = R.string.stop,
                    onClick = { FileNavigator.stop(it) }
                )
            )
        )
    }
}

private data class StatusText(@StringRes val textRes: Int, val color: Color)

@Immutable
private data class ToggleNavigatorButton(
    val color: Color,
    @DrawableRes val iconRes: Int,
    @StringRes val labelRes: Int,
    val onClick: (Context) -> Unit
)

@Composable
fun NavigatorStatusCard(
    modifier: Modifier = Modifier,
    navigatorVM: NavigatorViewModel = activityViewModel(),
    destinationsNavigator: DestinationsNavigator = LocalDestinationsNavigator.current
) {
    val navigatorIsRunning by navigatorVM.navigatorIsRunning.collectAsStateWithLifecycle()

    val navigatorStatusUiData = remember(navigatorIsRunning) {
        NavigatorStatusUiData.map.getValue(navigatorIsRunning)
    }

    HomeScreenCard(modifier) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = stringResource(R.string.navigator_status),
                style = MaterialTheme.typography.headlineMedium
            )
            Spacer(modifier = Modifier.width(6.dp))
            UpSlidingAnimatedContent(targetState = navigatorStatusUiData.statusText) {
                Text(
                    text = stringResource(id = it.textRes),
                    fontSize = 20.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = it.color
                )
            }
        }
        Spacer(modifier = Modifier.height(32.dp))
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceAround,
            modifier = Modifier.fillMaxWidth()
        ) {
            ToggleNavigatorButton(
                properties = navigatorStatusUiData.toggleButton,
                modifier = Modifier
                    .height(65.dp)
                    .width(180.dp)
            )
            FilledTonalButton(
                onClick = { destinationsNavigator.navigate(NavigatorSettingsScreenDestination) },
                modifier = Modifier.height(65.dp)
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_settings_24),
                        contentDescription = null
                    )
                    Text("Settings")
                }
            }
        }
    }
}

@Composable
private fun ToggleNavigatorButton(properties: ToggleNavigatorButton, modifier: Modifier = Modifier) {
    val context = LocalContext.current

    Button(
        onClick = { properties.onClick(context) },
        modifier = modifier
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
                    fontSize = 18.sp
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
                        durationMillis = DEFAULT_ANIMATION_DURATION,
                        easing = Easing.AnticipateOvershoot
                    ),
                    initialOffsetY = { it }
                ) togetherWith
                    slideOutVertically(
                        targetOffsetY = { -it },
                        animationSpec = tween(
                            durationMillis = DEFAULT_ANIMATION_DURATION,
                            easing = Easing.AnticipateOvershoot
                        )
                    )
            }
        },
        content = content
    )
}
