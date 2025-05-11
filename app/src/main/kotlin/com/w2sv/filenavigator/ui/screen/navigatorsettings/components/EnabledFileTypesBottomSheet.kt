package com.w2sv.filenavigator.ui.screen.navigatorsettings.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsIgnoringVisibility
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyGridItemScope
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Text
import androidx.compose.material3.TooltipBox
import androidx.compose.material3.TooltipDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.rememberTooltipState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.mapSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.snapshots.SnapshotStateMap
import androidx.compose.runtime.toMutableStateMap
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.w2sv.common.util.syncMapKeys
import com.w2sv.composed.OnChange
import com.w2sv.composed.extensions.thenIf
import com.w2sv.composed.extensions.toMutableStateMap
import com.w2sv.domain.model.filetype.CustomFileType
import com.w2sv.domain.model.filetype.FileType
import com.w2sv.domain.model.navigatorconfig.NavigatorConfig
import com.w2sv.domain.model.navigatorconfig.sortedByOrdinal
import com.w2sv.filenavigator.R
import com.w2sv.filenavigator.ui.designsystem.AppSnackbarContent
import com.w2sv.filenavigator.ui.designsystem.DeletionTooltip
import com.w2sv.filenavigator.ui.designsystem.FileTypeIcon
import com.w2sv.filenavigator.ui.designsystem.HighlightedDialogButton
import com.w2sv.filenavigator.ui.designsystem.SnackbarKind
import com.w2sv.filenavigator.ui.modelext.stringResource
import com.w2sv.filenavigator.ui.theme.AppTheme
import com.w2sv.kotlinutils.toggle
import kotlinx.collections.immutable.ImmutableMap
import kotlinx.collections.immutable.toImmutableMap
import kotlinx.coroutines.launch

