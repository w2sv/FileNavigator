package com.w2sv.filenavigator.ui.screen.navigatorsettings.components.filetypeconfiguration

import androidx.annotation.DrawableRes
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.FlowRowScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.InputChip
import androidx.compose.material3.InputChipDefaults
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SelectableChipColors
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.w2sv.common.util.colonSuffixed
import com.w2sv.filenavigator.ui.designsystem.DialogButton
import com.w2sv.filenavigator.ui.designsystem.HighlightedDialogButton
import com.w2sv.filenavigator.ui.theme.dialogSectionLabel
import com.w2sv.filenavigator.ui.util.CharSequenceText

@Composable
fun ColorPickerDialogOverlaidFileTypeConfigurationDialog(
    fileTypeColor: Color,
    applyColor: (Color) -> Unit,
    fileTypeConfigurationDialog: @Composable (() -> Unit) -> Unit
) {
    var showColorPickerDialog by rememberSaveable {
        mutableStateOf(false)
    }

    fileTypeConfigurationDialog { showColorPickerDialog = true }

    if (showColorPickerDialog) {
        ColorPickerDialog(
            initialColor = fileTypeColor,
            applyColor = applyColor,
            onDismissRequest = { showColorPickerDialog = false }
        )
    }
}

@Composable
fun FileTypeConfigurationDialog(
    @DrawableRes icon: Int,
    title: String,
    onConfirmButtonPress: () -> Unit,
    onDismissRequest: () -> Unit,
    fileTypeColor: Color,
    onConfigureColorButtonPress: () -> Unit,
    modifier: Modifier = Modifier,
    iconTint: Color = LocalContentColor.current,
    confirmButtonEnabled: Boolean = true,
    confirmButtonText: String = stringResource(com.w2sv.filenavigator.R.string.save),
    content: @Composable ColumnScope.() -> Unit
) {
    AlertDialog(
        modifier = modifier,
        icon = { Icon(painterResource(icon), contentDescription = null, modifier = Modifier.size(44.dp), tint = iconTint) },
        title = { Text(title) },
        onDismissRequest = onDismissRequest,
        text = {
            Column(
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .animateContentSize()
                    .padding(8.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                content()
                ColorRow(color = fileTypeColor, onConfigureColorButtonPress = onConfigureColorButtonPress)
            }
        },
        confirmButton = {
            HighlightedDialogButton(
                text = confirmButtonText,
                onClick = {
                    onConfirmButtonPress()
                    onDismissRequest()
                },
                enabled = confirmButtonEnabled
            )
        },
        dismissButton = { DialogButton(text = stringResource(com.w2sv.core.navigator.R.string.cancel), onClick = onDismissRequest) }
    )
}

@Composable
fun FileExtensionsChipFlowRow(modifier: Modifier = Modifier, content: @Composable FlowRowScope.() -> Unit) {
    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(1.dp),
        modifier = modifier,
        content = content
    )
}

@Composable
fun FileExtensionChip(
    extension: String,
    modifier: Modifier = Modifier,
    selected: Boolean = true,
    enabled: Boolean = true,
    colors: SelectableChipColors = InputChipDefaults.inputChipColors(),
    onClick: () -> Unit = {}
) {
    InputChip(
        selected = selected,
        enabled = enabled,
        onClick = onClick,
        modifier = modifier,
        label = {
            Text(
                text = extension,
                modifier = Modifier.padding(vertical = 6.dp),
                style = MaterialTheme.typography.bodyLarge
            )
        },
        colors = colors
    )
}

@Composable
private fun ColorRow(
    color: Color,
    onConfigureColorButtonPress: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxSize()
            .padding(bottom = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = stringResource(com.w2sv.filenavigator.R.string.color).colonSuffixed(),
            style = MaterialTheme.typography.dialogSectionLabel
        )
        Box(contentAlignment = Alignment.Center) {
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(CircleShape)
                    .border(Dp.Hairline, MaterialTheme.colorScheme.outline, CircleShape)
                    .background(color)
            )
            FilledTonalIconButton(
                onClick = onConfigureColorButtonPress,
                colors = IconButtonDefaults.filledTonalIconButtonColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(
                        alpha = 0.7f
                    )
                ),
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .offset(8.dp, 8.dp)
                    .border(Dp.Hairline, MaterialTheme.colorScheme.outline, CircleShape)
                    .size(32.dp)
            ) {
                Icon(Icons.Default.Settings, contentDescription = null, modifier = Modifier.size(24.dp))
            }
        }
    }
}

@Composable
fun RemoveExtensionFromFileTypeButtonRow(
    text: CharSequence,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center
    ) {
        Button(
            colors = ButtonDefaults.filledTonalButtonColors(
                containerColor = MaterialTheme.colorScheme.errorContainer,
                contentColor = MaterialTheme.colorScheme.onErrorContainer
            ),
            onClick = onClick,
            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 0.dp)
        ) {
            CharSequenceText(text)
        }
    }
}
