package com.w2sv.filenavigator.ui.screens.navigatorsettings.components.filetypeselection

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
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
import com.w2sv.filenavigator.ui.utils.orDisabledIf
import kotlinx.collections.immutable.toImmutableList

@Composable
fun FileTypeSourcesSurface(
    fileType: FileType,
    mediaFileSourceEnabled: (FileType.Source) -> Boolean,
    setMediaFileSourceEnabled: (FileType.Source, Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(8.dp),
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
        // Icon
        Box(modifier = Modifier.weight(0.2f), contentAlignment = Alignment.Center) {
            Icon(
                painter = painterResource(id = source.kind.iconRes),
                contentDescription = null,
                tint = source.fileType.color
                    .orDisabledIf(condition = !isEnabled)
            )
        }
        // Label
        Box(modifier = Modifier.weight(0.5f), contentAlignment = Alignment.CenterStart) {
            Text(
                text = stringResource(id = source.kind.labelRes),
                color = MaterialTheme.colorScheme.onSurface
                    .orDisabledIf(condition = !isEnabled)
            )
        }

        Box(modifier = Modifier.weight(0.15f), contentAlignment = Alignment.Center) {
            if (source.fileType.isMediaType) {
                Checkbox(
                    checked = isEnabled,
                    onCheckedChange = onCheckedChange
                )
            }
        }
    }
}