@Composable
fun EnabledFileTypesBottomSheet(
    fileTypeEnablementMap: ImmutableMap<FileType, Boolean>,
    applyFileTypeEnablementMap: (Map<FileType, Boolean>) -> Unit,
    deleteCustomFileType: (CustomFileType) -> Unit,
    showFileTypeCreationDialog: () -> Unit,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    sheetState: SheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
) {
    val mutableFileTypeEnablementMap = rememberSaveable(saver = fileTypeEnablementMapSaver(fileTypeEnablementMap.keys)) {
        fileTypeEnablementMap.toMutableStateMap()
    }
    val sortedFileTypes by remember { derivedStateOf { mutableFileTypeEnablementMap.keys.sortedByOrdinal() } }
    val enablementMapsUnequal by remember(fileTypeEnablementMap) {
        derivedStateOf { mutableFileTypeEnablementMap.toMap() != fileTypeEnablementMap }
    }
    val allFileTypesUnselected by remember { derivedStateOf { mutableFileTypeEnablementMap.values.all { !it } } }

    OnChange(fileTypeEnablementMap) {
        syncMapKeys(
            source = fileTypeEnablementMap,
            target = mutableFileTypeEnablementMap,
            valueOnAddedKeys = true
        )
    }

    val fileTypeCard: @Composable LazyGridItemScope.(FileType) -> Unit = remember {
        { fileType ->
            FileTypeCard(
                fileType = fileType,
                isSelected = mutableFileTypeEnablementMap.getValue(fileType),
                onClick = { mutableFileTypeEnablementMap.toggle(fileType) }
            )
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = sheetState,
        modifier = modifier
    ) {
        Text(
            text = stringResource(R.string.configure_navigated_file_types),
            fontSize = 22.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier
                .padding(bottom = 20.dp)
                .align(Alignment.CenterHorizontally)
        )
        AnimatedVisibility(allFileTypesUnselected) {
            AppSnackbarContent(
                snackbarKind = SnackbarKind.Error,
                message = stringResource(R.string.select_at_least_one_file_type),
                modifier = Modifier
                    .padding(vertical = 8.dp)
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            )
        }
        val scope = rememberCoroutineScope()
        LazyVerticalGrid(
            columns = GridCells.FixedSize(92.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterHorizontally)
        ) {
            items(sortedFileTypes, key = { it.ordinal }) { fileType ->
                if (fileType is CustomFileType) {
                    DeleteCustomFileTypeTooltipBox(deleteCustomFileType = { deleteCustomFileType(fileType) }) {
                        fileTypeCard(fileType)
                    }
                } else {
                    fileTypeCard(fileType)
                }
            }
            item {
                CreateFileTypeCard(onClick = showFileTypeCreationDialog)
            }
        }
        HighlightedDialogButton(
            text = stringResource(R.string.apply),
            onClick = remember {
                {
                    applyFileTypeEnablementMap(mutableFileTypeEnablementMap)
                    scope.launch {
                        sheetState.hide()
                        onDismissRequest()
                    }
                }
            },
            enabled = enablementMapsUnequal && !allFileTypesUnselected,
            modifier = Modifier
                .padding(end = 16.dp)
                .padding(horizontal = 8.dp)
                .align(Alignment.End)
                .padding(bottom = 12.dp) // To prevent elevation shadow cutoff
        )
        Spacer(modifier = Modifier.windowInsetsPadding(WindowInsets.navigationBarsIgnoringVisibility))
    }
}

private fun fileTypeEnablementMapSaver(fileTypes: Set<FileType>): Saver<SnapshotStateMap<FileType, Boolean>, Any> =
    mapSaver(
        save = { map -> map.mapKeys { it.key.ordinal.toString() } },
        restore = { restored ->
            restored
                .map { (ordinalString, isEnabled) ->
                    val ordinal = ordinalString.toInt()
                    fileTypes.first { it.ordinal == ordinal } to isEnabled as Boolean
                }
                .toMutableStateMap()
        }
    )

@Composable
private fun DeleteCustomFileTypeTooltipBox(deleteCustomFileType: () -> Unit, content: @Composable () -> Unit) {
    val tooltipState = rememberTooltipState()

    TooltipBox(
        positionProvider = TooltipDefaults.rememberPlainTooltipPositionProvider(),
        tooltip = {
            DeletionTooltip(
                onClick = {
                    deleteCustomFileType()
                    tooltipState.dismiss()
                },
                contentDescription = stringResource(R.string.delete_the_file_type)
            )
        },
        state = tooltipState,
        content = content
    )
}

@Composable
private fun LazyGridItemScope.FileTypeCard(
    fileType: FileType,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    GridCard(isSelected = isSelected, onClick = onClick, modifier = modifier) {
        FileTypeIcon(fileType, modifier = Modifier.size(46.dp))
        Spacer(modifier = Modifier.height(6.dp))
        Text(text = fileType.stringResource())
    }
}

@Composable
private fun LazyGridItemScope.CreateFileTypeCard(onClick: () -> Unit, modifier: Modifier = Modifier) {
    GridCard(isSelected = false, onClick = onClick, modifier = modifier) {
        Icon(
            imageVector = Icons.Default.Add,
            contentDescription = null,
            modifier = Modifier.size(32.dp)
        )
        Text(stringResource(R.string.create_file_type_card_label), textAlign = TextAlign.Center)
    }
}

@Composable
private fun LazyGridItemScope.GridCard(
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    ElevatedCard(
        modifier = modifier
            .padding(bottom = 12.dp)
            .animateItem()
            .thenIf(isSelected) {
                border(
                    color = MaterialTheme.colorScheme.primary,
                    width = 3.dp,
                    shape = MaterialTheme.shapes.medium
                )
            },
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHighest)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxSize()
                .clickable { onClick() }
                .padding(12.dp),
            content = content
        )
    }
}

@Preview
@Composable
private fun UsedFileTypesBottomSheetPrev() {
    AppTheme {
        EnabledFileTypesBottomSheet(
            fileTypeEnablementMap = NavigatorConfig.default.fileTypeConfigMap.mapValues { it.value.enabled }.toImmutableMap(),
            applyFileTypeEnablementMap = {},
            deleteCustomFileType = {},
            showFileTypeCreationDialog = {},
            onDismissRequest = {},
            sheetState = SheetState(initialValue = SheetValue.Expanded, skipPartiallyExpanded = true, density = LocalDensity.current)
        )
    }
}
