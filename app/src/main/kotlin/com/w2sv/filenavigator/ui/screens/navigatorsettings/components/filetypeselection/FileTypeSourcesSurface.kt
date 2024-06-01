package com.w2sv.filenavigator.ui.screens.navigatorsettings.components.filetypeselection

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
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
import com.w2sv.domain.model.navigatorconfig.FileTypeConfig
import com.w2sv.filenavigator.ui.model.color
import com.w2sv.filenavigator.ui.utils.orOnSurfaceDisabledIf
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toPersistentList

@Composable
fun FileTypeSourcesSurface(
    fileType: FileType,
    sourceTypes: ImmutableList<SourceType>,
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
            elements = sourceTypes,
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
            Checkbox(
                checked = isEnabled,
                onCheckedChange = onCheckedChange
            )
        }
    }
}