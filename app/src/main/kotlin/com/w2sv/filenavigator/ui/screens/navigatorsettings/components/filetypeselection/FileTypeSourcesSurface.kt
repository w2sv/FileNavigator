package com.w2sv.filenavigator.ui.screens.navigatorsettings.components.filetypeselection

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.w2sv.composed.InterElementDividedColumn
import com.w2sv.domain.model.FileType
import com.w2sv.domain.model.SourceType
import com.w2sv.filenavigator.R
import com.w2sv.filenavigator.ui.designsystem.drawer.AutoMoveIcon
import com.w2sv.filenavigator.ui.model.color
import com.w2sv.filenavigator.ui.utils.orOnSurfaceDisabledIf

@Composable
fun FileTypeSourcesSurface(
    fileType: FileType,
    mediaFileSourceEnabled: (SourceType) -> Boolean,
    setMediaFileSourceEnabled: (SourceType, Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = MaterialTheme.shapes.small,
        tonalElevation = 8.dp,
        modifier = modifier.fillMaxWidth()
    ) {
        InterElementDividedColumn(
            elements = fileType.sourceTypes,
            makeElement = { sourceType ->
                SourceRow(
                    fileType = fileType,
                    sourceType = sourceType,
                    isEnabled = mediaFileSourceEnabled(sourceType),
                    onCheckedChange = { setMediaFileSourceEnabled(sourceType, it) },
                    modifier = Modifier.height(44.dp)
                )
            }
        )
    }
}

@Composable
private fun SourceRow(
    fileType: FileType,
    sourceType: SourceType,
    isEnabled: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
        modifier = modifier
    ) {
        Icon(
            painter = painterResource(id = sourceType.iconRes),
            contentDescription = null,
            tint = fileType.color
                .orOnSurfaceDisabledIf(condition = !isEnabled),
            modifier = Modifier.padding(start = 26.dp, end = 18.dp)
        )
        Text(
            text = stringResource(id = sourceType.labelRes),
            color = MaterialTheme.colorScheme.onSurface
                .orOnSurfaceDisabledIf(condition = !isEnabled)
        )
        if (fileType.isMediaType) {
            Spacer(modifier = Modifier.weight(1f))
            AnimatedVisibility(visible = isEnabled) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    AutoMoveIcon(
                        modifier = Modifier.padding(end = 12.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    IconButton(onClick = { /*TODO*/ }, modifier = Modifier.width(24.dp)) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_more_vert_24),
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                        )
                    }
                }
            }
            Checkbox(
                checked = isEnabled,
                onCheckedChange = onCheckedChange
            )
        }
    }
}