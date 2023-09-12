package com.w2sv.filenavigator.ui.components

import android.view.animation.OvershootInterpolator
import androidx.annotation.StringRes
import androidx.compose.animation.animateColor
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.updateTransition
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.w2sv.data.model.Theme
import com.w2sv.filenavigator.R
import com.w2sv.filenavigator.ui.theme.AppTheme
import com.w2sv.filenavigator.ui.theme.DefaultAnimationDuration
import com.w2sv.filenavigator.ui.utils.toEasing

@Composable
fun ThemeSelectionRow(
    selected: Theme,
    onSelected: (Theme) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        remember {
            listOf(
                ThemeIndicatorProperties(
                    theme = Theme.Light,
                    label = R.string.light,
                    buttonColor = ButtonColor.Uniform(Color.White)
                ),
                ThemeIndicatorProperties(
                    theme = Theme.DeviceDefault,
                    label = R.string._default,
                    buttonColor = ButtonColor.Gradient(
                        Brush.linearGradient(
                            0.5f to Color.White,
                            0.5f to Color.Black
                        )
                    )
                ),
                ThemeIndicatorProperties(
                    theme = Theme.Dark,
                    label = R.string.dark,
                    buttonColor = ButtonColor.Uniform(Color.Black)
                )
            )
        }
            .forEach { properties ->
                ThemeColumn(
                    properties = properties,
                    isSelected = { properties.theme == selected },
                ) {
                    onSelected(properties.theme)
                }
            }
    }
}

@Immutable
data class ThemeIndicatorProperties(
    val theme: Theme,
    @StringRes val label: Int,
    val buttonColor: ButtonColor
)

sealed class ButtonColor(val containerColor: Color) {
    class Uniform(color: Color) : ButtonColor(color)
    class Gradient(val brush: Brush) : ButtonColor(Color.Transparent)
}

@Composable
private fun ThemeColumn(
    properties: ThemeIndicatorProperties,
    isSelected: () -> Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.SpaceEvenly,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        AppFontText(
            text = stringResource(id = properties.label),
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Spacer(modifier = Modifier.height(8.dp))
        ThemeButton(
            buttonColor = properties.buttonColor,
            contentDescription = stringResource(id = R.string.theme_button_cd).format(
                stringResource(id = properties.label)
            ),
            onClick = onClick,
            isSelected = isSelected
        )
    }
}

@Composable
fun ThemeButton(
    buttonColor: ButtonColor,
    contentDescription: String,
    onClick: () -> Unit,
    isSelected: () -> Boolean,
    modifier: Modifier = Modifier,
    size: Dp = 40.dp
) {
    val radius = with(LocalDensity.current) { (size / 2).toPx() }

    val transition = updateTransition(targetState = isSelected(), label = "")

    val borderWidth by transition.animateFloat(
        transitionSpec = {
            if (targetState) {
                tween(
                    durationMillis = DefaultAnimationDuration,
                    easing = OvershootInterpolator().toEasing()
                )
            } else {
                tween(durationMillis = DefaultAnimationDuration)
            }
        }, label = ""
    ) { state ->
        if (state) 3f else 0f
    }

    val borderColor by transition.animateColor(
        transitionSpec = {
            if (targetState) {
                tween(
                    durationMillis = DefaultAnimationDuration,
                    easing = OvershootInterpolator().toEasing()
                )
            } else {
                tween(durationMillis = DefaultAnimationDuration)
            }
        }, label = ""
    ) { state ->
        if (state) MaterialTheme.colorScheme.primary else Color.Transparent
    }

    Button(
        modifier = modifier
            .semantics {
                this.contentDescription = contentDescription
            }
            .size(size)
            .drawBehind {
                if (buttonColor is ButtonColor.Gradient) {
                    drawCircle(
                        buttonColor.brush,
                        radius = radius
                    )
                }
            },
        colors = ButtonDefaults.buttonColors(containerColor = buttonColor.containerColor),
        onClick = onClick,
        shape = CircleShape,
        border = BorderStroke(borderWidth.dp, borderColor)
    ) {}
}

@Preview
@Composable
fun ThemeButtonPrev() {
    AppTheme {
        val properties = ThemeIndicatorProperties(
            theme = Theme.Dark,
            label = R.string.dark,
            buttonColor = ButtonColor.Uniform(Color.Black)
        )
        ThemeButton(
            buttonColor = properties.buttonColor,
            contentDescription = "",
            onClick = { /*TODO*/ },
            isSelected = { true })
    }
}