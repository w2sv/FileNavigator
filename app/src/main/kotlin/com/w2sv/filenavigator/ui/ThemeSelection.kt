package com.w2sv.filenavigator.ui

import android.view.animation.AccelerateDecelerateInterpolator
import androidx.annotation.StringRes
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.updateTransition
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.w2sv.filenavigator.R
import com.w2sv.filenavigator.ui.theme.AppTheme
import com.w2sv.filenavigator.ui.theme.DefaultIconSize
import com.w2sv.filenavigator.utils.toEasing

enum class Theme {
    Light,
    DeviceDefault,
    Dark
}

@Composable
fun ThemeSelectionDialog(
    onDismissRequest: () -> Unit,
    selectedTheme: () -> Theme,
    onThemeSelected: (Theme) -> Unit,
    applyButtonEnabled: () -> Boolean,
    onApplyButtonClick: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismissRequest,
        title = { AppFontText(text = stringResource(id = R.string.theme)) },
        icon = {
            Icon(
                painter = painterResource(id = R.drawable.ic_nightlight_24),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.secondary,
                modifier = Modifier.size(DefaultIconSize)
            )
        },
        confirmButton = {
            DialogButton(onClick = { onApplyButtonClick() }, enabled = applyButtonEnabled()) {
                AppFontText(text = stringResource(id = R.string.apply))
            }
        },
        dismissButton = {
            DialogButton(onClick = onDismissRequest) {
                AppFontText(text = stringResource(id = R.string.cancel))
            }
        },
        text = {
            ThemeSelectionRow(selected = selectedTheme, onSelected = onThemeSelected)
        }
    )
}

@Composable
fun ThemeSelectionRow(
    modifier: Modifier = Modifier,
    selected: () -> Theme,
    onSelected: (Theme) -> Unit
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        listOf(
            ThemeIndicatorProperties(
                theme = Theme.Light,
                label = R.string.light,
                buttonColor = ButtonColor.Uniform(Color.White)
            ),
            ThemeIndicatorProperties(
                theme = Theme.DeviceDefault,
                label = R.string.device_default,
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
            .forEach { properties ->
                ThemeColumn(
                    properties = properties,
                    isSelected = { properties.theme == selected() },
                    modifier = Modifier.padding(
                        horizontal = 12.dp
                    )
                ) {
                    onSelected(properties.theme)
                }
            }
    }
}

@Stable
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
    size: Dp = 38.dp
) {
    val radius = with(LocalDensity.current) { (size / 2).toPx() }

    val transition = updateTransition(targetState = isSelected(), label = "")

    val rotation by transition.animateFloat(
        transitionSpec = {
            if (targetState)
                tween(
                    1000,
                    easing = AccelerateDecelerateInterpolator().toEasing()
                )
            else
                tween()
        },
        label = ""
    ) { state ->
        if (state) 540f else 0f
    }

    val borderGradientBrush = Brush.sweepGradient(
        listOf(
            MaterialTheme.colorScheme.primary,
            MaterialTheme.colorScheme.tertiary,
            MaterialTheme.colorScheme.primary
        )
    )

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
                if (isSelected()) {
                    rotate(rotation) {
                        drawCircle(borderGradientBrush, style = Stroke(9f), radius = radius)
                    }
                }
            },
        colors = ButtonDefaults.buttonColors(containerColor = buttonColor.containerColor),
        onClick = onClick,
        shape = CircleShape,
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