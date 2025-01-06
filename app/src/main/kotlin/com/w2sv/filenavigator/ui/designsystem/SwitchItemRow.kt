package com.w2sv.filenavigator.ui.designsystem

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
                Explanation(
                    text = it,
                    modifier = Modifier.padding(top = ItemRowDefaults.ExplanationTopPadding)
                )
            }
        }
        content()
    }
}

@Composable
private fun Explanation(text: CharSequence, modifier: Modifier = Modifier) {
    CharSequenceText(
        text = text,
        color = MaterialTheme.colorScheme.onSurfaceVariantDecreasedAlpha,
        modifier = modifier,
        fontSize = 14.sp
    )
}

@Composable
fun DefaultItemRowIcon(
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
        UnpaddedSwitch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            modifier = Modifier.padding(start = ItemRowDefaults.SwitchStartPadding)
        )
    }
}
