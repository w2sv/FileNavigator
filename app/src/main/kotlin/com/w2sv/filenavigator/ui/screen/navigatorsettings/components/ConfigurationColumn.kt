package com.w2sv.filenavigator.ui.screen.navigatorsettings.components

import android.content.Context
import android.net.Uri
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.w2sv.common.util.logIdentifier
import com.w2sv.common.util.takePersistableReadAndWriteUriPermission
import com.w2sv.domain.model.CustomFileType
import com.w2sv.domain.model.movedestination.LocalDestination
import com.w2sv.domain.model.movedestination.LocalDestinationApi
import com.w2sv.domain.model.navigatorconfig.NavigatorConfig
import com.w2sv.domain.usecase.MoveDestinationPathConverter
import com.w2sv.filenavigator.R
import com.w2sv.filenavigator.ui.LocalMoveDestinationPathConverter
import com.w2sv.filenavigator.ui.designsystem.DefaultItemRowIcon
import com.w2sv.filenavigator.ui.designsystem.IconSize
import com.w2sv.filenavigator.ui.designsystem.Padding
import com.w2sv.filenavigator.ui.designsystem.Spacing
import com.w2sv.filenavigator.ui.designsystem.SwitchItemRow
import com.w2sv.filenavigator.ui.screen.navigatorsettings.components.filetypeselection.FileTypeAccordion
import com.w2sv.filenavigator.ui.state.ReversibleNavigatorConfig
import kotlinx.collections.immutable.toImmutableMap
import kotlinx.coroutines.flow.update
import slimber.log.i

private val verticalPadding = 16.dp

@Composable
fun SubDirectoryIcon(modifier: Modifier = Modifier, tint: Color = MaterialTheme.colorScheme.onSurfaceVariant) {
    Icon(
        painter = painterResource(id = R.drawable.ic_subdirectory_arrow_right_24),
        contentDescription = null,
        tint = tint,
        modifier = modifier
    )
}

@Composable
fun rememberAutoMoveDestinationPath(
    destination: LocalDestinationApi?,
    context: Context = LocalContext.current,
    moveDestinationPathConverter: MoveDestinationPathConverter = LocalMoveDestinationPathConverter.current
): State<String?> =
    remember(destination) {
        mutableStateOf(
            destination?.let { moveDestinationPathConverter.invoke(it, context) }
        )
    }

