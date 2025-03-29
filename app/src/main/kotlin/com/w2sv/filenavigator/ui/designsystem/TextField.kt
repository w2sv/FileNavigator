package com.w2sv.filenavigator.ui.designsystem

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Warning
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.w2sv.filenavigator.ui.theme.AppColor
import com.w2sv.filenavigator.ui.util.TextEditor

/**
 * An [OutlinedTextField] based on a [TextEditor].
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun OutlinedTextField(
    editor: TextEditor<*>,
    placeholderText: String,
    onApply: () -> Unit,
    modifier: Modifier = Modifier,
    labelText: String? = null,
    applyIconImageVector: ImageVector = Icons.Outlined.Check,
    errorColor: Color = AppColor.error,
    showApplyIconOnlyWhenFocused: Boolean = true,
    showDisabledApplyButtonWhenEmpty: Boolean = false,
    actionButton: (@Composable () -> Unit)? = null
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()

    OutlinedTextField(
        value = editor.getValue(),
        onValueChange = { editor.update(it) },
        textStyle = MaterialTheme.typography.bodyLarge,
        placeholder = { Text(placeholderText, maxLines = 1) },
        label = labelText?.let { { Text(labelText, maxLines = 1) } },
        singleLine = true,
        modifier = modifier,
        trailingIcon = when {
            editor.invalidityReason != null -> {
                {
                    Icon(Icons.Outlined.Warning, contentDescription = null, tint = errorColor)
                }
            }

            editor.isValid && (!showApplyIconOnlyWhenFocused || isFocused) || showDisabledApplyButtonWhenEmpty -> {
                {
                    FilledTonalIconButton(
                        onClick = onApply,
                        modifier = Modifier.padding(end = 4.dp),
                        enabled = editor.isValid,
                        colors = IconButtonDefaults.filledTonalIconButtonColors(contentColor = AppColor.success)
                    ) {
                        Icon(applyIconImageVector, contentDescription = null)
                    }
                }
            }

            else -> {
                null
            }
        },
        isError = editor.invalidityReason != null,
        supportingText = editor.invalidityReason?.let { invalidityReason ->
            {
                FlowRow {
                    Text(
                        text = invalidityReason.text(),
                        color = errorColor
                    )
                    actionButton?.invoke()
                }
            }
        },
        interactionSource = interactionSource
    )
}
