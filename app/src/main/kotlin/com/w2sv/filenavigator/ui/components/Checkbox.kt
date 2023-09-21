package com.w2sv.filenavigator.ui.components

import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun AppCheckbox(
    checked: Boolean,
    onCheckedChange: ((Boolean) -> Unit)?,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    Checkbox(
        checked = checked,
        onCheckedChange = onCheckedChange,
        modifier = modifier,
        enabled = enabled,
        colors = CheckboxDefaults.colors(checkedColor = MaterialTheme.colorScheme.secondary)
    )
}

@Composable
fun RowScope.RightAlignedAppCheckbox(checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Spacer(modifier = Modifier.weight(1f))
    AppCheckbox(
        checked = checked,
        onCheckedChange = onCheckedChange,
    )
}