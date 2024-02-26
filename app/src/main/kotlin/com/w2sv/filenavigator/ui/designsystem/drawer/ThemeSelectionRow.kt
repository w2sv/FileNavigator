package com.w2sv.filenavigator.ui.designsystem.drawer

import android.view.animation.OvershootInterpolator
import androidx.annotation.StringRes
import androidx.compose.animation.animateColor
import androidx.compose.animation.core.animateDp
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.updateTransition
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.w2sv.domain.model.Theme
import com.w2sv.filenavigator.R
import com.w2sv.filenavigator.ui.utils.toEasing

@Composable
fun ThemeSelectionRow(
    selected: Theme,
    onSelected: (Theme) -> Unit,
    modifier: Modifier = Modifier,
    buttonSize: Dp = 44.dp,
    horizontalArrangement: Arrangement.Horizontal = Arrangement.Center,
) {
    Row(
        modifier = modifier,
        horizontalArrangement = horizontalArrangement,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        themeIndicatorProperties
            .forEach { properties ->
                ThemeIndicator(
                    properties = properties,
                    isSelected = { properties.theme == selected },
                    onClick = { onSelected(properties.theme) },
                    buttonModifier = Modifier.size(buttonSize)
                )
            }
    }
}

private val themeIndicatorProperties =
    listOf(
        ThemeIndicatorProperties(
            theme = Theme.Light,
            labelRes = R.string.light,
            buttonColoring = ButtonColor.Uniform(Color.White),
        ),
        ThemeIndicatorProperties(
            theme = Theme.Default,
            labelRes = R.string._default,
            buttonColoring = ButtonColor.Gradient(
                Brush.linearGradient(
                    0.5f to Color.White,
                    0.5f to Color.Black,
                ),
            ),
        ),
        ThemeIndicatorProperties(
            theme = Theme.Dark,
            labelRes = R.string.dark,
            buttonColoring = ButtonColor.Uniform(Color.Black),
        ),
    )

@Immutable
private data class ThemeIndicatorProperties(
    val theme: Theme,
    @StringRes val labelRes: Int,
    val buttonColoring: ButtonColor,
)

@Immutable
private sealed interface ButtonColor {
    val containerColor: Color

    @Immutable
    data class Uniform(override val containerColor: Color) : ButtonColor

    @Immutable
    data class Gradient(val brush: Brush) : ButtonColor {
        override val containerColor: Color = Color.Transparent
    }
}

@Composable
private fun ThemeIndicator(
    properties: ThemeIndicatorProperties,
    isSelected: () -> Boolean,
    modifier: Modifier = Modifier,
    buttonModifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = stringResource(id = properties.labelRes),
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Spacer(modifier = Modifier.height(8.dp))
        ThemeButton(
            buttonColor = properties.buttonColoring,
            contentDescription = stringResource(id = R.string.theme_button_cd).format(
                stringResource(id = properties.labelRes),
            ),
            onClick = onClick,
            isSelected = isSelected,
            modifier = buttonModifier,
        )
    }
}

@Composable
private fun ThemeButton(
    buttonColor: ButtonColor,
    contentDescription: String,
    onClick: () -> Unit,
    isSelected: () -> Boolean,
    modifier: Modifier = Modifier,
) {
    val transition = updateTransition(targetState = isSelected(), label = "")

    val borderWidth by transition.animateDp(
        transitionSpec = {
            if (targetState) {
                tween(
                    durationMillis = BORDER_ANIMATION_DURATION,
                    easing = OvershootInterpolator().toEasing(),
                )
            } else {
                tween(durationMillis = BORDER_ANIMATION_DURATION)
            }
        },
        label = "",
    ) { state ->
        if (state) 3.dp else 0.dp
    }

    val borderColor by transition.animateColor(
        transitionSpec = {
            if (targetState) {
                tween(
                    durationMillis = BORDER_ANIMATION_DURATION,
                    easing = OvershootInterpolator().toEasing(),
                )
            } else {
                tween(durationMillis = BORDER_ANIMATION_DURATION)
            }
        },
        label = "",
    ) { state ->
        if (state) MaterialTheme.colorScheme.primary else Color.Transparent
    }

    BoxWithConstraints(contentAlignment = Alignment.Center, modifier = modifier) {
        val size = with(LocalDensity.current) { constraints.maxWidth.toDp() }
        Button(
            modifier = Modifier
                .semantics {
                    this.contentDescription = contentDescription
                }
                .size(size)
                .drawBehind {
                    if (buttonColor is ButtonColor.Gradient) {
                        drawCircle(
                            brush = buttonColor.brush,
                            radius = (constraints.maxWidth / 2).toFloat(),
                        )
                    }
                },
            colors = ButtonDefaults.buttonColors(containerColor = buttonColor.containerColor),
            onClick = onClick,
            shape = CircleShape,
            border = BorderStroke(borderWidth, borderColor),
        ) {}
    }
}

private const val BORDER_ANIMATION_DURATION = 500