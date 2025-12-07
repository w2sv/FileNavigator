package com.w2sv.filenavigator.ui.screen.navigatorsettings.components.filetypeconfiguration

import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import com.smarttoolfactory.colorpicker.picker.HSVColorPickerCircularWithSliders
import com.w2sv.composed.core.colorSaver
import com.w2sv.core.common.R
import com.w2sv.filenavigator.ui.designsystem.DialogButton
import com.w2sv.filenavigator.ui.designsystem.HighlightedDialogButton

@Composable
fun ColorPickerDialog(
    initialColor: Color,
    applyColor: (Color) -> Unit,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier
) {
    var color by rememberSaveable(stateSaver = colorSaver()) {
        mutableStateOf(initialColor)
    }

    AlertDialog(
        onDismissRequest = onDismissRequest,
        confirmButton = {
            HighlightedDialogButton(
                text = stringResource(R.string.apply),
                onClick = {
                    applyColor(color)
                    onDismissRequest()
                },
                enabled = color != initialColor
            )
        },
        dismissButton = { DialogButton(stringResource(R.string.cancel), onClick = onDismissRequest) },
        text = {
            HSVColorPickerCircularWithSliders(
                initialColor = color,
                onColorChange = { newColor, _ -> color = newColor },
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
            )
        },
        modifier = modifier
    )
}
