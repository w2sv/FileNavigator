package com.w2sv.filenavigator.ui.screen.home.statusdisplay

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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.w2sv.core.common.R
import com.w2sv.filenavigator.ui.LocalNavigator
import com.w2sv.filenavigator.ui.navigation.Navigator
import com.w2sv.filenavigator.ui.screen.home.HomeScreenCard
import com.w2sv.filenavigator.ui.theme.AppColor
import com.w2sv.filenavigator.ui.theme.DEFAULT_ANIMATION_DURATION
import com.w2sv.filenavigator.ui.util.Easing
import com.w2sv.filenavigator.ui.util.PreviewOf
import com.w2sv.navigator.FileNavigator

private enum class NavigatorStatusCardData(val statusText: StatusText, val toggleButton: ToggleButton) {
    IsInactive(
        statusText = StatusText(
            textRes = R.string.inactive,
            color = AppColor.error
        ),
        toggleButton = ToggleButton(
            color = AppColor.success,
            iconRes = R.drawable.ic_start_24,
            labelRes = R.string.start,
            onClick = { FileNavigator.start(it) }
        )
    ),
    IsRunning(
        statusText = StatusText(
            textRes = R.string.active,
            color = AppColor.success
        ),
        toggleButton = ToggleButton(
            color = AppColor.error,
            iconRes = R.drawable.ic_stop_24,
            labelRes = R.string.stop,
            onClick = { FileNavigator.stop(it) }
        )
    );

    data class StatusText(@StringRes val textRes: Int, val color: Color)

    data class ToggleButton(val color: Color, @DrawableRes val iconRes: Int, @StringRes val labelRes: Int, val onClick: (Context) -> Unit)

    companion object {
        operator fun invoke(isRunning: Boolean): NavigatorStatusCardData =
            if (isRunning) IsRunning else IsInactive
    }
}

@Composable
fun NavigatorStatusCard(
    navigatorIsRunning: Boolean,
    modifier: Modifier = Modifier,
    navigator: Navigator = LocalNavigator.current
) {
    val uiData = remember(navigatorIsRunning) { NavigatorStatusCardData(navigatorIsRunning) }

    HomeScreenCard(
        modifier,
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        HeaderWithStatus(uiData.statusText)
        ButtonRow(
            toggleButton = uiData.toggleButton,
            onSettingsButtonClick = { navigator.toNavigatorSettings() },
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Preview
@Composable
private fun Prev() {
    PreviewOf { NavigatorStatusCard(navigatorIsRunning = true) }
}

@Composable
private fun HeaderWithStatus(statusText: NavigatorStatusCardData.StatusText, modifier: Modifier = Modifier) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = modifier) {
        Text(
            text = stringResource(R.string.navigator),
            style = MaterialTheme.typography.headlineMedium
        )
        VerticalDivider(
            modifier = Modifier
                .height(16.dp)
                .padding(horizontal = 12.dp),
            color = MaterialTheme.colorScheme.onSurface,
            thickness = Dp.Hairline
        )
        UpSlidingAnimatedContent(targetState = statusText) {
            Text(
                text = stringResource(id = it.textRes),
                style = MaterialTheme.typography.headlineMedium,
                color = it.color
            )
        }
    }
}

@Composable
private fun ButtonRow(
    toggleButton: NavigatorStatusCardData.ToggleButton,
    onSettingsButtonClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
    ) {
        val buttonHeight = 65.dp
        NavigatorToggleButton(
            properties = toggleButton,
            modifier = Modifier
                .height(buttonHeight)
                .weight(0.5f)
        )
        Spacer(modifier = Modifier.weight(0.05f))
        FilledTonalButton(
            onClick = onSettingsButtonClick,
            modifier = Modifier
                .height(buttonHeight)
                .weight(0.3f)
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_settings_24),
                    contentDescription = null
                )
                Text(stringResource(R.string.settings))
            }
        }
    }
}

@Composable
private fun NavigatorToggleButton(properties: NavigatorStatusCardData.ToggleButton, modifier: Modifier = Modifier) {
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
