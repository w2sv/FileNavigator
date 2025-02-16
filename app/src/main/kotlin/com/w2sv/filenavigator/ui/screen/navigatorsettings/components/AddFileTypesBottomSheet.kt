package com.w2sv.filenavigator.ui.screen.navigatorsettings.components

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsIgnoringVisibility
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.runtime.snapshots.SnapshotStateMap
import androidx.compose.runtime.toMutableStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.w2sv.composed.extensions.thenIf
import com.w2sv.composed.extensions.toMutableStateMap
import com.w2sv.domain.model.FileType
import com.w2sv.filenavigator.R
import com.w2sv.filenavigator.ui.designsystem.AppCardDefaults
import com.w2sv.filenavigator.ui.designsystem.DialogButton
import com.w2sv.filenavigator.ui.designsystem.FileTypeIcon
import com.w2sv.kotlinutils.toggle
import kotlinx.collections.immutable.ImmutableList
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AddFileTypesBottomSheet(
    disabledFileTypes: ImmutableList<FileType>,
    addFileTypes: (List<FileType>) -> Unit,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    sheetState: SheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
    scope: CoroutineScope = rememberCoroutineScope()
) {
    val selectionMap = remember(disabledFileTypes) {
        EnabledKeysTrackingSnapshotStateMap(
            disabledFileTypes
                .associateWith { false }
                .toMutableStateMap()
        )
    }

    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = sheetState,
        modifier = modifier
    ) {
        Text(
            text = stringResource(R.string.add_file_types),
            fontSize = 22.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier
                .padding(bottom = 20.dp)
                .align(Alignment.CenterHorizontally)
        )
        LazyVerticalGrid(
            columns = GridCells.FixedSize(92.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterHorizontally)
        ) {
            items(disabledFileTypes) { fileType ->
                FileTypeCard(
                    fileType = fileType,
                    isSelected = selectionMap.getValue(fileType),
                    onClick = { selectionMap.toggle(fileType) },
                    modifier = Modifier
                        .padding(bottom = 12.dp)
                )
            }
        }
        DialogButton(
            text = pluralStringResource(
                id = R.plurals.add_file_types_button,
                count = selectionMap.enabledKeys.size
            ),
            onClick = remember {
                {
                    addFileTypes(selectionMap.enabledKeys)
                    scope.launch {
                        sheetState.hide()
                        onDismissRequest()
                    }
                }
            },
            enabled = selectionMap.enabledKeys.isNotEmpty(),
            modifier = Modifier
                .padding(end = 16.dp)
                .animateContentSize()
                .padding(horizontal = 8.dp)
                .align(Alignment.End)
                .padding(bottom = 12.dp) // To prevent elevation shadow cutoff
        )
        Spacer(modifier = Modifier.windowInsetsPadding(WindowInsets.navigationBarsIgnoringVisibility))
    }
}

@Stable
private class EnabledKeysTrackingSnapshotStateMap<K>(private val map: SnapshotStateMap<K, Boolean> = mutableStateMapOf()) :
    MutableMap<K, Boolean> by map {

    val enabledKeys: SnapshotStateList<K> = keys.filter { getValue(it) }.toMutableStateList()

    override fun put(key: K, value: Boolean): Boolean? =
        map.put(key, value)
            .also {
                if (value) {
                    enabledKeys.add(key)
                } else {
                    enabledKeys.remove(key)
                }
            }
}

@Composable
private fun FileTypeCard(
    fileType: FileType,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    ElevatedCard(
        modifier = modifier
            .thenIf(isSelected) {
                border(
                    color = MaterialTheme.colorScheme.primary,
                    width = 3.dp,
                    shape = MaterialTheme.shapes.medium
                )
            },
        elevation = AppCardDefaults.elevation
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxSize()
                .clickable { onClick() }
                .padding(12.dp)
        ) {
            FileTypeIcon(fileType, modifier = Modifier.size(46.dp))
            Spacer(modifier = Modifier.height(6.dp))
            Text(text = stringResource(id = fileType.labelRes))
        }
    }
}
