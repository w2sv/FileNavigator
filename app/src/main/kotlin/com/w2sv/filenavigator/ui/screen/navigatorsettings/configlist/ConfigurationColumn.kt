package com.w2sv.filenavigator.ui.screen.navigatorsettings.configlist

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.w2sv.core.common.R
import com.w2sv.domain.model.filetype.FileType
import com.w2sv.domain.model.filetype.SourceType
import com.w2sv.domain.model.navigatorconfig.AutoMoveConfig
import com.w2sv.domain.model.navigatorconfig.NavigatorConfig
import com.w2sv.filenavigator.ui.designsystem.DefaultItemRowIcon
import com.w2sv.filenavigator.ui.designsystem.Padding
import com.w2sv.filenavigator.ui.designsystem.SwitchItemRow
import com.w2sv.filenavigator.ui.state.ReversibleNavigatorConfig
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableMap
import kotlinx.coroutines.flow.update
import slimber.log.i

private val verticalPadding = 16.dp

@Stable
class FileTypeActions(private val reversibleConfig: ReversibleNavigatorConfig) {
    fun toggleSource(
        fileType: FileType,
        sourceType: SourceType,
        enabled: Boolean
    ) {
        reversibleConfig.onFileSourceCheckedChange(
            fileType = fileType,
            sourceType = sourceType,
            checkedNew = enabled
        )
    }

    fun setAutoMoveConfig(
        fileType: FileType,
        sourceType: SourceType,
        config: AutoMoveConfig
    ) {
        reversibleConfig.update { it.updateAutoMoveConfig(fileType, sourceType) { config } }
    }

    fun setAutoMoveConfigs(
        fileType: FileType,
        config: AutoMoveConfig
    ) {
        reversibleConfig.update {
            it.updateAutoMoveConfigs(
                fileType = fileType,
                autoMoveConfig = config
            )
        }
    }
}

@Composable
fun NavigatorConfigurationColumn(
    config: NavigatorConfig,
    reversibleConfig: ReversibleNavigatorConfig,
    showFileTypesBottomSheet: () -> Unit,
    showFileTypeConfigurationDialog: (FileType) -> Unit,
    modifier: Modifier = Modifier
) {
    val fileTypeActions = remember(reversibleConfig) {
        i { "remembering fileTypeActions" }
        FileTypeActions(reversibleConfig)
    }
//    val moreItems = rememberMoreItems(config, reversibleConfig)

    LazyColumn(modifier = modifier) {
        item { FileTypeConfigurationHeader(modifier = Modifier.fillMaxWidth(), onEditButtonClick = showFileTypesBottomSheet) }
        items(config.sortedEnabledFileTypes, key = { it.ordinal }) { fileType ->
            i { "Laying out ${fileType.logIdentifier}" }

            FileTypeAccordion(
                fileType = fileType,
                setSourceAutoMoveConfigs = if (fileType.isMediaType) { autoMoveConfig ->
                    fileTypeActions.setAutoMoveConfigs(fileType, autoMoveConfig)
                } else null,
                sourceTypeConfigMap = config.fileTypeConfig(fileType).sourceTypeConfigMap.toImmutableMap(),
                onSourceCheckedChange = { sourceType, checkedNew -> fileTypeActions.toggleSource(fileType, sourceType, checkedNew) },
                setSourceAutoMoveConfig = { sourceType, autoMoveConfig ->
                    fileTypeActions.setAutoMoveConfig(
                        fileType,
                        sourceType,
                        autoMoveConfig
                    )
                },
                showFileTypeConfigurationDialog = showFileTypeConfigurationDialog,
                modifier = Modifier
                    .padding(vertical = 4.dp)
                    .animateItem()
            )
        }
//        MoreColumn(moreItems)
    }
}

@Composable
private fun FileTypeConfigurationHeader(modifier: Modifier = Modifier, onEditButtonClick: () -> Unit) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        SectionHeader(text = stringResource(id = R.string.navigated_file_types))
        FilledTonalIconButton(onClick = onEditButtonClick) {
            Icon(
                imageVector = Icons.Default.Edit,
                contentDescription = stringResource(R.string.configure_the_used_file_types)
            )
        }
    }
}

@Composable
private fun SectionHeader(text: String, modifier: Modifier = defaultSectionHeaderModifier) {
    Text(
        text = text,
        style = MaterialTheme.typography.headlineMedium,
        modifier = modifier
    )
}

private val defaultSectionHeaderModifier = Modifier.padding(vertical = verticalPadding)

@Composable
private fun rememberMoreItems(config: NavigatorConfig, reversibleConfig: ReversibleNavigatorConfig) =
    remember {
        persistentListOf(
            SwitchItemRowData(
                iconRes = R.drawable.ic_files_24,
                labelRes = R.string.show_batch_move_notification,
                checked = { config.showBatchMoveNotification },
                onCheckedChange = { checked -> reversibleConfig.update { it.copy(showBatchMoveNotification = checked) } },
                explanationRes = R.string.batch_move_explanation
            ),
            SwitchItemRowData(
                iconRes = R.drawable.ic_battery_low_24,
                labelRes = R.string.disable_navigator_on_low_battery,
                checked = { config.disableOnLowBattery },
                onCheckedChange = { checked -> reversibleConfig.update { it.copy(disableOnLowBattery = checked) } }
            ),
            SwitchItemRowData(
                iconRes = R.drawable.ic_restart_24,
                labelRes = R.string.start_navigator_on_system_boot,
                checked = { config.startOnBoot },
                onCheckedChange = { checked -> reversibleConfig.update { it.copy(startOnBoot = checked) } }
            )
        )
    }

private fun LazyListScope.MoreColumn(items: ImmutableList<SwitchItemRowData>) {
    item { SectionHeader(text = stringResource(id = R.string.more)) }
    items(items, key = { it.iconRes }) { item ->
        SwitchItemRow(
            icon = { DefaultItemRowIcon(res = item.iconRes) },
            labelRes = item.labelRes,
            checked = item.checked(),
            onCheckedChange = item.onCheckedChange,
            explanation = item.explanationRes?.let { stringResource(it) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp)
        )
    }
    item { Spacer(Modifier.height(Padding.fabButtonBottomPadding)) }
}

@Immutable
data class SwitchItemRowData(
    @DrawableRes val iconRes: Int,
    @StringRes val labelRes: Int,
    val checked: () -> Boolean,
    val onCheckedChange: (Boolean) -> Unit,
    @StringRes val explanationRes: Int? = null
)