@Composable
fun AutoMoveRow(
    destinationPath: String,
    changeDestination: () -> Unit,
    modifier: Modifier = Modifier
) {
    CompositionLocalProvider(value = LocalContentColor provides MaterialTheme.colorScheme.onSurfaceVariant) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = modifier
                .padding(start = 10.dp, bottom = 4.dp)
        ) {
            SubDirectoryIcon(
                modifier = Modifier
                    .padding(end = 16.dp)
                    .size(20.dp)
            )
            Text(destinationPath, modifier = Modifier.weight(1f), fontSize = 14.sp)
            IconButton(
                onClick = { changeDestination() },
                modifier = Modifier.size(IconSize.Big)
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_folder_edit_24),
                    contentDescription = stringResource(R.string.select_the_auto_move_destination),
                    modifier = Modifier.size(IconSize.Big),
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
fun rememberSelectAutoMoveDestination(onDestinationSelected: (LocalDestinationApi) -> Unit): ManagedActivityResultLauncher<Uri?, Uri?> {
    val context: Context = LocalContext.current
    return rememberLauncherForActivityResult(contract = ActivityResultContracts.OpenDocumentTree()) { optionalTreeUri ->
        optionalTreeUri?.let { treeUri ->
            context.contentResolver.takePersistableReadAndWriteUriPermission(treeUri)
            onDestinationSelected(
                LocalDestination.fromTreeUri(
                    context = context,
                    treeUri = treeUri
                )!! // TODO: null case possible?
            )
        }
    }
}

@Composable
fun NavigatorConfigurationColumn(
    config: NavigatorConfig,
    reversibleConfig: ReversibleNavigatorConfig,
    showAddFileTypesBottomSheet: () -> Unit,
    showCustomFileTypeConfigurationDialog: (CustomFileType) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(modifier = modifier) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                SectionHeader(
                    text = stringResource(id = R.string.file_types)
                )
                FilledTonalIconButton(onClick = showAddFileTypesBottomSheet) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = stringResource(R.string.add_a_file_type)
                    )
                }
            }
        }
        items(config.enabledFileTypes, key = { it }) { fileType ->
            i { "Laying out ${fileType.logIdentifier}" }

            FileTypeAccordion(
                fileType = fileType,
                excludeFileType = remember(fileType) {
                    {
                        reversibleConfig.onFileTypeCheckedChange(
                            fileType = fileType,
                            checkedNew = false
                        )
                    }
                },
                setSourceAutoMoveConfigs = remember(fileType) {
                    if (fileType.isMediaType) {
                        { autoMoveConfig ->
                            reversibleConfig.update {
                                it.copyWithAlteredAutoMoveConfigs(
                                    fileType = fileType,
                                    autoMoveConfig = autoMoveConfig
                                )
                            }
                        }
                    } else {
                        null
                    }
                },
                sourceTypeConfigMap = config.fileTypeConfig(fileType).sourceTypeConfigMap.toImmutableMap(),
                onSourceCheckedChange = remember(fileType) {
                    { source, checked ->
                        reversibleConfig.onFileSourceCheckedChange(
                            fileType = fileType,
                            sourceType = source,
                            checkedNew = checked
                        )
                    }
                },
                setSourceAutoMoveConfig = remember(fileType) {
                    { sourceType, autoMoveConfig ->
                        reversibleConfig.update {
                            it.copyWithAlteredAutoMoveConfig(fileType, sourceType) {
                                autoMoveConfig
                            }
                        }
                    }
                },
                showCustomFileTypeConfigurationDialog = showCustomFileTypeConfigurationDialog,
                modifier = Modifier
                    .padding(vertical = 4.dp)
                    .animateItem()
            )
        }
        item {
            SectionHeader(
                text = stringResource(id = R.string.more)
            )
            MoreColumnItems(
                showBatchMoveNotification = config.showBatchMoveNotification,
                setShowBatchMoveNotification = { checked ->
                    reversibleConfig.update { it.copy(showBatchMoveNotification = checked) }
                },
                disableOnLowBattery = config.disableOnLowBattery,
                setDisableOnLowBattery = { checked ->
                    reversibleConfig.update { it.copy(disableOnLowBattery = checked) }
                },
                startOnBoot = config.startOnBoot,
                setStartOnBoot = { checked ->
                    reversibleConfig.update { it.copy(startOnBoot = checked) }
                },
                modifier = Modifier
                    .padding(bottom = Padding.fabButtonBottomPadding)
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp)
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
private fun MoreColumnItems(
    showBatchMoveNotification: Boolean,
    setShowBatchMoveNotification: (Boolean) -> Unit,
    disableOnLowBattery: Boolean,
    setDisableOnLowBattery: (Boolean) -> Unit,
    startOnBoot: Boolean,
    setStartOnBoot: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(Spacing.VerticalItemRow)
    ) {
        SwitchItemRow(
            icon = { DefaultItemRowIcon(res = com.w2sv.core.navigator.R.drawable.ic_files_24) },
            labelRes = R.string.show_batch_move_notification,
            checked = showBatchMoveNotification,
            onCheckedChange = setShowBatchMoveNotification,
            explanation = stringResource(R.string.batch_move_explanation)
        )
        SwitchItemRow(
            icon = { DefaultItemRowIcon(res = R.drawable.ic_battery_low_24) },
            labelRes = R.string.disable_navigator_on_low_battery,
            checked = disableOnLowBattery,
            onCheckedChange = setDisableOnLowBattery
        )
        SwitchItemRow(
            icon = { DefaultItemRowIcon(res = R.drawable.ic_restart_24) },
            labelRes = R.string.start_navigator_on_system_boot,
            checked = startOnBoot,
            onCheckedChange = setStartOnBoot
        )
    }
}
