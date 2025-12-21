package com.w2sv.filenavigator.ui.designsystem

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.w2sv.filenavigator.ui.theme.AppTheme
import com.w2sv.filenavigator.ui.theme.onSurfaceVariantDecreasedAlpha
import com.w2sv.filenavigator.ui.util.CharSequenceText

object ItemRowDefaults {
    val IconTextSpacing = 16.dp
    val SwitchStartPadding = 8.dp
    val ExplanationTopPadding = 2.dp
}

@Composable
fun ItemRow(
    icon: @Composable RowScope.() -> Unit,
    @StringRes labelRes: Int,
    modifier: Modifier = Modifier,
    explanation: CharSequence? = null,
    verticalAlignment: Alignment.Vertical = Alignment.Top,
    content: @Composable RowScope.() -> Unit
) {
    Row(
        verticalAlignment = verticalAlignment,
        modifier = modifier
    ) {
        icon()
        Spacer(modifier = Modifier.width(ItemRowDefaults.IconTextSpacing))
        Column(modifier = Modifier.weight(1f)) {
            Text(text = stringResource(id = labelRes))
            explanation?.let {
                CharSequenceText(
                    text = it,
                    color = MaterialTheme.colorScheme.onSurfaceVariantDecreasedAlpha,
                    modifier = Modifier.padding(top = ItemRowDefaults.ExplanationTopPadding),
                    fontSize = 14.sp
                )
            }
        }
        content()
    }
}

@Composable
fun ItemRowIcon(
    @DrawableRes res: Int,
    modifier: Modifier = Modifier,
    tint: Color = LocalContentColor.current
) {
    Icon(
        painter = painterResource(id = res),
        modifier = modifier,
        contentDescription = null,
        tint = tint
    )
}

@Composable
fun SwitchItemRow(
    icon: @Composable RowScope.() -> Unit,
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
            modifier = Modifier
                .padding(start = ItemRowDefaults.SwitchStartPadding)
                .offset(y = (-8).dp) // Circumvent inherent Switch top padding to align switch with the rest of the item row
        )
    }
}

@Preview
@Composable
private fun SwitchItemRowPrev() {
    AppTheme {
        Surface {
            SwitchItemRow(
                icon = { ItemRowIcon(com.w2sv.core.common.R.drawable.ic_palette_24) },
                labelRes = com.w2sv.core.common.R.string.appearance,
                checked = true,
                onCheckedChange = {},
                explanation = stringResource(com.w2sv.core.common.R.string.show_storage_volume_names_explanation)
            )
        }
    }
}
