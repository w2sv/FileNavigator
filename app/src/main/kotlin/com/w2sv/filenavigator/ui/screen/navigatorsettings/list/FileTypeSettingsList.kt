package com.w2sv.filenavigator.ui.screen.navigatorsettings.list

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.w2sv.core.common.R
import com.w2sv.domain.model.filetype.FileType
import com.w2sv.domain.model.navigatorconfig.NavigatorConfig
import com.w2sv.filenavigator.ui.screen.navigatorsettings.list.navigatorconfigactions.NavigatorConfigActions
import kotlinx.collections.immutable.toImmutableMap

fun LazyListScope.fileTypeSettingsList(
    config: NavigatorConfig,
    fileTypeConfigActions: NavigatorConfigActions,
    showFileTypesBottomSheet: () -> Unit,
    showFileTypeConfigurationDialog: (FileType) -> Unit
) {
    item {
        NavigatorSettingsListSectionHeader(
            text = stringResource(id = R.string.navigated_file_types),
            padding = PaddingValues(top = 8.dp, bottom = 4.dp),
            endContent = {
                FilledTonalIconButton(onClick = showFileTypesBottomSheet) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = stringResource(R.string.configure_the_used_file_types)
                    )
                }
            }
        )
    }
    items(config.sortedEnabledFileTypes, key = { it.ordinal }) { fileType ->
        FileTypeSettingsPanel(
            fileType = fileType,
            setSourceAutoMoveConfigs = if (fileType.isMediaType) {
                { autoMoveConfig ->
                    fileTypeConfigActions.setAutoMoveConfigs(fileType = fileType, config = autoMoveConfig)
                }
            } else {
                null
            },
            sourceTypeConfigMap = config.fileTypeConfig(fileType).sourceTypeConfigMap.toImmutableMap(),
            onSourceCheckedChange = { sourceType, checkedNew ->
                fileTypeConfigActions.setSourceTypeEnablement(
                    fileType = fileType,
                    sourceType = sourceType,
                    enabled = checkedNew
                )
            },
            setSourceAutoMoveConfig = { sourceType, autoMoveConfig ->
                fileTypeConfigActions.setAutoMoveConfig(
                    fileType = fileType,
                    sourceType = sourceType,
                    config = autoMoveConfig
                )
            },
            showFileTypeConfigurationDialog = showFileTypeConfigurationDialog,
            modifier = Modifier.animateItem()
        )
    }
}
