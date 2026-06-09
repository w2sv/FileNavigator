package com.w2sv.designsystem.component

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import com.w2sv.designsystem.theme.AppTheme
import com.w2sv.designsystem.theme.onSurfaceVariantDecreasedAlpha
import com.w2sv.modules.common.R

object ItemLayoutDimens {
    val IconTextSpacing = 16.dp
    val SwitchStartPadding = 8.dp
    val ExplanationTopPadding = 2.dp
    val ExplanationFontSize = 14.sp
}

@Composable
fun ItemLayout(
    icon: @Composable () -> Unit,
    @StringRes labelRes: Int,
    modifier: Modifier = Modifier,
    explanation: CharSequence? = null,
    trailingContent: (@Composable () -> Unit)? = null
) {
    ConstraintLayout(modifier.fillMaxWidth()) {
        val (iconRef, labelRef, contentRef, explanationRef) = createRefs()

        Box(
            modifier = Modifier.constrainAs(iconRef) {
                start.linkTo(parent.start)
                if (trailingContent != null) {
                    centerVerticallyTo(contentRef)
                } else {
                    top.linkTo(parent.top)
                }
            },
            content = { icon() }
        )

        Text(
            text = stringResource(id = labelRes),
            modifier = Modifier.constrainAs(labelRef) {
                if (trailingContent != null) {
                    centerVerticallyTo(contentRef)
                    linkTo(iconRef.end, contentRef.start, startMargin = ItemLayoutDimens.IconTextSpacing)
                    width = Dimension.fillToConstraints
                } else {
                    start.linkTo(iconRef.end, margin = ItemLayoutDimens.IconTextSpacing)
                    centerVerticallyTo(iconRef)
                }
            }
        )

        trailingContent?.let {
            Box(
                modifier = Modifier.constrainAs(contentRef) {
                    end.linkTo(parent.end)
                    top.linkTo(parent.top)
                },
                content = { it() }
            )
        }

        explanation?.let {
            CharSequenceText(
                text = it,
                fontSize = ItemLayoutDimens.ExplanationFontSize,
                color = colorScheme.onSurfaceVariantDecreasedAlpha,
                modifier = Modifier.constrainAs(explanationRef) {
                    if (trailingContent != null) {
                        top.linkTo(labelRef.bottom, margin = ItemLayoutDimens.ExplanationTopPadding)
                        centerHorizontallyTo(labelRef)
                    } else {
                        top.linkTo(labelRef.bottom)
                        linkTo(labelRef.start, parent.end)
                    }
                    width = Dimension.fillToConstraints
                }
            )
        }
    }
}

@Composable
fun SwitchItemLayout(
    icon: @Composable () -> Unit,
    @StringRes labelRes: Int,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    explanation: CharSequence? = null
) {
    ItemLayout(
        icon = icon,
        labelRes = labelRes,
        modifier = modifier,
        explanation = explanation
    ) {
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            modifier = Modifier.padding(start = ItemLayoutDimens.SwitchStartPadding)
        )
    }
}

@Preview
@Composable
private fun SwitchItemLayoutPrev() {
    AppTheme {
        Surface {
            SwitchItemLayout(
                icon = { Icon(R.drawable.ic_palette_24) },
                labelRes = R.string.appearance,
                checked = true,
                onCheckedChange = {},
                explanation = stringResource(R.string.show_storage_volume_names_explanation)
            )
        }
    }
}
