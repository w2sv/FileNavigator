package com.w2sv.filenavigator.ui.designsystem

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import com.w2sv.core.common.R
import com.w2sv.designsystem.CharSequenceText
import com.w2sv.designsystem.theme.AppTheme
import com.w2sv.designsystem.theme.onSurfaceVariantDecreasedAlpha

object ItemRowTokens {
    val IconTextSpacing = 16.dp
    val SwitchStartPadding = 8.dp
    val ExplanationTopPadding = 2.dp
}

@Composable
fun ItemRow(
    icon: @Composable () -> Unit,
    @StringRes labelRes: Int,
    modifier: Modifier = Modifier,
    explanation: CharSequence? = null,
    content: @Composable () -> Unit
) {
    ConstraintLayout(modifier.fillMaxWidth()) {
        val (iconRef, labelRef, contentRef, explanationRef) = createRefs()

        Box(
            modifier = Modifier.constrainAs(iconRef) {
                centerVerticallyTo(contentRef)
                start.linkTo(parent.start)
            },
            content = { icon() }
        )

        Text(
            text = stringResource(id = labelRes),
            modifier = Modifier.constrainAs(labelRef) {
                centerVerticallyTo(contentRef)
                linkTo(iconRef.end, contentRef.start, startMargin = ItemRowTokens.IconTextSpacing)
                width = Dimension.fillToConstraints
            }
        )

        Box(
            modifier = Modifier.constrainAs(contentRef) {
                end.linkTo(parent.end)
                top.linkTo(parent.top)
            },
            content = { content() }
        )

        explanation?.let {
            CharSequenceText(
                text = it,
                fontSize = 14.sp,
                color = colorScheme.onSurfaceVariantDecreasedAlpha,
                modifier = Modifier.constrainAs(explanationRef) {
                    top.linkTo(labelRef.bottom, margin = ItemRowTokens.ExplanationTopPadding)
                    centerHorizontallyTo(labelRef)
                    width = Dimension.fillToConstraints
                }
            )
        }
    }
}

@Composable
fun ItemRowIcon(@DrawableRes res: Int, modifier: Modifier = Modifier, tint: Color = LocalContentColor.current) {
    Icon(
        painter = painterResource(id = res),
        modifier = modifier,
        contentDescription = null,
        tint = tint
    )
}

@Composable
fun SwitchItemRow(
    icon: @Composable () -> Unit,
    @StringRes labelRes: Int,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    explanation: CharSequence? = null
) {
    ItemRow(
        icon = icon,
        labelRes = labelRes,
        modifier = modifier,
        explanation = explanation
    ) {
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            modifier = Modifier.padding(start = ItemRowTokens.SwitchStartPadding)
        )
    }
}

@Preview
@Composable
private fun SwitchItemRowPrev() {
    AppTheme {
        Surface {
            SwitchItemRow(
                icon = { ItemRowIcon(R.drawable.ic_palette_24) },
                labelRes = R.string.appearance,
                checked = true,
                onCheckedChange = {},
                explanation = stringResource(R.string.show_storage_volume_names_explanation)
            )
        }
    }
}
