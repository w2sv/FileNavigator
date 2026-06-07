package com.w2sv.filenavigator.ui.screen.home.navigatorstatus

import android.content.Context
import androidx.annotation.StringRes
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedContentScope
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.graphics.res.animatedVectorResource
import androidx.compose.animation.graphics.res.rememberAnimatedVectorPainter
import androidx.compose.animation.graphics.vector.AnimatedImageVector
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.w2sv.designsystem.Easing
import com.w2sv.designsystem.theme.DEFAULT_ANIMATION_DURATION
import com.w2sv.filenavigator.ui.designsystem.AppCard
import com.w2sv.filenavigator.ui.designsystem.AppCardHeaderIcon
import com.w2sv.filenavigator.ui.util.PreviewOf
import com.w2sv.modules.common.R
import com.w2sv.navigator.FileNavigator

@Preview
@Composable
private fun ActivePrev() {
    PreviewOf { NavigatorStatusCard(navigatorIsRunning = true) }
}

@Preview
@Composable
private fun InactivePrev() {
    PreviewOf { NavigatorStatusCard(navigatorIsRunning = false) }
}

private enum class NavigatorStatusCardData(val statusText: StatusText, val toggleButton: ToggleButton) {
    IsInactive(
        statusText = StatusText(
            textRes = R.string.inactive,
            color = { MaterialTheme.colorScheme.error }
        ),
        toggleButton = ToggleButton(
            labelRes = R.string.start,
            onClick = { FileNavigator.start(it) }
        )
    ),
    IsRunning(
        statusText = StatusText(
            textRes = R.string.active,
            color = { MaterialTheme.colorScheme.tertiary }
        ),
        toggleButton = ToggleButton(
            labelRes = R.string.stop,
            onClick = { FileNavigator.stop(it) }
        )
    );

    data class StatusText(@StringRes val textRes: Int, val color: @Composable () -> Color)

    data class ToggleButton(@StringRes val labelRes: Int, val onClick: (Context) -> Unit)

    companion object {
        operator fun invoke(isRunning: Boolean): NavigatorStatusCardData =
            if (isRunning) IsRunning else IsInactive
    }
}

@Composable
fun NavigatorStatusCard(navigatorIsRunning: Boolean, modifier: Modifier = Modifier) {
    val uiData = remember(navigatorIsRunning) { NavigatorStatusCardData(navigatorIsRunning) }

    AppCard(
        title = "${stringResource(R.string.navigator)}:",
        headerIcon = { AppCardHeaderIcon(R.drawable.ic_app_logo_24) },
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp),
        trailingHeaderContent = {
            Spacer(modifier = Modifier.width(8.dp))
            StatusText(uiData.statusText)
        }
    ) {
        NavigatorToggleButton(
            properties = uiData.toggleButton,
            navigatorIsRunning = navigatorIsRunning,
            modifier = Modifier
                .height(55.dp)
                .fillMaxWidth()
        )
    }
}

@Composable
private fun StatusText(statusText: NavigatorStatusCardData.StatusText, modifier: Modifier = Modifier) {
    FadeScaleAnimatedContent(
        targetState = statusText,
        modifier = modifier,
        contentAlignment = Alignment.CenterStart
    ) {
        Text(
            text = stringResource(id = it.textRes),
            style = MaterialTheme.typography.headlineMedium,
            color = it.color()
        )
    }
}

@Composable
private fun NavigatorToggleButton(
    properties: NavigatorStatusCardData.ToggleButton,
    navigatorIsRunning: Boolean,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val cornerRadius by animateDpAsState(
        targetValue = if (navigatorIsRunning) 28.dp else 8.dp,
        animationSpec = tween(
            durationMillis = DEFAULT_ANIMATION_DURATION,
            easing = Easing.AnticipateOvershoot
        )
    )

    Button(
        onClick = { properties.onClick(context) },
        modifier = modifier,
        shape = RoundedCornerShape(cornerRadius)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                painter = rememberAnimatedVectorPainter(
                    animatedImageVector = AnimatedImageVector.animatedVectorResource(R.drawable.ic_pause_to_play),
                    atEnd = !navigatorIsRunning
                ),
                contentDescription = null,
                modifier = Modifier.size(32.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            FadeScaleAnimatedContent(targetState = properties.labelRes) {
                Text(
                    text = stringResource(id = it),
                    fontSize = 18.sp
                )
            }
        }
    }
}

@Composable
private fun <S> FadeScaleAnimatedContent(
    targetState: S,
    modifier: Modifier = Modifier,
    contentAlignment: Alignment = Alignment.Center,
    label: String = "FadeScaleAnimatedContent",
    contentKey: (targetState: S) -> Any? = { it },
    content: @Composable AnimatedContentScope.(targetState: S) -> Unit
) {
    AnimatedContent(
        targetState = targetState,
        modifier = modifier,
        contentAlignment = contentAlignment,
        label = label,
        contentKey = contentKey,
        transitionSpec = {
            fadeIn(
                animationSpec = tween(
                    durationMillis = DEFAULT_ANIMATION_DURATION,
                    easing = Easing.AnticipateOvershoot
                )
            ) + scaleIn(
                initialScale = 0.92f,
                animationSpec = tween(
                    durationMillis = DEFAULT_ANIMATION_DURATION,
                    easing = Easing.AnticipateOvershoot
                )
            ) togetherWith
                fadeOut(
                    animationSpec = tween(
                        durationMillis = DEFAULT_ANIMATION_DURATION / 2,
                        easing = Easing.AnticipateOvershoot
                    )
                ) + scaleOut(
                    targetScale = 1.08f,
                    animationSpec = tween(
                        durationMillis = DEFAULT_ANIMATION_DURATION / 2,
                        easing = Easing.AnticipateOvershoot
                    )
                )
        },
        content = content
    )
}
