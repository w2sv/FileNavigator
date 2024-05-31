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
import com.w2sv.filenavigator.ui.model.color
import com.w2sv.filenavigator.ui.utils.orOnSurfaceDisabledIf
import kotlinx.collections.immutable.toImmutableList

@Composable
fun FileTypeSourcesSurface(
    fileType: FileType,
    mediaFileSourceEnabled: (FileType.Source) -> Boolean,
    setMediaFileSourceEnabled: (FileType.Source, Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = MaterialTheme.shapes.small,
        tonalElevation = 8.dp,
        modifier = modifier.fillMaxWidth()
    ) {
        InterElementDividedColumn(
            elements = fileType.sources.toImmutableList(),
            makeElement = { source ->
                SourceRow(
                    source = source,
                    isEnabled = mediaFileSourceEnabled(source),
                    onCheckedChange = { setMediaFileSourceEnabled(source, it) },
                    modifier = Modifier.height(44.dp)
                )
            }
        )
    }
}

@Composable
private fun SourceRow(
    source: FileType.Source,
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
            painter = painterResource(id = source.kind.iconRes),
            contentDescription = null,
            tint = source.fileType.color
                .orOnSurfaceDisabledIf(condition = !isEnabled),
            modifier = Modifier.padding(start = 26.dp, end = 18.dp)
        )
        Text(
            text = stringResource(id = source.kind.labelRes),
            color = MaterialTheme.colorScheme.onSurface
                .orOnSurfaceDisabledIf(condition = !isEnabled)
        )
        if (source.fileType.isMediaType) {
            Spacer(modifier = Modifier.weight(1f))
            Checkbox(
                checked = isEnabled,
                onCheckedChange = onCheckedChange
            )
        }
    }
}